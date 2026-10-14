package dev.kelmagic.server;
import dev.kelmagic.*;
import dev.kelmagic.core.SpellProfiles;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
@EventBusSubscriber(modid=KelMagic.MODID)
public final class FlameLife {
 public static boolean grant(ServerPlayer p,double scale){
  p.setData(MagicAttachments.FLAME_HEAL,(float)Math.max(.1,Math.min(24,2*scale)));
  p.addEffect(new MobEffectInstance(MagicContent.FLAME_LIFE,SpellProfiles.duration(1200,scale),0));return true;
 }
 @SubscribeEvent(priority=EventPriority.HIGH)
 public static void incoming(LivingIncomingDamageEvent event){
  var e=event.getEntity();if(!e.hasEffect(MagicContent.FLAME_LIFE))return;
  var s=event.getSource();
  // Do not use IS_FIRE: that includes LAVA/HOT_FLOOR, which must retain their damage.
  if(s.is(DamageTypes.IN_FIRE) || s.is(DamageTypes.ON_FIRE) || s.is(ArcaneFire.DAMAGE))event.setCanceled(true);
 }
 private FlameLife(){}
}
