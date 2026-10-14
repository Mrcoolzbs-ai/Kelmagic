package dev.kelmagic.server;

import dev.kelmagic.KelMagic;
import dev.kelmagic.core.*;
import dev.kelmagic.network.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.tick.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid=KelMagic.MODID)
public final class MagicServer {
    private static final ResourceKey<DamageType> MANA_DEBT = ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"mana_debt"));
    public static double maximum(ServerPlayer p) { return (p.getData(MagicAttachments.BASE_MANA_MAX)>=0?p.getData(MagicAttachments.BASE_MANA_MAX):ManaMath.maximum(p.getData(MagicAttachments.MANA).dragonUpgrade(),equipmentBonus(p)))+(p.serverLevel().getGameTime()<p.getData(MagicAttachments.MANA_BOOST_END)?50:0); }
    public static double equipmentBonus(ServerPlayer p) {
        double bonus=0;
        for(var slot:java.util.List.of(net.minecraft.world.entity.EquipmentSlot.HEAD,net.minecraft.world.entity.EquipmentSlot.CHEST,net.minecraft.world.entity.EquipmentSlot.LEGS,net.minecraft.world.entity.EquipmentSlot.FEET)) {
            var stack=p.getItemBySlot(slot);
            if(stack.getItem() instanceof dev.kelmagic.item.ManaArmorItem item && item.getEquipmentSlot()==slot) bonus+=item.manaBonus;
        }
        return bonus;
    }
    public static void sync(ServerPlayer p) { PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,snapshot(p,SpellSnapshot.SYNC)); }
    public static SpellSnapshot snapshot(ServerPlayer p,int kind) {
        SpellSession s=p.getData(MagicAttachments.SESSION);
        return new SpellSnapshot(p.getUUID(),s.generation(),s.revision(),Math.min(maximum(p),p.getData(MagicAttachments.MANA).current()),maximum(p),s.words(),kind);
    }
    private static void publish(ServerPlayer p,int kind) { PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,snapshot(p,kind)); }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void chat(ServerChatEvent event) {
        if(event.isCanceled()) return;
        ServerPlayer p=event.getPlayer();
        var word=KelLexicon.recognize(event.getRawText());
        if(word.isEmpty()) return; // Do not cancel/replace vanilla public chat.
        if(!p.isAlive() || p.isSpectator()) { PacketDistributor.sendToPlayer(p,snapshot(p,SpellSnapshot.END)); return; }
        p.serverLevel().playSound(null,p.blockPosition(),dev.kelmagic.MagicSounds.WORD.value(),net.minecraft.sounds.SoundSource.PLAYERS,0.65f,0.92f+p.getRandom().nextFloat()*0.16f);
        SpellSession s=p.getData(MagicAttachments.SESSION);
        if(!s.append(word.get().word())) {
            MagicProgress.award(p,"overload_101");p.displayClientMessage(Component.translatable("kelmagic.overload"),false);
            clear(p); OverloadEffects.trigger(p); return;
        }
        var mana=p.getData(MagicAttachments.MANA);
        var spend=ManaMath.spend(Math.min(mana.current(),maximum(p)),word.get().word().length());
        p.setData(MagicAttachments.MANA,mana.withCurrent(spend.remaining()));
        hurtMana(p,spend.healthDamage());
        if(p.isAlive()){MagicProgress.award(p,"first_word");publish(p,SpellSnapshot.ACCEPT);}
    }
    public static void hurtMana(ServerPlayer p,float amount) {
        if(amount<=0 || p.isCreative() || p.isSpectator() || p.getAbilities().invulnerable) return;
        var velocity=p.getDeltaMovement();
        p.hurt(MagicDamage.source(p,"mana_debt"),amount);
        // Keep pre-existing movement, but never add a mana-debt impulse.
        p.setDeltaMovement(velocity);
    }
    public static void action(ServerPlayer p,SpellAction action) { finish(p,action,true); }
    public static void actionFromScripter(ServerPlayer p,SpellAction action) { finish(p,action,false); }
    private static void finish(ServerPlayer p,SpellAction action,boolean fromChat) {
        SpellSession s=p.getData(MagicAttachments.SESSION);
        if(!p.isAlive() || p.isSpectator()) return;
        if(!s.authorize(action.generation(),action.revision())) {
            if(s.allowResync(p.serverLevel().getGameTime())) PacketDistributor.sendToPlayer(p,snapshot(p,SpellSnapshot.SYNC));
            return;
        }
        var words=java.util.List.copyOf(s.words());
        var plan=SpellParser.plan(words);var effect=plan.effect();
        if(fromChat && !action.cancel()) dev.kelmagic.item.ScripterItem.capture(p,words);
        clear(p); // Invalidate before effects. A duplicate packet cannot execute again.
        if(action.cancel()) { p.displayClientMessage(Component.translatable("kelmagic.cancelled"),true); return; }
        if(effect==SpellParser.Effect.UNDEFINED || effect==SpellParser.Effect.SELECTION_ONLY) {
            SpellEffects.fizzle(p,effect==SpellParser.Effect.SELECTION_ONLY?"kelmagic.incomplete":"kelmagic.undefined");return;
        }
        if(!SpellEffects.execute(p,plan)) SpellEffects.fizzle(p,"kelmagic.no_target");else{MagicProgress.award(p,"first_spell");if(words.size()>=2)MagicProgress.award(p,"combination");if(plan.effect()==SpellParser.Effect.FLAME_LIFE)MagicProgress.award(p,"flame_life");}
    }
    private static void clear(ServerPlayer p) {
        p.getData(MagicAttachments.SESSION).clear(); publish(p,SpellSnapshot.END);
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer p) || !p.isAlive()) return;
        if(p.isSpectator()) { if(p.getData(MagicAttachments.SESSION).active()) clear(p); return; }
        var mana=p.getData(MagicAttachments.MANA);double max=maximum(p);
        if(mana.current()>max) { mana=mana.withCurrent(max);p.setData(MagicAttachments.MANA,mana); }
        if(p.getData(MagicAttachments.SESSION).regenDue(ManaMath.interval(mana.dragonUpgrade(),p.hasEffect(dev.kelmagic.MagicContent.FLOW))) && mana.current()<max) {
            p.setData(MagicAttachments.MANA,mana.withCurrent(ManaMath.regenerate(mana.current(),max)));
            PacketDistributor.sendToPlayer(p,snapshot(p,SpellSnapshot.SYNC));
        }
        if(p.getData(MagicAttachments.SESSION).needsManaSync(p.getData(MagicAttachments.MANA).current(),max)) sync(p);
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if(e.getEntity() instanceof ServerPlayer p) { p.getData(MagicAttachments.SESSION).clear();PacketDistributor.sendToPlayer(p,snapshot(p,SpellSnapshot.SYNC)); }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e) { if(e.getEntity() instanceof ServerPlayer p) clear(p); }
    @SubscribeEvent public static void death(LivingDeathEvent e) { if(e.getEntity() instanceof ServerPlayer p) clear(p); }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e) { if(e.getEntity() instanceof ServerPlayer p) clear(p); }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if(e.getEntity() instanceof ServerPlayer p) {
            p.getData(MagicAttachments.SESSION).clear();
            // End-return is not death: preserve the saved amount in that case.
            if(!e.isEndConquered()) p.setData(MagicAttachments.MANA,p.getData(MagicAttachments.MANA).withCurrent(maximum(p)));
            PacketDistributor.sendToPlayer(p,snapshot(p,SpellSnapshot.END));
        }
    }
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking e) {
        if(e.getEntity() instanceof ServerPlayer observer && e.getTarget() instanceof ServerPlayer target)
            PacketDistributor.sendToPlayer(observer,snapshot(target,SpellSnapshot.SYNC));
    }
    @SubscribeEvent public static void serverTick(ServerTickEvent.Post e) { OverloadEffects.tick(); }
    @SubscribeEvent public static void stop(ServerStoppedEvent e) { OverloadEffects.clear(); }
    private MagicServer() {}
}
