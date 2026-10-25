package dev.kelmagic.server;
import dev.kelmagic.*;
import dev.kelmagic.network.BurnSnapshot;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
@EventBusSubscriber(modid=KelMagic.MODID)
public final class ArcaneFire {
 public static final ResourceKey<DamageType> DAMAGE=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"arcane_fire"));
 private static void sync(LivingEntity e){PacketDistributor.sendToPlayersTrackingEntityAndSelf(e,new BurnSnapshot(e.getId(),e.getData(MagicAttachments.ARCANE_BURN)));}
 public static void extinguish(LivingEntity e){if(e.hasData(MagicAttachments.ARCANE_BURN)&&e.getData(MagicAttachments.ARCANE_BURN)>0){e.setData(MagicAttachments.ARCANE_BURN,0);sync(e);}}
 public static boolean ignite(LivingEntity e){return ignite(e,Config.FIRE_SECONDS.get()*60,1f);}
 public static boolean ignite(LivingEntity e,int duration,float power){
  if(dev.kelmagic.artifact.ArtifactRuntime.immune(e)||!e.isAlive() || e.isSpectator() || e instanceof Player p && p.getAbilities().invulnerable)return false;
  int old=e.getData(MagicAttachments.ARCANE_BURN);float oldPower=e.getData(MagicAttachments.ARCANE_POWER);
  int next=Math.max(old,Math.max(1,Math.min(3600,duration)));float nextPower=old==0?Math.max(.01f,Math.min(16,power)):Math.max(oldPower,Math.max(.01f,Math.min(16,power)));
  e.setData(MagicAttachments.ARCANE_BURN,next);e.setData(MagicAttachments.ARCANE_POWER,nextPower);
  if(old==0 || next>old+20 || nextPower!=oldPower)sync(e);return true;
 }
 public static void advance(LivingEntity e){
  if(!e.hasData(MagicAttachments.ARCANE_BURN))return;int left=e.getData(MagicAttachments.ARCANE_BURN);if(left<=0)return;
  if(!e.isAlive() || e.isSpectator() || e instanceof Player p && p.getAbilities().invulnerable || e.isInWaterRainOrBubble()){e.setData(MagicAttachments.ARCANE_BURN,0);sync(e);return;}
  if(e.tickCount%20==0){
   var velocity=e.getDeltaMovement();boolean impulse=e.hasImpulse;
   e.hurt(new DamageSource(e.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE)),e.getData(MagicAttachments.ARCANE_POWER));
   e.setDeltaMovement(velocity);e.hasImpulse=impulse;
  }
  // Keep the status alive during the final damage call so its death loot is cooked too.
  e.setData(MagicAttachments.ARCANE_BURN,left-1);
  if(left==1 || e.tickCount%20==0)sync(e);
 }
 @SubscribeEvent public static void tick(EntityTickEvent.Post e){if(!e.getEntity().level().isClientSide && e.getEntity() instanceof LivingEntity l)advance(l);}
 @SubscribeEvent public static void tracking(PlayerEvent.StartTracking e){if(e.getEntity() instanceof ServerPlayer observer && e.getTarget() instanceof LivingEntity target)PacketDistributor.sendToPlayer(observer,new BurnSnapshot(target.getId(),target.hasData(MagicAttachments.ARCANE_BURN)?target.getData(MagicAttachments.ARCANE_BURN):0));}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)sync(p);}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)sync(p);}
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
 public static void contact(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event){
  var victim=event.getEntity();var source=event.getSource();if(victim.level().isClientSide||source.is(DAMAGE)||!(source.getEntity() instanceof LivingEntity attacker)||attacker==victim||event.getNewDamage()<=0)return;
  if(attacker instanceof Player a && victim instanceof Player b && (!a.canHarmPlayer(b)||attacker.level().getServer()!=null&&!attacker.level().getServer().isPvpAllowed()))return;
  int a=attacker.hasData(MagicAttachments.ARCANE_BURN)?attacker.getData(MagicAttachments.ARCANE_BURN):0,b=victim.hasData(MagicAttachments.ARCANE_BURN)?victim.getData(MagicAttachments.ARCANE_BURN):0;if(a<=0&&b<=0)return;float ap=a>0?attacker.getData(MagicAttachments.ARCANE_POWER):1,bp=b>0?victim.getData(MagicAttachments.ARCANE_POWER):1;
  if(a>0&&!victim.isInWaterRainOrBubble())ignite(victim,a,ap);
  if(b>0&&!attacker.isInWaterRainOrBubble())ignite(attacker,b,bp);
 }
 private ArcaneFire(){}
}
