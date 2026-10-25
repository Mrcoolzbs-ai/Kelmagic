package dev.kelmagic.artifact;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
public final class ArtifactData {
    public static CompoundTag get(ItemStack s) { return s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(); }
    public static void edit(ItemStack s, Consumer<CompoundTag> edit) { var tag=get(s);edit.accept(tag);s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag)); }
    public static double charge(ItemStack s,double max) { return ArtifactRules.finite(get(s).getDouble("KelCharge"),max); }
    public static void charge(ItemStack s,double value,double max) { edit(s,t->t.putDouble("KelCharge",ArtifactRules.finite(value,max))); }
    private ArtifactData() {}
}
