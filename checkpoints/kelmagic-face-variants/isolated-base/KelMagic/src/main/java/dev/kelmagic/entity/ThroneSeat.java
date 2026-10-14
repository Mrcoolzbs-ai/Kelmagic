package dev.kelmagic.entity;
import dev.kelmagic.*;import dev.kelmagic.server.*;import net.minecraft.core.BlockPos;import net.minecraft.nbt.CompoundTag;import net.minecraft.world.entity.*;import net.minecraft.world.level.Level;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.phys.Vec3;
/** Invisible, server-owned seat; unlike a dummy armour stand it has no hitbox or equipment. */
public final class ThroneSeat extends Entity {
 private BlockPos home=BlockPos.ZERO;private int manaTicks;
 public ThroneSeat(EntityType<? extends ThroneSeat> type,Level level){super(type,level);noPhysics=true;setNoGravity(true);}
 @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){}
 public void bind(BlockPos p){home=p.immutable();var st=level().getBlockState(p);var facing=st.hasProperty(dev.kelmagic.block.MageThroneBlock.FACING)?st.getValue(dev.kelmagic.block.MageThroneBlock.FACING):net.minecraft.core.Direction.NORTH;setPos(p.getX()+.5-facing.getStepX()*.25,p.getY()-.17,p.getZ()+.5-facing.getStepZ()*.25);}
 public BlockPos home(){return home;}
 public boolean valid(){return !isRemoved()&&level().getBlockState(home).is(MagicMascot.THRONE.get())&&level().getBlockState(home.above()).is(MagicMascot.THRONE.get());}
 public boolean regenerate(ServerPlayer p){if(!valid()||p.getVehicle()!=this)return false;if(++manaTicks<20)return false;manaTicks=0;var m=p.getData(MagicAttachments.MANA);double max=MagicServer.maximum(p);p.setData(MagicAttachments.MANA,m.withCurrent(Math.min(max,m.current()+1)));return true;}
 @Override public void tick(){super.tick();if(!level().isClientSide&&(!valid()||getPassengers().isEmpty())){ejectPassengers();discard();}}
 @Override protected boolean canAddPassenger(Entity p){return p instanceof net.minecraft.world.entity.player.Player&&getPassengers().isEmpty();}
 @Override protected void positionRider(Entity p,Entity.MoveFunction move){if(hasPassenger(p))move.accept(p,getX(),getY(),getZ());}
 @Override public Vec3 getDismountLocationForPassenger(LivingEntity p){for(var dir:net.minecraft.core.Direction.Plane.HORIZONTAL){var at=home.relative(dir);var v=Vec3.atBottomCenterOf(at);if(level().noCollision(p,p.getBoundingBox().move(v.subtract(p.position()))))return v;}return Vec3.atBottomCenterOf(home.above(2));}
 @Override protected void readAdditionalSaveData(CompoundTag n){home=BlockPos.of(n.getLong("Throne"));manaTicks=Math.clamp(n.getInt("ManaTicks"),0,19);}
 @Override protected void addAdditionalSaveData(CompoundTag n){n.putLong("Throne",home.asLong());n.putInt("ManaTicks",manaTicks);}
}
