package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.entity.RedMage;import net.minecraft.resources.ResourceLocation;import software.bernie.geckolib.model.GeoModel;import software.bernie.geckolib.animation.AnimationState;import software.bernie.geckolib.cache.object.GeoBone;
public final class RedMageGeoModel extends GeoModel<RedMage> {
 static ResourceLocation loc(String s){return ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,s);}
 public ResourceLocation getModelResource(RedMage e){return loc("geo/red_mage.geo.json");}
 public ResourceLocation getTextureResource(RedMage e){return loc("textures/entity/red_mage_geo.png");}
 public ResourceLocation getAnimationResource(RedMage e){return loc("animations/red_mage.animation.json");}
 private void bone(String n,java.util.function.Consumer<GeoBone> f){getBone(n).ifPresent(f);}
 @Override public void setCustomAnimations(RedMage e,long id,AnimationState<RedMage> state){super.setCustomAnimations(e,id,state);float p=state.getPartialTick(),t=e.tickCount+p;boolean cat=e.kitten(),seat=e.throne().isPresent();
 bone("cat",b->{b.setHidden(!cat);b.setChildrenHidden(!cat);});bone("body",b->{b.setHidden(cat);b.setChildrenHidden(cat);});for(String n:new String[]{"left_leg","right_leg"})bone(n,b->{b.setHidden(cat);b.setChildrenHidden(cat);});
 boolean book=e.scene()==2||e.scene()==25;bone("book",b->{b.setHidden(!book);b.setChildrenHidden(!book);b.setScaleX(1);b.setScaleY(1);b.setScaleZ(1);});
 if(e.scene()==0&&!e.speech()&&!seat)bone("head",b->{b.setRotY(b.getInitialSnapshot().getRotY()+(float)Math.toRadians(Math.clamp(net.minecraft.util.Mth.wrapDegrees(e.yHeadRot-e.yBodyRot),-55,55))*.65f);b.setRotX(b.getInitialSnapshot().getRotX()+(float)Math.toRadians(Math.clamp(e.getXRot(),-25,35))*.65f);});
 float breath=dev.kelmagic.client.face.MouthStates.breathingEnabled(e)?RedMage.breathing(t)*.025f:0;
 for(String side:new String[]{"left","right"}){
  bone("chest_"+side,b->{b.setScaleY(1+breath*.45f);b.setScaleZ(1+breath);var dyn=side.equals("left")?e.chestLeft:e.chestRight;b.setPosY(b.getInitialSnapshot().getOffsetY()+(MageClientConfig.PHYSICS.get()?Math.clamp(-dyn.lift(p),-.18f,.18f):0));b.setRotX(b.getInitialSnapshot().getRotX()+(MageClientConfig.PHYSICS.get()?Math.clamp(dyn.pitch(p),-.06f,.06f):0));});
 }
 // Rear hair stays attached to the moving head. Only pitch compensation lets it hang under gravity.
 bone("head",h->{bone("hair_back",b->{b.setHidden(false);b.setChildrenHidden(false);b.setScaleZ(1);b.setPosZ(b.getInitialSnapshot().getOffsetZ());b.setRotX(-h.getRotX()+(seat?.1f:0)+(MageClientConfig.ACCESSORIES.get()?Math.clamp(e.hairDynamics.pitch(p)*.5f,-.04f,.04f):0));});
 for(String side:new String[]{"left","right"})bone("hair_front_"+side,b->{b.setRotX(b.getInitialSnapshot().getRotX()-h.getRotX()*.7f);b.setRotZ((float)Math.toRadians(side.equals("left")?24:-24));});});
 for(String n:new String[]{"hair_mid","hair_tip"})bone(n,b->{b.setHidden(false);b.setRotX(b.getInitialSnapshot().getRotX()+(MageClientConfig.ACCESSORIES.get()?Math.clamp(e.hairDynamics.pitch(p)*.4f,-.035f,.035f):0));});
 // Recessed sockets: pupils track the look direction; eyelids and brows supply expression without floating plates.
 bone("head",h->dev.kelmagic.client.face.MageEyeStates.headPose(e,h,p));
 int mood=e.expression();var eyes=dev.kelmagic.client.face.MageEyeStates.sample(e,p);
 for(String side:new String[]{"left","right"}){
  // Sole eyelid transform writer; GeckoLib clips are evaluated before this hook.
  bone("lid_"+side,b->b.setScaleY(side.equals("left")?eyes.leftMask():eyes.rightMask()));
  bone("pupil_"+side,b->{b.setPosX(b.getInitialSnapshot().getOffsetX()+Math.clamp(net.minecraft.util.Mth.wrapDegrees(e.yHeadRot-e.yBodyRot)*.007f,-.3f,.3f));b.setPosY(b.getInitialSnapshot().getOffsetY()+Math.clamp(-e.getXRot()*.006f,-.12f,.12f));});
  bone("brow_"+side,b->{b.setRotZ(mood==3?(side.equals("left")?-.16f:.16f):mood==1?(side.equals("left")?-.09f:.09f):0);b.setPosY(b.getInitialSnapshot().getOffsetY()+(mood==1?.12f:mood==3?-.12f:0));});
 }
 // These cloth caps now belong to their own thigh, including crossed-leg and crouch scenes.
 bone("tunic_back",b->b.setHidden(seat));for(String n:new String[]{"pouch","pouch_strap"})bone(n,b->b.setPosY(b.getInitialSnapshot().getOffsetY()+(seat?2:0)));
 }
}
