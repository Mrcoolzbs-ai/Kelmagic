package dev.kelmagic.client.face;
import dev.kelmagic.KelMagic;
import dev.kelmagic.entity.RedMage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import java.util.WeakHashMap;
/** Client-only, per-entity state. Values do not retain their weak entity keys.
 * Only already registered/rendered entities are updated: no world scan or sensory polling. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class MageEyeStates {
 private static final WeakHashMap<RedMage,Entry> STATES=new WeakHashMap<>();
 private static final class Entry {
  final EyeStateController controller;final LightExposureSensor light=new LightExposureSensor();float squint;float cheekL,cheekR,lowerL,lowerR;
  Entry(RedMage e){controller=new EyeStateController(e.getUUID().getMostSignificantBits()^e.getUUID().getLeastSignificantBits(),e.tickCount/20.0,EyeStateController.NORMAL);}
 }
 private static Entry entry(RedMage e){return STATES.computeIfAbsent(e,Entry::new);}
 private static void update(RedMage e,Entry v){int mood=e.expression();float mask=mood==1?.25f:mood==3?.35f:e.scene()==27?.5f:.07f;
  var signal=v.controller.signals();if(signal.expressionState()!=mood)v.controller.setSignals(new EyeStateController.ContextSignals(signal.lightExposure(),signal.lookingAtPlayer(),signal.nearbyExplosionTime(),signal.asymmetricSquintLeft(),signal.asymmetricSquintRight(),mood));
  float opening=(1-mask)/.93f;
  float light=0; // E muscle experiment: environmental reactions explicitly disconnected.
  float combined=1-(1-Math.clamp(v.squint,0,1))*(1-light);
  v.controller.setBase(e.tickCount/20.0,opening,opening,combined);
  v.controller.setMuscles(e.tickCount/20.0,Math.max(v.cheekL,v.squint*.35f),Math.max(v.cheekR,v.squint*.35f),Math.max(v.lowerL,v.squint*.55f),Math.max(v.lowerR,v.squint*.55f));
 }
 public static EyeStateController.Frame sample(RedMage e,float partial){Entry v=entry(e);update(e,v);return v.controller.sample((e.tickCount+partial)/20.0);}
 public static void headPose(RedMage e,software.bernie.geckolib.cache.object.GeoBone h,float partial){/* Intentionally disconnected in experimental E: no sun/lava sensor. */}
 public static LightExposureSensor.Reading lightReading(RedMage e){return entry(e).light.reading();}
 public static void sustainedSquint(RedMage e,float value){Entry v=entry(e);v.squint=value;update(e,v);}
 /** QA/interface input, normalized [0,1]. No automatic smile/asymmetry gameplay. */
 public static void musclePose(RedMage e,float cl,float cr,float ll,float lr){Entry v=entry(e);v.cheekL=cl;v.cheekR=cr;v.lowerL=ll;v.lowerR=lr;update(e,v);}
 public static void context(RedMage e,EyeStateController.ContextSignals value){entry(e).controller.setSignals(value);}
 public static void blinkProfile(RedMage e,EyeStateController.BlinkProfile p){entry(e).controller.setBlinkProfile(e.tickCount/20.0,p);}
 public static void burst(RedMage e){entry(e).controller.requestBurst(e.tickCount/20.0,3,EyeStateController.FAST,.075);}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
  STATES.entrySet().removeIf(pair->pair.getKey().isRemoved());
  STATES.forEach((e,v)->{

   update(e,v);v.controller.advance(e.tickCount/20.0);
  });
 }
 @SubscribeEvent public static void unload(net.neoforged.neoforge.event.level.LevelEvent.Unload event){if(event.getLevel().isClientSide())STATES.clear();}
 private MageEyeStates(){}
}
