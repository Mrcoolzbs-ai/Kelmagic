package dev.kelmagic.client;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.kelmagic.*;
import dev.kelmagic.item.SeekerLanternItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent;
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)public final class ApprovedContentClient {
 static boolean active(Minecraft mc){if(mc.player==null)return false;float c=mc.player.getCooldowns().getCooldownPercent(MagicApprovedContent.SEEKER_LANTERN.get(),0);return c>=(float)(SeekerLanternItem.COOLDOWN-SeekerLanternItem.REVEAL_TICKS)/SeekerLanternItem.COOLDOWN;}
 @SubscribeEvent public static void render(RenderLevelStageEvent e){if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.options.hideGui||!active(mc))return;Vec3 camera=e.getCamera().getPosition();var pose=e.getPoseStack();pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);VertexConsumer buf=mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());var level=mc.level;int cx=mc.player.chunkPosition().x,cz=mc.player.chunkPosition().z,r=Mth.ceil(50/16f);boolean any=false;for(int x=cx-r;x<=cx+r;x++)for(int z=cz-r;z<=cz+r;z++){if(!level.hasChunk(x,z))continue;var chunk=level.getChunkSource().getChunk(x,z,ChunkStatus.FULL,false);if(chunk==null)continue;for(var be:chunk.getBlockEntities().values()){if(!(be instanceof ChestBlockEntity||be instanceof EnderChestBlockEntity||be instanceof BarrelBlockEntity||be instanceof ShulkerBoxBlockEntity))continue;if(be.getBlockPos().distSqr(mc.player.blockPosition())>2500)continue;LevelRenderer.renderLineBox(pose,buf,new AABB(be.getBlockPos()).inflate(.02),.35f,.95f,1,.95f);any=true;}}pose.popPose();if(any)mc.renderBuffers().bufferSource().endBatch(RenderType.lines());}
 @SubscribeEvent public static void muffle(PlaySoundSourceEvent e){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;var s=e.getSound();if(s.isRelative())return;Vec3 from=new Vec3(s.getX(),s.getY(),s.getZ()),to=mc.player.getEyePosition();if(from.distanceToSqr(to)<=2304&&crosses(mc.level,from,to))e.getChannel().setVolume(s.getVolume()*.32f);}
 static boolean crosses(ClientLevel l,Vec3 a,Vec3 b){Vec3 d=b.subtract(a);double len=d.length();if(len<.001)return false;Vec3 step=d.scale(.5/len),q=a;for(double t=0;t<=len;t+=.5){var p=BlockPos.containing(q);if(l.hasChunkAt(p)&&l.getBlockState(p).is(MagicApprovedContent.ECHO_ISOLATOR.get()))return true;q=q.add(step);}return false;}
}
