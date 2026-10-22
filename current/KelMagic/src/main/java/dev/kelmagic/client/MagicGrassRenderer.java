package dev.kelmagic.client;
import com.mojang.blaze3d.vertex.*;
import dev.kelmagic.KelMagic;
import dev.kelmagic.content.MagicDreams;
import dev.kelmagic.core.GrassWaves;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
/** One batched overlay for visible magical turf; no per-block entities or per-block render dispatch. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class MagicGrassRenderer {
 private static final RenderType TOP=RenderType.entityCutoutNoCullZOffset(ResourceLocation.fromNamespaceAndPath("kelmagic","textures/block/magic_grass_wave.png"));
 private static final RenderType SIDE=RenderType.entityCutoutNoCullZOffset(ResourceLocation.fromNamespaceAndPath("kelmagic","textures/block/magic_grass_overlay.png"));
 private static net.minecraft.world.level.Level cachedLevel;private static long cachedTick=Long.MIN_VALUE;private static BlockPos cachedCenter=BlockPos.ZERO;private static List<BlockPos> grass=List.of();private static List<GrassWaves.Source> sources=List.of();
 public MagicGrassRenderer(Object ignored){}
 public static int color(double x,double z,double ticks){return GrassWaves.color(x,0,z,ticks,List.of());}
 private static void scan(net.minecraft.world.level.Level l,BlockPos center){if(cachedLevel==l&&l.getGameTime()-cachedTick<20&&center.distSqr(cachedCenter)<64)return;cachedLevel=l;cachedTick=l.getGameTime();cachedCenter=center;int radius=Math.min(128,(Minecraft.getInstance().options.getEffectiveRenderDistance()+1)*16);var turf=new ArrayList<BlockPos>();var wave=new ArrayList<GrassWaves.Source>();for(int cx=(center.getX()-radius)>>4;cx<=(center.getX()+radius)>>4;cx++)for(int cz=(center.getZ()-radius)>>4;cz<=(center.getZ()+radius)>>4;cz++){var c=l.getChunkSource().getChunk(cx,cz,false);if(c==null)continue;for(int x=0;x<16;x++)for(int z=0;z<16;z++){int wx=(cx<<4)+x,wz=(cz<<4)+z;if((wx-center.getX())*(wx-center.getX())+(wz-center.getZ())*(wz-center.getZ())>radius*radius)continue;int y=l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,wx,wz)-1;var p=new BlockPos(wx,y,wz);if(l.getBlockState(p).is(MagicDreams.GRASS.get()))turf.add(p);}for(int sy=0;sy<c.getSections().length;sy++){var section=c.getSections()[sy];if(!section.getStates().maybeHas(s->s.is(dev.kelmagic.MagicResonance.MANA.get())))continue;int y0=l.getMinBuildHeight()+sy*16;for(int x=0;x<16;x++)for(int y=0;y<16;y++)for(int z=0;z<16;z++)if(section.getBlockState(x,y,z).is(dev.kelmagic.MagicResonance.MANA.get())){int wx=(cx<<4)+x,wz=(cz<<4)+z;if((wx-center.getX())*(wx-center.getX())+(wz-center.getZ())*(wz-center.getZ())<=112*112)wave.add(new GrassWaves.Source(wx+.5,y0+y,wz+.5));}}}grass=List.copyOf(turf);sources=List.copyOf(wave);}
 @SubscribeEvent public static void render(RenderLevelStageEvent e){if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;scan(mc.level,mc.player.blockPosition());Vec3 camera=e.getCamera().getPosition();var pose=e.getPoseStack();pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);var mat=pose.last();var buffers=mc.renderBuffers().bufferSource();double time=mc.level.getGameTime()+e.getPartialTick().getGameTimeDeltaPartialTick(false);
  // Non-fixed render types share a streaming builder. Finish the top pass before asking
  // BufferSource for the side texture; otherwise the second getBuffer closes the first.
  VertexConsumer top=buffers.getBuffer(TOP);for(var p:grass){if(!mc.level.getBlockState(p).is(MagicDreams.GRASS.get()))continue;int c=GrassWaves.color(p.getX()+.5,p.getY()+1,p.getZ()+.5,time,sources),light=LevelRenderer.getLightColor(mc.level,p.above());float x=p.getX(),y=p.getY(),z=p.getZ();if(!mc.level.getBlockState(p.above()).isSolidRender(mc.level,p.above()))quad(top,mat,x,y+1.002f,z,x,y+1.002f,z+1,x+1,y+1.002f,z+1,x+1,y+1.002f,z,c,light,0,1,0);}buffers.endBatch(TOP);
  VertexConsumer side=buffers.getBuffer(SIDE);for(var p:grass){if(!mc.level.getBlockState(p).is(MagicDreams.GRASS.get()))continue;int c=GrassWaves.color(p.getX()+.5,p.getY()+1,p.getZ()+.5,time,sources),light=LevelRenderer.getLightColor(mc.level,p.above());float x=p.getX(),y=p.getY(),z=p.getZ();for(var f:Direction.Plane.HORIZONTAL){if(mc.level.getBlockState(p.relative(f)).isSolidRender(mc.level,p.relative(f)))continue;float x1=x+(f==Direction.EAST?1:0),z1=z+(f==Direction.SOUTH?1:0),x2=f.getAxis()==Direction.Axis.X?x1:x+1,z2=f.getAxis()==Direction.Axis.Z?z1:z+1,o=.002f,dx=f.getStepX()*o,dz=f.getStepZ()*o;quad(side,mat,x1+dx,y+.625f,z1+dz,x1+dx,y+1.001f,z1+dz,x2+dx,y+1.001f,z2+dz,x2+dx,y+.625f,z2+dz,c,light,f.getStepX(),0,f.getStepZ());}}buffers.endBatch(SIDE);pose.popPose();}
 private static void quad(VertexConsumer o,PoseStack.Pose m,float x1,float y1,float z1,float x2,float y2,float z2,float x3,float y3,float z3,float x4,float y4,float z4,int c,int light,float nx,float ny,float nz){v(o,m,x1,y1,z1,c,0,0,light,nx,ny,nz);v(o,m,x2,y2,z2,c,0,1,light,nx,ny,nz);v(o,m,x3,y3,z3,c,1,1,light,nx,ny,nz);v(o,m,x4,y4,z4,c,1,0,light,nx,ny,nz);}
 private static void v(VertexConsumer o,PoseStack.Pose m,float x,float y,float z,int c,float u,float v,int light,float nx,float ny,float nz){o.addVertex(m,x,y,z).setColor(c).setUv(u,v).setOverlay(0).setLight(light).setNormal(m,nx,ny,nz);}
 public int getViewDistance(){return Math.min(128,(Minecraft.getInstance().options.getEffectiveRenderDistance()+1)*16);}
}
