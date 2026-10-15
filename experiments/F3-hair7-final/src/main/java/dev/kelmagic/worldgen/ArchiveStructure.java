package dev.kelmagic.worldgen;
import com.mojang.serialization.MapCodec;import net.minecraft.world.level.levelgen.structure.*;import java.util.Optional;
/** Searchable subset of existing village pieces; never generates a duplicate building. */
public final class ArchiveStructure extends Structure {
 public static final MapCodec<ArchiveStructure> CODEC=Structure.simpleCodec(ArchiveStructure::new);
 public ArchiveStructure(StructureSettings settings){super(settings);}
 @Override protected Optional<GenerationStub> findGenerationPoint(GenerationContext context){return Optional.empty();}
 @Override public StructureType<?> type(){return ArchiveWorldgen.TYPE.get();}
}
