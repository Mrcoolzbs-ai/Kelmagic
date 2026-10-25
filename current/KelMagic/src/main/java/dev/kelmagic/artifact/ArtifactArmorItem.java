package dev.kelmagic.artifact;
import dev.kelmagic.item.ManaArmorItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
import java.util.List;
public final class ArtifactArmorItem extends ManaArmorItem {
    public final String set;
    public ArtifactArmorItem(String set,Holder<ArmorMaterial> mat,Type type,Properties props,double bonus,boolean robe){super(mat,type,props,bonus,robe);this.set=set;}
    @Override public void appendHoverText(ItemStack s,Item.TooltipContext c,List<Component> lines,TooltipFlag f){super.appendHoverText(s,c,lines,f);lines.add(Component.translatable("kelmagic.artifact.set."+set));}
}
