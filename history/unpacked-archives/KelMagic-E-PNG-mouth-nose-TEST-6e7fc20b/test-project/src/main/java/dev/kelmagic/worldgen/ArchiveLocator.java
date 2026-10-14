package dev.kelmagic.worldgen;
import com.mojang.datafixers.util.Pair;import java.util.*;import net.minecraft.core.*;import net.minecraft.core.registries.Registries;import net.minecraft.resources.ResourceLocation;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.level.ChunkPos;import net.minecraft.world.level.chunk.status.ChunkStatus;import net.minecraft.world.level.levelgen.structure.*;import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;import net.minecraft.world.level.levelgen.structure.placement.*;
/** Uses vanilla village candidate placements and actual generated starts, including unexplored chunks.
 * No scanning every block, no permanent tick hook, no duplicate archive generation. */
public final class ArchiveLocator {
 public static boolean isArchive(Holder<Structure> holder){return holder.is(ArchiveWorldgen.ARCHIVE);}
 public static List<BlockPos> positions(StructureStart start){var out=new ArrayList<BlockPos>();if(!start.isValid())return out;for(var piece:start.getPieces())if(piece instanceof PoolElementStructurePiece p&&p.getElement() instanceof SinglePoolElement element){var id=((dev.kelmagic.mixin.SinglePoolElementAccessor)(Object)element).kelmagic$template().left();if(id.isPresent()&&id.get().getNamespace().equals("kelmagic")&&id.get().getPath().startsWith("archive/"))out.add(p.getBoundingBox().getCenter());}return out;}
 public static Pair<BlockPos,Holder<Structure>> locate(ServerLevel level,BlockPos origin,int radius,boolean skipReferenced){
  // A structures-disabled world can still contain saved starts; inspect those without generation.
  boolean generate=level.getServer().getWorldData().worldGenOptions().generateStructures();
  var registry=level.registryAccess().registryOrThrow(Registries.STRUCTURE);var archive=registry.getHolder(ArchiveWorldgen.ARCHIVE);if(archive.isEmpty())return null;
  var state=level.getChunkSource().getGeneratorState();var placements=placements(level);
  var seen=new HashSet<Long>();var center=new ChunkPos(origin);
  for(int ring=0;ring<=radius;ring++){BlockPos best=null;double distance=Double.MAX_VALUE;
   for(var entry:placements.entrySet()){var placement=entry.getKey();int spacing=placement.spacing();
    for(int dx=-ring;dx<=ring;dx++)for(int dz=-ring;dz<=ring;dz++){if(Math.abs(dx)!=ring&&Math.abs(dz)!=ring)continue;var candidate=placement.getPotentialStructureChunk(state.getLevelSeed(),center.x+dx*spacing,center.z+dz*spacing);if(!seen.add(candidate.toLong())||!placement.isStructureChunk(state,candidate.x,candidate.z))continue;
     // Vanilla locate also requests STRUCTURE_STARTS, not complete terrain/features.
     var chunk=generate?level.getChunk(candidate.x,candidate.z,ChunkStatus.STRUCTURE_STARTS):level.getChunkSource().getChunk(candidate.x,candidate.z,ChunkStatus.STRUCTURE_STARTS,false);if(chunk==null)continue;for(var structure:entry.getValue()){var start=chunk.getStartForStructure(structure);if(start==null||!start.isValid()||skipReferenced&&!start.canBeReferenced())continue;for(var at:positions(start)){double d=horizontalDistance(origin,at);if(d<distance){best=at;distance=d;}}}
    }
   }
   if(best!=null)return Pair.of(best,archive.get());
  }
  return null;
 }
 public static Map<RandomSpreadStructurePlacement,Set<Structure>> placements(ServerLevel level){
  var registry=level.registryAccess().registryOrThrow(Registries.STRUCTURE);var state=level.getChunkSource().getGeneratorState();var placements=new LinkedHashMap<RandomSpreadStructurePlacement,Set<Structure>>();
  for(String biome:List.of("plains","desert","taiga","snowy","savanna")){var village=registry.getHolder(net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE,ResourceLocation.withDefaultNamespace("village_"+biome)));if(village.isEmpty())continue;for(var placement:state.getPlacementsForStructure(village.get()))if(placement instanceof RandomSpreadStructurePlacement spread)placements.computeIfAbsent(spread,k->new HashSet<>()).add(village.get().value());}
return placements;
 }
 private static double horizontalDistance(BlockPos a,BlockPos b){double x=(double)a.getX()-b.getX(),z=(double)a.getZ()-b.getZ();return x*x+z*z;}
 private ArchiveLocator(){}
}
