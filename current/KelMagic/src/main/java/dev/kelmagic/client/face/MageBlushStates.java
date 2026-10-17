package dev.kelmagic.client.face;
import dev.kelmagic.KelMagic;import dev.kelmagic.entity.RedMage;
import java.util.WeakHashMap;
import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.bus.api.SubscribeEvent;
/** Render-thread API, per client entity. Default OFF; no automatic signals or network policy. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class MageBlushStates {
 private static final WeakHashMap<RedMage,CheekBlushController> VALUES=new WeakHashMap<>();
 private static CheekBlushController value(RedMage e){return VALUES.computeIfAbsent(e,k->new CheekBlushController());}
 public static void set(RedMage e,float intensity){value(e).set(intensity);}
 public static void transition(RedMage e,float intensity,double seconds){transition(e,intensity,seconds,0);}
 public static void transition(RedMage e,float intensity,double seconds,float partial){value(e).transition(intensity,seconds,(e.tickCount+(double)partial)/20);}
 public static float sample(RedMage e,float partial){var v=VALUES.get(e);return v==null?0:v.sample((e.tickCount+(double)partial)/20);}
 public static void clear(RedMage e){VALUES.remove(e);}
 @SubscribeEvent public static void unload(net.neoforged.neoforge.event.level.LevelEvent.Unload event){if(event.getLevel().isClientSide())VALUES.clear();}
 private MageBlushStates(){}
}
