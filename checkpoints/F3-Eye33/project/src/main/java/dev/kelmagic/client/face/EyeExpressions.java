package dev.kelmagic.client.face;
import dev.kelmagic.entity.RedMage;import java.util.*;import net.minecraft.world.entity.LivingEntity;
/** Client-side named expression API; no automatic gameplay/emotion producers installed. */
public final class EyeExpressions {
 private static final Map<RedMage,EyeExpressionController> STATES=Collections.synchronizedMap(new WeakHashMap<>());
 private static EyeExpressionController entry(RedMage e){return STATES.computeIfAbsent(e,k->new EyeExpressionController());}
 private static double now(RedMage e,float p){return (e.tickCount+(double)Math.clamp(p,0,1))/20.0;}
 public static long play(RedMage e,String name,EyeExpressionController.Options options){return play(e,EyeExpressionController.Preset.valueOf(name.toUpperCase(Locale.ROOT)),options);}
 public static long play(RedMage e,EyeExpressionController.Preset name,EyeExpressionController.Options options){return entry(e).play(now(e,0),name,options);}
 public static long transferTo(RedMage e,LivingEntity target,EyeExpressionController.Options options){MageGazeStates.selectEntity(e,target,options.duration()+3);return play(e,EyeExpressionController.Preset.TARGET_TRANSFER,options);}
 public static boolean cancel(RedMage e,long handle,double fadeSeconds){return entry(e).cancel(now(e,0),handle,fadeSeconds);}
 public static void cancel(RedMage e){var p=entry(e);p.cancel(now(e,0),p.token(),.22);}
 public static Set<String> names(){var n=new LinkedHashSet<String>();for(var p:EyeExpressionController.Preset.values())n.add(p.name());return Collections.unmodifiableSet(n);}
 public static EyeExpressionController.Frame sample(RedMage e,float partial){return entry(e).sample(now(e,partial));}
 public static EyeStateController.Frame compose(EyeStateController.Frame base,EyeExpressionController.Frame a){
  float l=Math.min(1/.93f,base.leftOpening()*(1+a.widen()))*(1-a.closeLeft());float r=Math.min(1/.93f,base.rightOpening()*(1+a.widen()))*(1-a.closeRight());
  return new EyeStateController.Frame(l,r,base.blinkClosure(),base.burstActive(),base.cheekLeft(),base.cheekRight(),base.lowerLeft(),base.lowerRight());
 }
 private EyeExpressions(){}
}
