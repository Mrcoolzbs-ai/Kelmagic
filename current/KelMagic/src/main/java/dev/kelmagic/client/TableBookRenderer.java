package dev.kelmagic.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import dev.kelmagic.archive.ArchiveTableEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Table-supported hinged cover and page block. No bobbing, levitation or screen overlay. */
public final class TableBookRenderer implements BlockEntityRenderer<ArchiveTableEntity> {
 private static final ResourceLocation WHITE=ResourceLocation.fromNamespaceAndPath("kelmagic","textures/block/codex_white.png");
 private static final ResourceLocation COVER=ResourceLocation.fromNamespaceAndPath("kelmagic","textures/block/codex_cover.png");
 public TableBookRenderer(BlockEntityRendererProvider.Context context){}
 @Override public void render(ArchiveTableEntity table,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){
  if(!table.hasCodex()||table.getLevel()==null)return;
  var player=net.minecraft.client.Minecraft.getInstance().player;boolean near=player!=null&&player.position().distanceToSqr(dev.kelmagic.archive.TableOrientation.bookCenter(table.getBlockPos(),table.getBlockState()))<=100;var view=near?TableBookClient.view(table):null;float open=view==null?0:view.openness(partial);
  p.pushPose();p.translate(.5,.5,.5);var facing=table.getBlockState().getValue(dev.kelmagic.archive.ArchiveTableBlock.FACING);float yaw=switch(facing){case EAST->-90;case SOUTH->-180;case WEST->-270;default->0;};p.mulPose(Axis.YP.rotationDegrees(yaw));p.translate(-.5,-.5,-.5);p.translate(.5-.225*(1-open),TableBookView.BOOK_Y,TableBookView.BOOK_Z);
  box(p,buffers,0,-.04f,-.275f,.455f,-.023f,.275f,0xFF392536,light);
  box(p,buffers,0,-.023f,-.263f,.44f,0,.263f,0xFFD8C69D,light);
  p.pushPose();p.mulPose(Axis.ZP.rotationDegrees(-180*(1-open)));
  box(p,buffers,-.455f,-.04f,-.275f,0,-.023f,.275f,0xFF392536,light);
  box(p,buffers,-.44f,-.023f,-.263f,0,0,.263f,0xFFD8C69D,light);
  // Outside of the moving cover becomes the visible top of the CLOSED book.
  face(p,buffers,COVER,-.45f,-.0405f,-.27f,-.005f,.27f,0,0,1,1,0xFFFFFFFF,light);
  p.popPose();
  if(open>.98f&&view!=null&&view.ready()){
   face(p,buffers,view.texture(),-.52f,.001f,-.28f,.52f,.28f,0,1,1,0,0xFFFFFFFF,light);
   float progress=view.flipProgress();
   if(progress>=0){int count=view.flipCount();for(int i=0;i<count;i++){
    float phase=Math.clamp((progress*(count+1)-i)/2,0,1);if(phase<=0||phase>=1)continue;
    p.pushPose();p.translate(0,.003+i*.001,0);float angle=view.flipDirection()>0?180*phase:180*(1-phase);p.mulPose(Axis.ZP.rotationDegrees(angle));
    face(p,buffers,view.texture(),0,.001f,-.263f,.44f,.263f,.52f,.84f,.88f,.16f,0xFFFFFFFF,light);p.popPose();
   }}
  }
  p.popPose();
 }
 private static void face(PoseStack p,MultiBufferSource b,ResourceLocation texture,float x0,float y,float z0,float x1,float z1,float u0,float v0,float u1,float v1,int color,int light){
  var v=b.getBuffer(RenderType.entityCutoutNoCull(texture));var pose=p.last();
  vertex(v,pose,x0,y,z0,u0,v0,color,light,0,1,0);vertex(v,pose,x0,y,z1,u0,v1,color,light,0,1,0);vertex(v,pose,x1,y,z1,u1,v1,color,light,0,1,0);vertex(v,pose,x1,y,z0,u1,v0,color,light,0,1,0);
 }
 private static void vertex(VertexConsumer v,PoseStack.Pose p,float x,float y,float z,float u,float w,int c,int l,float nx,float ny,float nz){v.addVertex(p,x,y,z).setColor(c).setUv(u,w).setOverlay(OverlayTexture.NO_OVERLAY).setLight(l).setNormal(p,nx,ny,nz);}
 private static void box(PoseStack p,MultiBufferSource b,float x0,float y0,float z0,float x1,float y1,float z1,int color,int light){
  face(p,b,WHITE,x0,y0,z0,x1,z1,0,0,1,1,color,light);face(p,b,WHITE,x0,y1,z0,x1,z1,0,0,1,1,color,light);
  var v=b.getBuffer(RenderType.entityCutoutNoCull(WHITE));var q=p.last();
  float[][] pts={{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0},{x1,y0,z1},{x0,y0,z1},{x0,y1,z1},{x1,y1,z1},{x0,y0,z1},{x0,y0,z0},{x0,y1,z0},{x0,y1,z1},{x1,y0,z0},{x1,y0,z1},{x1,y1,z1},{x1,y1,z0}};
  for(int i=0;i<pts.length;i++)vertex(v,q,pts[i][0],pts[i][1],pts[i][2],i%2,i/2%2,color,light,0,1,0);
 }
}
