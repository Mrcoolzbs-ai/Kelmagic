package dev.kelmagic.client.motion;

import dev.kelmagic.entity.RedMage;
import dev.kelmagic.client.face.MageGazeStates;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import java.util.WeakHashMap;

/** Client render adapter only: no navigation, velocity, targets or server orientation are written. */
@net.neoforged.fml.common.EventBusSubscriber(modid="kelmagic",value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class MageMotion {
 @net.neoforged.bus.api.SubscribeEvent public static void unload(net.neoforged.neoforge.event.level.LevelEvent.Unload event){if(event.getLevel().isClientSide())STATES.clear();}
 @net.neoforged.bus.api.SubscribeEvent public static void leave(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event){if(event.getEntity() instanceof RedMage e)STATES.remove(e);}

 private static final WeakHashMap<RedMage,MotionState> STATES=new WeakHashMap<>();
 private static final String[] BONES={"root","body","head","left_leg","right_leg","left_shin","right_shin","left_arm","right_arm","left_forearm","right_forearm"};
 public static boolean enabled(RedMage e){return e.isAlive()&&e.throne().isEmpty()&&!e.kitten()&&!e.isPassenger()&&!e.isInWater()&&!e.onClimbable()&&!e.isCrouching()&&!e.isFallFlying()&&!e.isNoGravity()&&!e.hasEffect(MobEffects.LEVITATION)&&(e.scene()==0||e.scene()==9);}
 public static MotionState prepare(RedMage e,float partial){
  MotionState s=STATES.get(e);if(s==null){s=new MotionState(e.getUUID().getMostSignificantBits()^e.getUUID().getLeastSignificantBits());STATES.put(e,s);}
  float p=Math.clamp(partial,0,1),body=Mth.rotLerp(p,e.yBodyRotO,e.yBodyRot),head=Mth.rotLerp(p,e.yHeadRotO,e.yHeadRot),pitch=Mth.lerp(p,e.xRotO,e.getXRot());
  var gaze=MageGazeStates.reading(e);int target=-1;
  var combat=e.visualCombatTarget();
  if(combat!=null){var v=combat.getEyePosition(p);double dx=v.x-e.getX(),dz=v.z-e.getZ(),dy=v.y-e.getEyeY();if(dx*dx+dz*dz>1e-8)head=(float)Math.toDegrees(Math.atan2(-dx,dz));pitch=(float)-Math.toDegrees(Math.atan2(dy,Math.hypot(dx,dz)));target=combat.getId();}
  else if(gaze.target()!=null && (gaze.kind()==MageGazeStates.Kind.PLAYER||gaze.kind()==MageGazeStates.Kind.HOSTILE||gaze.kind()==MageGazeStates.Kind.MOB||gaze.kind()==MageGazeStates.Kind.POINT)){
   var v=gaze.target();double dx=v.x-e.getX(),dz=v.z-e.getZ(),dy=v.y-e.getEyeY();
   if(dx*dx+dz*dz>.01){head=(float)Math.toDegrees(Math.atan2(-dx,dz));pitch=(float)-Math.toDegrees(Math.atan2(dy,Math.hypot(dx,dz)));target=gaze.entityId()<0?Integer.MIN_VALUE:gaze.entityId();}
  }
  s.sample((e.tickCount+p)/20.,Mth.lerp(p,e.xo,e.getX()),Mth.lerp(p,e.yo,e.getY()),Mth.lerp(p,e.zo,e.getZ()),body,head,pitch,e.onGround(),enabled(e),e.visualRelocating(),target);
  return s;
 }
 public static void apply(GeoModel<?> model,RedMage e,float partial){
  var s=prepare(e,partial);boolean combat=e.visualCombatTarget()!=null&&!e.kitten()&&e.throne().isEmpty();if(s.weight<.0001&&!combat)return;
  for(int i=0;i<BONES.length;i++){
   if(s.weight<.0001&&i!=MotionState.HEAD)continue;
   GeoBone b=model.getBone(BONES[i]).orElse(null);if(b==null)continue;
   // Talk owns head and right-arm gesture; the free left arm keeps its gait.
   if(e.scene()==9&&((i==MotionState.HEAD&&!combat)||i==MotionState.RIGHT_ARM||i==MotionState.RIGHT_FOREARM))continue;
   var rest=b.getInitialSnapshot();float[] q=s.pose[i];float w=combat&&i==MotionState.HEAD?1:s.weight;
   b.setRotX(Mth.lerp(w,b.getRotX(),rest.getRotX()+q[0]));b.setRotY(Mth.lerp(w,b.getRotY(),rest.getRotY()+q[1]));b.setRotZ(Mth.lerp(w,b.getRotZ(),rest.getRotZ()+q[2]));
   b.setPosX(Mth.lerp(w,b.getPosX(),rest.getOffsetX()+q[3]));b.setPosY(Mth.lerp(w,b.getPosY(),rest.getOffsetY()+q[4]));b.setPosZ(Mth.lerp(w,b.getPosZ(),rest.getOffsetZ()+q[5]));
  }
 }
 private MageMotion(){}
}
