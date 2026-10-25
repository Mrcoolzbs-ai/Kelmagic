package dev.kelmagic.artifact;
import dev.kelmagic.*;
import dev.kelmagic.server.*;
import dev.kelmagic.core.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.*;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
@EventBusSubscriber(modid=KelMagic.MODID)
public final class ArtifactRuntime {
    public static final class State {public Vec3 last=Vec3.ZERO;public long combatUntil,arrivalUntil,rootAt,dewAt,lastAction,movedUntil;public double regen;public float paid;public boolean ownSilent;public long silentActionUntil;public Vec3 silentActionAt=Vec3.ZERO;}
    private static final Map<ServerPlayer,State> STATES=new WeakHashMap<>();
    public static State state(ServerPlayer p){return STATES.computeIfAbsent(p,u->new State());}
    public static boolean has(LivingEntity p,Item item){return ArtifactAccessories.has(p,item);}
    public static int pieces(LivingEntity p,String set){int count=0;for(var slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET)){var s=p.getItemBySlot(slot);if(s.getItem() instanceof ArtifactArmorItem armor&&armor.set.equals(set)&&armor.getEquipmentSlot()==slot)count++;}return count;}
    public static boolean immune(LivingEntity p){return pieces(p,"strict_negation")==4;}
    public static boolean silent(Entity e){return e instanceof LivingEntity l&&has(l,ArtifactContent.SILENT.get());}
    public static double maximum(ServerPlayer p){return has(p,ArtifactContent.DEEP.get())?10:0;}
    public static double range(ServerPlayer p){return Config.RANGE.get()*(has(p,ArtifactContent.FAR.get())&&!ArtifactMagic.area(p)?1.5:1);}
    public static boolean grove(LivingEntity p){return p.level().getBlockState(p.blockPosition().below()).is(dev.kelmagic.content.MagicDreams.GRASS.get());}
    public static boolean canCast(ServerPlayer p){return p.serverLevel().getGameTime()>=p.getPersistentData().getLong("KelMuteUntil")&&!ArtifactWorld.muted(p.serverLevel(),p.position());}
    public static boolean canSpeak(ServerPlayer p){return !silent(p)&&canCast(p);}
    public static boolean canPayWord(ServerPlayer p,String word){return !has(p,ArtifactContent.CAUTIOUS.get())||p.isCreative()||p.getData(MagicAttachments.MANA).current()+1e-8>=word.length();}
    public static void regenerate(ServerPlayer p,MagicAttachments.Mana mana,double max){var s=state(p);int interval=ManaMath.interval(mana.dragonUpgrade(),p.hasEffect(MagicContent.FLOW));double factor=ArtifactRules.regenFactor(has(p,ArtifactContent.DEEP.get()),pieces(p,"deep_source"));if(has(p,ArtifactContent.CALM.get())&&p.serverLevel().getGameTime()>=s.combatUntil&&p.serverLevel().getGameTime()>=s.movedUntil&&p.position().distanceToSqr(s.last)<.0001&&p.getDeltaMovement().lengthSqr()<.0004)factor*=1.6;
        if(has(p,ArtifactContent.EVEN.get())){addMana(p,factor/interval);return;}
        if(factor==1){if(p.getData(MagicAttachments.SESSION).regenDue(interval)&&mana.current()<max)addMana(p,1);return;}
        s.regen+=factor;if(s.regen>=interval){s.regen-=interval;addMana(p,1);}
    }
    public static void addMana(ServerPlayer p,double amount){if(amount<=0)return;var mana=p.getData(MagicAttachments.MANA);p.setData(MagicAttachments.MANA,mana.withCurrent(Math.min(MagicServer.maximum(p),mana.current()+amount)));}
    public static boolean spend(ServerPlayer p,double amount){if(!Double.isFinite(amount)||amount<0)return false;if(p.isCreative())return true;var mana=p.getData(MagicAttachments.MANA);if(mana.current()+1e-8<amount)return false;p.setData(MagicAttachments.MANA,mana.withCurrent(mana.current()-amount));MagicServer.sync(p);return true;}
    public static boolean canHarm(Player p,LivingEntity other){return !immune(other)&&(!(other instanceof Player q)||p.getServer()!=null&&p.getServer().isPvpAllowed()&&p.canHarmPlayer(q));}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void chat(ServerChatEvent e){var p=e.getPlayer();boolean magicMessage=java.util.Arrays.stream(e.getRawText().strip().split("\\s+")).allMatch(KelLexicon.WORDS::containsKey);if(silent(p)||(!canCast(p)&&magicMessage)){e.setCanceled(true);p.displayClientMessage(net.minecraft.network.chat.Component.translatable("kelmagic.artifact.silenced"),true);return;}if(ArtifactWorld.capture(e))e.setCanceled(true);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void status(MobEffectEvent.Applicable e){if(immune(e.getEntity()))e.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);}
    @SubscribeEvent public static void shortened(MobEffectEvent.Added e){int count=pieces(e.getEntity(),"strict_negation");var effect=e.getEffectInstance();if(count>0&&count<4&&effect!=null&&!effect.isInfiniteDuration())((dev.kelmagic.mixin.ArtifactEffectAccess)(Object)effect).kelmagic$duration(Math.max(1,(int)(effect.getDuration()*(1-count*.15))));}
    @SubscribeEvent public static void equipment(LivingEquipmentChangeEvent e){int count=0;for(var slot:java.util.List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET)){var stack=slot==e.getSlot()?e.getTo():e.getEntity().getItemBySlot(slot);if(stack.getItem() instanceof ArtifactArmorItem a&&a.set.equals("strict_negation")&&a.getEquipmentSlot()==slot)count++;}if(count==4&&!e.getEntity().level().isClientSide){e.getEntity().removeAllEffects();ArcaneFire.extinguish(e.getEntity());e.getEntity().getPersistentData().remove("KelRootUntil");e.getEntity().getPersistentData().remove("KelMuteUntil");e.getEntity().getPersistentData().remove("KelHeavyUntil");}}
    @SubscribeEvent public static void command(net.neoforged.neoforge.event.CommandEvent e){var source=e.getParseResults().getContext().getSource();if(source.getEntity() instanceof ServerPlayer p&&silent(p)){String command=e.getParseResults().getReader().getString().strip();if(command.matches("(?s)^/?(?:say|msg|tell|w|me|teammsg|tm)(?:\\s.*)?$"))e.setCanceled(true);}}
    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p)||!p.isAlive())return;var s=state(p);long now=p.serverLevel().getGameTime();boolean quiet=silent(p);if(quiet&&!s.ownSilent){s.ownSilent=!p.isSilent();if(s.ownSilent)p.setSilent(true);}else if(!quiet&&s.ownSilent){p.setSilent(false);s.ownSilent=false;}
        if(immune(p)){p.removeAllEffects();ArcaneFire.extinguish(p);p.setData(MagicAttachments.MANA_BOOST_END,0L);p.setData(MagicAttachments.NEUTRAL_KIND,0);}
        int grove=pieces(p,"grove_keeper");if(grove>0&&grove(p)&&now>=s.combatUntil&&now%80==0)p.heal(grove*.15f);
        if(has(p,ArtifactContent.DEW.get())&&grove(p)&&!p.serverLevel().isDay()&&now>=s.dewAt){var stack=ArtifactAccessories.stack(p,ArtifactContent.DEW.get());ArtifactData.charge(stack,ArtifactData.charge(stack,1)+.05,1);s.dewAt=now+100;}
        if(has(p,ArtifactContent.ROOT.get())&&grove(p)&&now>=s.rootAt){ArtifactWorld.rootSense(p);s.rootAt=now+40;}
        if(p.isUsingItem()&&p.getUseItem().is(ArtifactContent.ORB.get()))ArtifactCombat.holdProjectiles(p);
        ArtifactWorld.tickSample(p);if(p.position().distanceToSqr(s.last)>.0001)s.movedUntil=now+2;s.last=p.position();
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void hurt(LivingIncomingDamageEvent e){var victim=e.getEntity();var source=e.getSource();if(source.getEntity() instanceof ServerPlayer attacker&&attacker!=victim)state(attacker).arrivalUntil=0;boolean debt=source.is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,ArtifactContent.id("mana_debt")));
        boolean magic=source.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO)||source.is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,ArtifactContent.id("spell")))||source.is(ArcaneFire.DAMAGE);
        if(!debt&&magic&&immune(victim)){e.setCanceled(true);return;}
        if(victim instanceof ServerPlayer p){var s=state(p);s.combatUntil=p.serverLevel().getGameTime()+160;if(p.isUsingItem()&&p.getUseItem().is(ArtifactContent.SHIELD.get())&&magic&&ArtifactCombat.parryDamage(p,source)){e.setCanceled(true);return;}int anchor=pieces(p,"steady_anchor");if(!debt&&source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)&&anchor>0)e.setAmount(e.getAmount()*(1-anchor*.12f));if(!debt&&anchor==4&&p.getPersistentData().getBoolean("KelAnchored"))e.setAmount(e.getAmount()*.75f);if(!debt&&pieces(p,"grove_keeper")>0&&grove(p))e.setAmount(e.getAmount()*(1-pieces(p,"grove_keeper")*.025f));}
        ArtifactCombat.shareDamage(e);
    }
    @SubscribeEvent public static void damaged(LivingDamageEvent.Post e){if(e.getNewDamage()<=0)return;if(e.getEntity() instanceof ServerPlayer p){if(pieces(p,"grove_keeper")==4){long now=p.serverLevel().getGameTime();if(e.getSource().getEntity() instanceof LivingEntity attacker&&canHarm(p,attacker)&&now>=p.getPersistentData().getLong("KelGroveRetaliate")){p.getPersistentData().putLong("KelGroveRetaliate",now+200);ArtifactCombat.root(attacker,p,60);}if(p.getHealth()<=p.getMaxHealth()*.4&&now>=p.getPersistentData().getLong("KelGroveGuard")){p.getPersistentData().putLong("KelGroveGuard",now+300);ArtifactCombat.rootBarrier(p);}}}}
    @SubscribeEvent public static void attack(AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p){state(p).arrivalUntil=0;state(p).combatUntil=p.serverLevel().getGameTime()+160;recordSilentAction(p,e.getTarget().position());}}
    @SubscribeEvent public static void changeTarget(LivingChangeTargetEvent e){if(e.getNewAboutToBeSetTarget() instanceof ServerPlayer p&&((has(p,ArtifactContent.ARRIVAL.get())&&state(p).arrivalUntil>p.serverLevel().getGameTime())||(e.getEntity() instanceof net.minecraft.world.entity.monster.Phantom&&ArtifactWorld.nightGuard(p))))e.setNewAboutToBeSetTarget(null);}
    public static void arrived(ServerPlayer p){if(has(p,ArtifactContent.ARRIVAL.get())){state(p).arrivalUntil=p.serverLevel().getGameTime()+100;for(var mob:p.serverLevel().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(24)))if(mob.getTarget()==p)mob.setTarget(null);ArtifactVisuals.ring(p.serverLevel(),p.position().add(0,.15,0),.8,0xb08ae3,20);}}
    @SubscribeEvent public static void interact(PlayerInteractEvent.RightClickBlock e){if(e.getEntity() instanceof ServerPlayer p){recordSilentAction(p,Vec3.atCenterOf(e.getPos()));if(!ArtifactWorld.allowed(p,e.getPos())&&p.serverLevel().getBlockState(e.getPos()).getBlock().builtInRegistryHolder().key().location().getNamespace().equals("kelmagic")){e.setCanceled(true);p.displayClientMessage(net.minecraft.network.chat.Component.translatable("kelmagic.artifact.denied"),true);}}}
    @SubscribeEvent public static void left(PlayerInteractEvent.LeftClickBlock e){if(e.getEntity() instanceof ServerPlayer p)recordSilentAction(p,Vec3.atCenterOf(e.getPos()));}
    public static void recordSilentAction(ServerPlayer p,Vec3 pos){if(silent(p)){state(p).silentActionAt=pos;state(p).silentActionUntil=p.serverLevel().getGameTime()+8;}}
    @SubscribeEvent public static void sound(PlayLevelSoundEvent.AtEntity e){if(silent(e.getEntity()))e.setCanceled(true);}
    @SubscribeEvent public static void soundAt(PlayLevelSoundEvent.AtPosition e){if(e.getLevel() instanceof ServerLevel l)for(var p:l.players())if(silent(p)){var s=state(p);if(e.getPosition().distanceToSqr(p.position())<1.5||l.getGameTime()<=s.silentActionUntil&&e.getPosition().distanceToSqr(s.silentActionAt)<3){e.setCanceled(true);return;}}}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)cleanup(p);}
    @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)cleanup(p);}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p){ArtifactWorld.releaseSample(p);state(p).paid=0;state(p).regen=0;}}
    private static void cleanup(ServerPlayer p){ArtifactWorld.releaseSample(p);var s=STATES.remove(p);if(s!=null&&s.ownSilent)p.setSilent(false);}
    @SubscribeEvent public static void stop(ServerStoppedEvent e){STATES.clear();ArtifactWorld.clear();ArtifactMagic.clear();}
    private ArtifactRuntime(){}
}
