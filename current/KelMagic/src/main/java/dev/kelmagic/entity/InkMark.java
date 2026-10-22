package dev.kelmagic.entity;
import dev.kelmagic.content.InkPatterns;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import java.util.UUID;
/** A non-colliding face decal. Saved with its chunk; no block replacement or block-entity hijacking. */
public final class InkMark extends Entity {
 private static final EntityDataAccessor<BlockPos> ANCHOR=SynchedEntityData.defineId(InkMark.class,EntityDataSerializers.BLOCK_POS);
 private static final EntityDataAccessor<Integer> FACE=SynchedEntityData.defineId(InkMark.class,EntityDataSerializers.INT),COLOR=SynchedEntityData.defineId(InkMark.class,EntityDataSerializers.INT),ROTATION=SynchedEntityData.defineId(InkMark.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<String> PATTERN=SynchedEntityData.defineId(InkMark.class,EntityDataSerializers.STRING);
 private UUID owner;private net.minecraft.world.level.block.Block support;
 public InkMark(EntityType<? extends InkMark> t,Level l){super(t,l);noPhysics=true;}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(ANCHOR,BlockPos.ZERO);b.define(FACE,2);b.define(COLOR,0x169c9c);b.define(ROTATION,0);b.define(PATTERN,"u2192");}
 public void configure(BlockPos pos,Direction face,String pattern,int color,UUID owner,int rotation){entityData.set(ANCHOR,pos.immutable());entityData.set(FACE,face.ordinal());entityData.set(PATTERN,InkPatterns.get(pattern).id());entityData.set(COLOR,color&0xFFFFFF);entityData.set(ROTATION,Math.floorMod(rotation,4));this.owner=owner;support=level().getBlockState(pos).getBlock();setPos(pos.getX()+.5+face.getStepX()*.502,pos.getY()+.5+face.getStepY()*.502,pos.getZ()+.5+face.getStepZ()*.502);}
 public BlockPos anchor(){return entityData.get(ANCHOR);}public Direction face(){return Direction.values()[Math.clamp(entityData.get(FACE),0,5)];}public String pattern(){return entityData.get(PATTERN);}public int inkColor(){return entityData.get(COLOR);}public int rotation(){return entityData.get(ROTATION);}public boolean owned(net.minecraft.world.entity.player.Player p){return p.getUUID().equals(owner);}
 @Override public void tick(){super.tick();if(!level().isClientSide&&tickCount%10==0&&level().hasChunkAt(anchor())){var s=level().getBlockState(anchor());if(s.getBlock()!=support||!Block.isFaceFull(s.getShape(level(),anchor()),face()))discard();}}
 @Override protected void addAdditionalSaveData(CompoundTag t){t.putInt("AnchorX",anchor().getX());t.putInt("AnchorY",anchor().getY());t.putInt("AnchorZ",anchor().getZ());t.putInt("Face",face().ordinal());t.putString("Pattern",pattern());t.putInt("InkColor",inkColor());t.putInt("Rotation",rotation());if(owner!=null)t.putUUID("Owner",owner);if(support!=null)t.putString("Support",net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(support).toString());}
 @Override protected void readAdditionalSaveData(CompoundTag t){configure(new BlockPos(t.getInt("AnchorX"),t.getInt("AnchorY"),t.getInt("AnchorZ")),Direction.values()[Math.clamp(t.getInt("Face"),0,5)],t.getString("Pattern"),t.getInt("InkColor"),t.hasUUID("Owner")?t.getUUID("Owner"):null,t.getInt("Rotation"));if(t.contains("Support"))support=net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse(t.getString("Support")));}
 @Override public boolean isPickable(){return false;}@Override public boolean isPushable(){return false;}@Override public boolean isNoGravity(){return true;}
}
