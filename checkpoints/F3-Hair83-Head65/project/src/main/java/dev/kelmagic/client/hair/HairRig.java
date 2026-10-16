package dev.kelmagic.client.hair;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.RenderUtil;
/** Only hair bones are written. Chest/eye/mouth/breathing controllers are read, never driven here. */
public final class HairRig {
 /** Composition exactly matches GeckoLib prepMatrixForBone, stopping before the common body ancestor. */
 public static Matrix4f belowBody(GeoBone bone){
  java.util.ArrayDeque<GeoBone> chain=new java.util.ArrayDeque<>();
  for(var b=bone;b!=null&&!b.getName().equals("body");b=b.getParent())chain.addFirst(b);
  PoseStack p=new PoseStack();for(var b:chain)RenderUtil.prepMatrixForBone(p,b);return new Matrix4f(p.last().pose());
 }
 public static void apply(GeoModel<?> model){
  for(String side:new String[]{"left","right"}){
   var top=model.getBone("hair_"+side).orElse(null);var chest=model.getBone("chest_"+side).orElse(null);var bridge=model.getBone("hair_front_"+side).orElse(null);
   if(top==null||chest==null||bridge==null)continue;
   int sign=side.equals("left")?1:-1;
   Vector3f a=belowBody(top).transformPosition(new Vector3f(-sign*3.58f/16,24.35f/16,-4.58f/16));
   Vector3f b=belowBody(chest).transformPosition(new Vector3f(-sign*3.16f/16,23.05f/16,-6.09f/16));
   Vector3f delta=new Vector3f(a).sub(b);var aim=HairBridgeMath.aim(delta.x,delta.y,delta.z);
   bridge.setPosX(-b.x*16);bridge.setPosY(b.y*16);bridge.setPosZ(b.z*16);
   bridge.setRotX((float)aim.pitch());bridge.setRotY(0);bridge.setRotZ((float)aim.roll());bridge.setScaleX(1);bridge.setScaleY((float)(aim.length()*16));bridge.setScaleZ(1);
  }
 }
 private HairRig(){}
}
