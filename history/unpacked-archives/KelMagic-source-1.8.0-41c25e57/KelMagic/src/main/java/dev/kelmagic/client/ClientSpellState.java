package dev.kelmagic.client;

import dev.kelmagic.KelMagic;
import dev.kelmagic.core.*;
import dev.kelmagic.network.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class ClientSpellState {
    public static final class Visual {
        public final List<String> words; public final long created;
        public long fading=-1;
        Visual(List<String> words) { this.words=List.copyOf(words);created=ticks; }
    }
    public static final Map<UUID,Visual> VISUALS = new HashMap<>();
    public static SpellSnapshot own;
    public static long ticks;
    private static int pending, pendingAge, actionAttempts;
    private static boolean reopen, chatSuspended;
    private static Boolean queuedAction;
    private static long lastActionToken;
    private static int lastActionRevision=-1;
    public static boolean preparing() { return own!=null && !own.words().isEmpty(); }
    public static void accept(SpellSnapshot s) {
        var mc=Minecraft.getInstance();if(mc.player==null) return;
        if(s.words().stream().anyMatch(w -> !KelLexicon.WORDS.containsKey(w))) return;
        if(s.kind()==SpellSnapshot.END) {
            var visual=VISUALS.get(s.player());if(visual!=null) visual.fading=ticks;
        } else if(!s.words().isEmpty()) {
            var visual=VISUALS.get(s.player());
            if(visual==null || !visual.words.equals(s.words())) VISUALS.put(s.player(),new Visual(s.words()));
        }
        if(!s.player().equals(mc.player.getUUID())) return;
        own=s;
        if(s.kind()==SpellSnapshot.ACCEPT) {
            pending=Math.max(0,pending-1);pendingAge=0;
            if(queuedAction==null && !chatSuspended) reopen=true;
        }
        if(s.kind()==SpellSnapshot.END) { pending=0;queuedAction=null;actionAttempts=0;reopen=false; }
        flushAction();
    }
    private static void flushAction() {
        if(queuedAction==null || pending>0 || own==null || own.generation()==0 || own.words().isEmpty()) return;
        if(actionAttempts>=3 || own.generation()==lastActionToken && own.revision()==lastActionRevision) return;
        lastActionToken=own.generation();lastActionRevision=own.revision();actionAttempts++;
        PacketDistributor.sendToServer(new SpellAction(own.generation(),own.revision(),queuedAction));
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void chat(ClientChatEvent e) {
        if(e.isCanceled() || KelLexicon.recognize(e.getMessage()).isEmpty()) return;
        pending++;pendingAge=0;
    }
    @SubscribeEvent public static void key(ScreenEvent.KeyPressed.Pre e) {
        if(!(e.getScreen() instanceof ChatScreen screen) || e.isCanceled()) return;
        int key=e.getKeyCode();
        String current=screen.children().stream().filter(w -> w instanceof EditBox).map(w -> ((EditBox)w).getValue()).findFirst().orElse("");
        // An empty preparation chat must not trap the player: inventory suspends, not cancels, casting.
        var mc=Minecraft.getInstance();
        if(current.isEmpty() && mc.options.keyInventory.matches(key,e.getScanCode()) && mc.player!=null) {
            e.setCanceled(true);chatSuspended=true;reopen=false;
            mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));return;
        }
        if(key!=GLFW.GLFW_KEY_ESCAPE && key!=GLFW.GLFW_KEY_ENTER && key!=GLFW.GLFW_KEY_KP_ENTER) return;
        String input=screen.children().stream().filter(w -> w instanceof EditBox).map(w -> ((EditBox)w).getValue()).findFirst().orElse("");
        if(key!=GLFW.GLFW_KEY_ESCAPE && !input.isBlank()) return;
        if(!preparing() && pending==0) return;
        e.setCanceled(true);
        queuedAction=key==GLFW.GLFW_KEY_ESCAPE;
        actionAttempts=0;lastActionRevision=-1;reopen=false;
        Minecraft.getInstance().setScreen(null);
        flushAction();
    }
    @SubscribeEvent public static void screen(ScreenEvent.Opening e) {
        if(e.getNewScreen() instanceof ChatScreen) chatSuspended=false;
        else if(e.getNewScreen()!=null) { chatSuspended=true;reopen=false; }
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        ticks++; var mc=Minecraft.getInstance();
        if(mc.level==null || mc.player==null) { reset();return; }
        VISUALS.entrySet().removeIf(entry -> entry.getValue().fading>=0 && ticks-entry.getValue().fading>24 || mc.level.getPlayerByUUID(entry.getKey())==null);
        if(pending>0 && ++pendingAge>=400) {
            pending=0;pendingAge=0;queuedAction=preparing()?true:null;reopen=false;actionAttempts=0;lastActionRevision=-1;
            mc.player.displayClientMessage(Component.translatable("kelmagic.chat_timeout"),false);flushAction();
        }
        if(reopen) {
            if(!mc.player.isAlive()) reopen=false;
            else if(mc.screen==null && !chatSuspended) { mc.setScreen(new ChatScreen(""));reopen=false; }
            else if(mc.screen instanceof ChatScreen) reopen=false;
        }
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e) { reset(); }
    private static void reset() { VISUALS.clear();own=null;pending=0;pendingAge=0;queuedAction=null;reopen=false;chatSuspended=false;actionAttempts=0;lastActionRevision=-1; }
    private ClientSpellState() {}
}
