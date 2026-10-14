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
 public static boolean ignite(LivingEntity e){
  if(!e.isAlive() || e.isSpectator() || e instanceof Player p && p.getAbilities().invulnerable)return false;
  int old=e.getData(MagicAttachments.ARCANE_BURN);e.setData(MagicAttachments.ARCANE_BURN,Math.max(old,Config.FIRE_SECONDS.get()*60));if(old==0)sync(e);return true;
 }
 public static void advance(LivingEntity e){
  int left=e.getData(MagicAttachments.ARCANE_BURN);if(left<=0)return;
  if(!e.isAlive() || e.isSpectator() || e instanceof Player p && p.getAbilities().invulnerable || e.isInWaterRainOrBubble()){e.setData(MagicAttachments.ARCANE_BURN,0);sync(e);return;}
  if(e.tickCount%20==0){
   var velocity=e.getDeltaMovement();boolean impulse=e.hasImpulse;
   e.hurt(new DamageSource(e.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE)),1);
   e.setDeltaMovement(velocity);e.hasImpulse=impulse;
  }
  // Keep the status alive during the final damage call so its death loot is cooked too.
  e.setData(MagicAttachments.ARCANE_BURN,left-1);
  if(left==1 || e.tickCount%20==0)sync(e);
 }
 @SubscribeEvent public static void tick(EntityTickEvent.Post e){if(!e.getEntity().level().isClientSide && e.getEntity() instanceof LivingEntity l)advance(l);}
 @SubscribeEvent public static void tracking(PlayerEvent.StartTracking e){if(e.getEntity() instanceof ServerPlayer observer && e.getTarget() instanceof LivingEntity target)PacketDistributor.sendToPlayer(observer,new BurnSnapshot(target.getId(),target.getData(MagicAttachments.ARCANE_BURN)));}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)sync(p);}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)sync(p);}
 private ArcaneFire(){}
}
