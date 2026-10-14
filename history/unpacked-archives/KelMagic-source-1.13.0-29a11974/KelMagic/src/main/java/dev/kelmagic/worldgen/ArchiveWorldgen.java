package dev.kelmagic.worldgen;
import dev.kelmagic.KelMagic;import net.minecraft.core.registries.Registries;import net.minecraft.resources.*;import net.minecraft.world.level.levelgen.structure.*;import net.neoforged.neoforge.registries.*;
public final class ArchiveWorldgen {
 public static final ResourceKey<Structure> ARCHIVE=ResourceKey.create(Registries.STRUCTURE,ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"archive"));
 public static final DeferredRegister<StructureType<?>> TYPES=DeferredRegister.create(Registries.STRUCTURE_TYPE,KelMagic.MODID);
 public static final DeferredHolder<StructureType<?>,StructureType<ArchiveStructure>> TYPE=TYPES.register("archive",()->()->ArchiveStructure.CODEC);
 private ArchiveWorldgen(){}
}
