package dev.kelmagic.client;
import net.minecraft.world.item.ItemStack;import net.minecraft.core.component.DataComponents;import net.minecraft.resources.ResourceLocation;import net.minecraft.world.entity.EquipmentSlot;
public final class TrimAppearance {
 public static final java.util.List<String> PATTERNS=java.util.List.of("sentry","dune","coast","wild","ward","eye","vex","tide","snout","rib","spire","wayfinder","shaper","silence","raiser","host","flow","bolt");
 public static final java.util.List<String> MATERIALS=java.util.List.of("quartz","iron","netherite","redstone","copper","gold","emerald","diamond","lapis","amethyst");
 public static int materialIndex(ItemStack s){var t=s.get(DataComponents.TRIM);return t==null?0:MATERIALS.indexOf(t.material().value().assetName())+1;}
 public static int index(ItemStack s){var t=s.get(DataComponents.TRIM);if(t==null)return 0;int p=PATTERNS.indexOf(t.pattern().value().assetId().getPath()),m=MATERIALS.indexOf(t.material().value().assetName());return p<0||m<0?0:1+p*10+m;}
 private static final ResourceLocation[][] TEXTURES=new ResourceLocation[8][181];
 public static ResourceLocation texture(ItemStack s,boolean robe,EquipmentSlot slot){int id=index(s);if(id==0)return null;int row=(robe?4:0)+switch(slot){case HEAD->0;case CHEST->1;case LEGS->2;case FEET->3;default->throw new IllegalArgumentException("Not armor");};var texture=TEXTURES[row][id];if(texture==null){String pattern=PATTERNS.get((id-1)/10),material=MATERIALS.get((id-1)%10);TEXTURES[row][id]=texture=ResourceLocation.fromNamespaceAndPath("kelmagic","textures/trims/"+(robe?"mage":"arcane")+"_"+slot.getName()+"/"+pattern+"_"+material+".png");}return texture;}

 public static int color(ItemStack s){return 0xFFFFFFFF;}
 private TrimAppearance(){}
}
