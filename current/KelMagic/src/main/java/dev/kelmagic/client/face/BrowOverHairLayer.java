package dev.kelmagic.client.face;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.kelmagic.KelMagic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.RenderUtil;
import java.util.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

/** Screen-registered brow surface, not forward-extruded geometry.
 * Mark original front quads against world depth BEFORE head/hair; replay ONLY these pixels
 * AFTER the model, with stencil. Nothing writes depth, shifts XY, edits bones or hair.
 * Front-facing quads only: no through-back-of-head brows. One reserved stencil bit, restored.
 */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class BrowOverHairLayer {
 private static final int BIT=0x40;
 private record Patch(Matrix4f pose,Matrix3f normal,GeoQuad quad,String side){}
 private final List<Patch> patches=new ArrayList<>();
 private final MultiBufferSource.BufferSource own=MultiBufferSource.immediate(new ByteBufferBuilder(16384));
 public void begin(MultiBufferSource buffers){patches.clear();if(buffers instanceof MultiBufferSource.BufferSource b)b.endBatch();}
 @SubscribeEvent public static void frame(net.neoforged.neoforge.client.event.RenderFrameEvent.Pre event){
  var target=Minecraft.getInstance().getMainRenderTarget();if(!target.isStencilEnabled())target.enableStencil();
 }
 public void mark(PoseStack parent,GeoBone head,GeoModel<?> model,int light,int overlay){
  if(Boolean.getBoolean("kelmagic.preview.hideBrowOverlay"))return;
  parent.pushPose();RenderUtil.prepMatrixForBone(parent,head);
  for(String side:new String[]{"left","right"})model.getBone("brow_"+side).ifPresent(b->{
   if(b.isHidden())return;parent.pushPose();RenderUtil.prepMatrixForBone(parent,b);
   for(var cube:b.getCubes())for(var q:cube.quads())if(q!=null&&q.direction()==Direction.NORTH){
    var cameraMatrix=new Matrix4f(RenderSystem.getModelViewMatrix()).mul(parent.last().pose());
    var center=new Vector3f();for(var v:q.vertices())center.add(v.position());center.mul(.25f);cameraMatrix.transformPosition(center);
    var normal=new Vector3f(q.normal());cameraMatrix.transformDirection(normal).normalize();
    if(normal.dot(new Vector3f(center).negate().normalize())>.12f)patches.add(new Patch(new Matrix4f(parent.last().pose()),new Matrix3f(parent.last().normal()),q,side));
   }parent.popPose();
  });parent.popPose();if(patches.isEmpty())return;
  var state=new StencilState();try{
   GL11.glEnable(GL11.GL_STENCIL_TEST);GL11.glStencilMask(BIT);GL11.glClearStencil(0);GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
   GL11.glStencilFunc(GL11.GL_ALWAYS,BIT,BIT);GL11.glStencilOp(GL11.GL_KEEP,GL11.GL_KEEP,GL11.GL_REPLACE);
   draw(true,light,overlay);
  }finally{state.restore();}
 }
 public void finish(MultiBufferSource buffers,int light,int overlay){
  if(patches.isEmpty())return;if(buffers instanceof MultiBufferSource.BufferSource b)b.endBatch();
  var state=new StencilState();try{
   GL11.glEnable(GL11.GL_STENCIL_TEST);GL11.glStencilMask(0);GL11.glStencilFunc(GL11.GL_EQUAL,BIT,BIT);GL11.glStencilOp(GL11.GL_KEEP,GL11.GL_KEEP,GL11.GL_KEEP);
   draw(false,light,overlay);
  }finally{state.restore();patches.clear();}
 }
 private void draw(boolean mask,int light,int overlay){
  for(var p:patches){var type=Types.type(p.side,mask);var out=own.getBuffer(type);int start=p.side.equals("left")?322:546;
   var normal=new Vector3f(p.quad.normal());p.normal.transform(normal);
   for(var v:p.quad.vertices()){var pos=new Vector3f(v.position());p.pose.transformPosition(pos);
    out.addVertex(pos.x,pos.y,pos.z).setColor(-1).setUv((2+(v.texU()*1024-start))/32,(2+(v.texV()*1024-802))/32).setOverlay(overlay).setLight(light).setNormal(normal.x,normal.y,normal.z);
   }
  }own.endBatch();
 }
 private static final class Types extends RenderType {
  Types(){super("unused",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,256,false,false,()->{},()->{});}
  private static RenderType create(String side,boolean mask){return RenderType.create("kelmagic_brow_"+side+(mask?"_mask":"_overlay"),DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,256,false,false,CompositeState.builder()
   .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER).setTextureState(new TextureStateShard(ResourceLocation.fromNamespaceAndPath("kelmagic","textures/entity/face/brow_"+side+".png"),false,false))
   .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
   .setDepthTestState(mask?LEQUAL_DEPTH_TEST:NO_DEPTH_TEST).setWriteMaskState(new WriteMaskStateShard(!mask,false)).createCompositeState(false));}
  private static final RenderType LM=create("left",true),RM=create("right",true),LO=create("left",false),RO=create("right",false);
  static RenderType type(String side,boolean mask){return side.equals("left")?(mask?LM:LO):(mask?RM:RO);}
 }
 private static final class StencilState {
  final boolean enabled=GL11.glIsEnabled(GL11.GL_STENCIL_TEST);final int clear=GL11.glGetInteger(GL11.GL_STENCIL_CLEAR_VALUE);
  final int func=GL11.glGetInteger(GL11.GL_STENCIL_FUNC),ref=GL11.glGetInteger(GL11.GL_STENCIL_REF),value=GL11.glGetInteger(GL11.GL_STENCIL_VALUE_MASK),write=GL11.glGetInteger(GL11.GL_STENCIL_WRITEMASK),fail=GL11.glGetInteger(GL11.GL_STENCIL_FAIL),zfail=GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_FAIL),pass=GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_PASS);
  final int bfunc=GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_STENCIL_BACK_FUNC),bref=GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_STENCIL_BACK_REF),bvalue=GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_STENCIL_BACK_VALUE_MASK),bwrite=GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_STENCIL_BACK_WRITEMASK),bfail=GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_STENCIL_BACK_FAIL),bzfail=GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_STENCIL_BACK_PASS_DEPTH_FAIL),bpass=GL11.glGetInteger(org.lwjgl.opengl.GL20.GL_STENCIL_BACK_PASS_DEPTH_PASS);
  void restore(){org.lwjgl.opengl.GL20.glStencilFuncSeparate(GL11.GL_FRONT,func,ref,value);org.lwjgl.opengl.GL20.glStencilMaskSeparate(GL11.GL_FRONT,write);org.lwjgl.opengl.GL20.glStencilOpSeparate(GL11.GL_FRONT,fail,zfail,pass);org.lwjgl.opengl.GL20.glStencilFuncSeparate(GL11.GL_BACK,bfunc,bref,bvalue);org.lwjgl.opengl.GL20.glStencilMaskSeparate(GL11.GL_BACK,bwrite);org.lwjgl.opengl.GL20.glStencilOpSeparate(GL11.GL_BACK,bfail,bzfail,bpass);GL11.glClearStencil(clear);if(enabled)GL11.glEnable(GL11.GL_STENCIL_TEST);else GL11.glDisable(GL11.GL_STENCIL_TEST);}
 }
}
