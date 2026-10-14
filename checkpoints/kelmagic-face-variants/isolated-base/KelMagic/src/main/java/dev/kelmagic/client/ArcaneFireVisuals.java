package dev.kelmagic.client;
import java.util.*;
import dev.kelmagic.network.BurnSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
/** Server-authoritative burn visuals, isolated per client world and with bounded TTL. */
public final class ArcaneFireVisuals {
 private record Burn(ClientLevel level,long until){}
 private static final Map<Integer,Burn> burns=new HashMap<>();
 public static void accept(BurnSnapshot p){var l=Minecraft.getInstance().level;if(l==null)return;burns.entrySet().removeIf(e->e.getValue().level!=l || e.getValue().until<=l.getGameTime());if(p.ticks()==0)burns.remove(p.entityId());else burns.put(p.entityId(),new Burn(l,l.getGameTime()+p.ticks()));}
 public static boolean active(Entity e){if(e==null)return false;var b=burns.get(e.getId());return b!=null && b.level==e.level() && b.until>e.level().getGameTime() && e.isAlive() && !e.isSpectator();}
 public static TextureAtlasSprite sprite(int i){return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ResourceLocation.fromNamespaceAndPath("kelmagic","block/magic_fire_"+i));}
 private ArcaneFireVisuals(){}
}
