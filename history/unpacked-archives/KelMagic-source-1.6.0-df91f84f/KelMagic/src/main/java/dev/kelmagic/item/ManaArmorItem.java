package dev.kelmagic.item;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
public final class ManaArmorItem extends ArmorItem {
    public final int manaBonus;public final boolean robe;
    public ManaArmorItem(Holder<ArmorMaterial> material,Type type,Properties props,int bonus,boolean robe) {
        super(material,type,props);this.manaBonus=bonus;this.robe=robe;
    }
    @Override public void appendHoverText(ItemStack stack,Item.TooltipContext ctx,List<Component> tips,TooltipFlag flag) {
        super.appendHoverText(stack,ctx,tips,flag);tips.add(Component.translatable("kelmagic.equipment_bonus",manaBonus));
    }
}
