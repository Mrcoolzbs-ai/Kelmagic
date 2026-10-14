package dev.kelmagic.client;
import dev.kelmagic.research.*;import net.minecraft.client.*;import net.minecraft.client.gui.*;import net.minecraft.client.gui.components.toasts.*;import net.minecraft.world.item.*;
public final class DiscoveryToast implements Toast {
 private final String id;private final String label;private final ItemStack icon;
 public DiscoveryToast(ResearchCatalog.Node n){id=n.id();label=Research.name(n);icon=CodexPaint.icon(n.icon());}
 public Object getToken(){return id;}
 public Toast.Visibility render(GuiGraphics g,ToastComponent c,long time){g.fill(0,0,160,32,0xFF203537);g.renderOutline(0,0,160,32,0xFFD5B578);g.renderItem(icon,7,8);var f=Minecraft.getInstance().font;g.drawString(f,"Открытие совершено",29,6,0xE2C891,false);g.drawString(f,f.plainSubstrByWidth(label,125),29,18,0xBBD9D5,false);return time<5000?Toast.Visibility.SHOW:Toast.Visibility.HIDE;}
}
