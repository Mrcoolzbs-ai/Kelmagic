package dev.kelmagic.artifact;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
public final class ArtifactSeasons extends SavedData {
    private record Entry(UUID owner,String block){}
    private final Map<BlockPos,Entry> locks=new HashMap<>();
    public static ArtifactSeasons get(ServerLevel l){return l.getDataStorage().computeIfAbsent(new Factory<>(ArtifactSeasons::new,ArtifactSeasons::load),"kelmagic_stopped_seasons");}
    public boolean toggle(BlockPos p,UUID owner,BlockState state){var e=locks.get(p);if(e!=null){if(!e.owner.equals(owner))return false;locks.remove(p);}else{if(locks.size()>=4096||locks.values().stream().filter(x->x.owner.equals(owner)).count()>=128)return false;locks.put(p.immutable(),new Entry(owner,BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString()));}setDirty();return true;}
    public static boolean locked(ServerLevel l,BlockPos p){var data=get(l);var e=data.locks.get(p);if(e==null)return false;if(!e.block.equals(BuiltInRegistries.BLOCK.getKey(l.getBlockState(p).getBlock()).toString())){data.locks.remove(p);data.setDirty();return false;}return true;}
    public static ArtifactSeasons load(CompoundTag t,HolderLookup.Provider p){var data=new ArtifactSeasons();for(var element:t.getList("Locks",Tag.TAG_COMPOUND)){if(data.locks.size()>=4096)break;var n=(CompoundTag)element;if(n.hasUUID("Owner"))data.locks.put(BlockPos.of(n.getLong("Pos")),new Entry(n.getUUID("Owner"),n.getString("Block")));}return data;}
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider p){var list=new ListTag();locks.forEach((pos,e)->{var n=new CompoundTag();n.putLong("Pos",pos.asLong());n.putUUID("Owner",e.owner);n.putString("Block",e.block);list.add(n);});tag.put("Locks",list);return tag;}
}
