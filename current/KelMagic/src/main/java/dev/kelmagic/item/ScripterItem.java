package dev.kelmagic.item;
import java.util.*;
import dev.kelmagic.*;
import dev.kelmagic.core.*;
import dev.kelmagic.server.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
public final class ScripterItem extends Item {
    public ScripterItem(Properties props){super(props);}
    public static int cost(List<String> words){return (words.stream().mapToInt(String::length).sum()+1)/2;}
    public static boolean valid(List<String> words) {
        if(words.isEmpty() || words.size()>100 || words.stream().anyMatch(w->!KelLexicon.WORDS.containsKey(w))) return false;
        var e=SpellParser.parse(words);return e!=SpellParser.Effect.UNDEFINED && e!=SpellParser.Effect.SELECTION_ONLY;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if(player instanceof ServerPlayer p) {
            if(p.isSpectator() || !dev.kelmagic.artifact.ArtifactRuntime.canCast(p) || p.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
            var s=p.getData(MagicAttachments.SESSION);
            if(!p.isShiftKeyDown()) {
                if(!stack.getOrDefault(MagicComponents.RECORDING.get(),false)) {
                    if(s.active()) {p.displayClientMessage(Component.translatable("kelmagic.scripter_finish_first"),true);return InteractionResultHolder.fail(stack);}
                    stack.set(MagicComponents.RECORDING.get(),true);
                    stack.remove(MagicComponents.CANDIDATE.get());
                    p.displayClientMessage(Component.translatable("kelmagic.scripter_armed"),false);
                } else {
                    var candidate=stack.getOrDefault(MagicComponents.CANDIDATE.get(),List.<String>of());
                    if(valid(candidate)) {
                        stack.set(MagicComponents.SPELL.get(),List.copyOf(candidate));if(SpellParser.parse(candidate)!=SpellParser.Effect.GRAND){dev.kelmagic.research.Research.observe(p,"record_spell");}
                        p.displayClientMessage(Component.translatable("kelmagic.scripter_saved",candidate.size(),cost(candidate)),false);
                    } else p.displayClientMessage(Component.translatable("kelmagic.scripter_record_cancelled"),false);
                    stack.remove(MagicComponents.RECORDING.get());stack.remove(MagicComponents.CANDIDATE.get());
                }
            } else {
                var words=stack.getOrDefault(MagicComponents.SPELL.get(),List.<String>of());
                if(stack.getOrDefault(MagicComponents.RECORDING.get(),false) || s.active() || !valid(words)) {p.displayClientMessage(Component.translatable("kelmagic.scripter_blocked"),true);return InteractionResultHolder.fail(stack);}
                // No arbitrary client spell payload: read the authoritative item component and revalidate it.
                var mana=p.getData(MagicAttachments.MANA);if(dev.kelmagic.artifact.ArtifactRuntime.has(p,dev.kelmagic.artifact.ArtifactContent.CAUTIOUS.get())&&mana.current()<cost(words))return InteractionResultHolder.fail(stack);dev.kelmagic.artifact.ArtifactRuntime.state(p).paid=cost(words);var spend=ManaMath.spend(Math.min(MagicServer.maximum(p),mana.current()),cost(words));
                p.setData(MagicAttachments.MANA,mana.withCurrent(spend.remaining()));Playback.run(p,"scripter",()->{MagicServer.hurtMana(p,spend.healthDamage());
                if(p.isAlive()) {
                    words.forEach(s::append);MagicServer.sync(p);
                    MagicServer.actionFromScripter(p,new dev.kelmagic.network.SpellAction(s.generation(),s.revision(),false));
                    p.getCooldowns().addCooldown(this,20);
                }});
                MagicServer.sync(p);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }
    /** Capture only this player's next completed local-chat spell; never item playback. */
    public static void capture(ServerPlayer p,List<String> words) {
        if(!valid(words)) return;
        for(var hand:InteractionHand.values()) {
            var stack=p.getItemInHand(hand);
            if(stack.getItem() instanceof ScripterItem && stack.getOrDefault(MagicComponents.RECORDING.get(),false)
                && stack.getOrDefault(MagicComponents.CANDIDATE.get(),List.<String>of()).isEmpty()) {
                stack.set(MagicComponents.CANDIDATE.get(),List.copyOf(words));
                p.displayClientMessage(Component.translatable("kelmagic.scripter_captured",words.size()),false);
            }
        }
    }
    @Override public void appendHoverText(ItemStack stack,Item.TooltipContext ctx,List<Component> tips,TooltipFlag flag) {
        var words=stack.getOrDefault(MagicComponents.SPELL.get(),List.<String>of());
        tips.add(Component.translatable("kelmagic.scripter_controls"));
        if(stack.getOrDefault(MagicComponents.RECORDING.get(),false)) {
            var candidate=stack.getOrDefault(MagicComponents.CANDIDATE.get(),List.<String>of());
            tips.add(Component.translatable(candidate.isEmpty()?"kelmagic.scripter_waiting":"kelmagic.scripter_ready"));
            if(!candidate.isEmpty()) tips.add(Component.literal(String.join(" ",candidate)));
        }
        if(!words.isEmpty()) {tips.add(Component.literal(String.join(" ",words)));tips.add(Component.translatable("kelmagic.scripter_cost",cost(words)));}
    }
}
