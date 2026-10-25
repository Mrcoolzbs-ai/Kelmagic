package dev.kelmagic.server;
import dev.kelmagic.content.ThresholdContent;
import dev.kelmagic.entity.ThresholdPortal;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.server.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
/** World-persistent owner-scoped pairs. Absence is a tombstone for unloaded endpoints. */
@EventBusSubscriber(modid="kelmagic")
public final class ThresholdLinks extends SavedData {
 public record End(UUID id,UUID owner,ResourceKey<Level> dimension,Vec3 at,float yaw,int color,UUID mate){End link(UUID m){return new End(id,owner,dimension,at,yaw,color,m);}}
 public final Map<UUID,End> ends=new LinkedHashMap<>();
 public static ThresholdLinks get(MinecraftServer s){return s.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(ThresholdLinks::new,ThresholdLinks::load),"kelmagic_threshold_links");}
 public static ThresholdLinks load(CompoundTag tag,HolderLookup.Provider provider){var data=new ThresholdLinks();for(var t:tag.getList("Ends",Tag.TAG_COMPOUND)){var n=(CompoundTag)t;try{var e=new End(n.getUUID("Id"),n.getUUID("Owner"),ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,ResourceLocation.parse(n.getString("Dimension"))),new Vec3(n.getDouble("X"),n.getDouble("Y"),n.getDouble("Z")),n.getFloat("Yaw"),n.getInt("Color"),n.hasUUID("Mate")?n.getUUID("Mate"):null);data.ends.put(e.id,e);}catch(RuntimeException ignored){dev.kelmagic.KelMagic.LOGGER.warn("Ignoring malformed threshold endpoint");}}return data;}
 @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider provider){var list=new ListTag();for(var e:ends.values()){var n=new CompoundTag();n.putUUID("Id",e.id);n.putUUID("Owner",e.owner);n.putString("Dimension",e.dimension.location().toString());n.putDouble("X",e.at.x);n.putDouble("Y",e.at.y);n.putDouble("Z",e.at.z);n.putFloat("Yaw",e.yaw);n.putInt("Color",e.color);if(e.mate!=null)n.putUUID("Mate",e.mate);list.add(n);}tag.put("Ends",list);return tag;}
 public static net.minecraft.world.phys.BlockHitResult target(ServerPlayer p){var eye=p.getEyePosition();return p.serverLevel().clip(new net.minecraft.world.level.ClipContext(eye,eye.add(p.getLookAngle().scale(dev.kelmagic.Config.RANGE.get())),net.minecraft.world.level.ClipContext.Block.OUTLINE,net.minecraft.world.level.ClipContext.Fluid.NONE,p));}
 public static void sound(ServerLevel l,Vec3 pos,net.minecraft.sounds.SoundEvent sound,float volume){l.playSound(null,pos.x,pos.y+1,pos.z,sound,net.minecraft.sounds.SoundSource.BLOCKS,volume,1);}
 public static boolean open(ServerPlayer p){var l=p.serverLevel();var hit=target(p);if(hit.getType()!=HitResult.Type.BLOCK)return false;var floor=hit.getBlockPos();if(!l.getBlockState(floor).isFaceSturdy(l,floor,Direction.UP))return false;var at=Vec3.atBottomCenterOf(floor.above());var box=new AABB(at.x-.48,at.y,at.z-.48,at.x+.48,at.y+2,at.z+.48);
  if(!l.getWorldBorder().isWithinBounds(BlockPos.containing(at))||!l.isInWorldBounds(BlockPos.containing(at).above())||!l.hasChunkAt(BlockPos.containing(at))||!l.noCollision(box)||!l.mayInteract(p,BlockPos.containing(at))||!p.getAbilities().mayBuild)return false;
  var data=get(p.getServer());var pending=data.ends.values().stream().filter(e->e.owner.equals(p.getUUID())&&e.mate==null).findFirst().orElse(null);
  int color=pending==null?net.minecraft.util.Mth.hsvToRgb(l.random.nextFloat(),.65f,1f)&0xffffff:pending.color;
  var portal=ThresholdContent.TYPE.get().create(l);if(portal==null)return false;portal.moveTo(at.x,at.y,at.z,p.getDirection().toYRot(),0);portal.color(color);
  var end=new End(portal.getUUID(),p.getUUID(),l.dimension(),at,portal.getYRot(),color,pending==null?null:pending.id);
  data.ends.put(end.id,end);if(!l.addFreshEntity(portal)){data.ends.remove(end.id);return false;}
  if(pending!=null){data.ends.put(pending.id,pending.link(end.id));MagicProgress.award(p,"threshold_pair");}sound(l,at,dev.kelmagic.MagicSounds.THRESHOLD_OPEN.get(),.7f);data.setDirty();return true;
 }
 public static void close(ThresholdPortal portal,Entity attacker){if(!(portal.level() instanceof ServerLevel l))return;var data=get(l.getServer());var e=data.ends.remove(portal.getUUID());if(e!=null){sound(l,e.at,dev.kelmagic.MagicSounds.THRESHOLD_CLOSE.get(),.7f);if(e.mate!=null){var mate=data.ends.remove(e.mate);if(mate!=null){var world=l.getServer().getLevel(mate.dimension);if(world!=null){sound(world,mate.at,dev.kelmagic.MagicSounds.THRESHOLD_CLOSE.get(),.7f);var entity=world.getEntity(mate.id);if(entity!=null)entity.discard();}}}data.setDirty();if(attacker instanceof ServerPlayer p)MagicProgress.award(p,"threshold_break");}portal.discard();}
 public static void tick(ThresholdPortal portal){var l=(ServerLevel)portal.level();var data=get(l.getServer());var e=data.ends.get(portal.getUUID());if(e==null){portal.discard();return;}if((portal.tickCount+portal.getId())%100==0)sound(l,e.at,dev.kelmagic.MagicSounds.THRESHOLD_HUM.get(),.25f);if(e.mate==null)return;if(dev.kelmagic.artifact.ArtifactWorld.portalBlocked(l,e.at)){if(portal.tickCount%80==0)dev.kelmagic.artifact.ArtifactVisuals.sound(l,e.at,"threshold_lock",.7f);return;}var mate=data.ends.get(e.mate);if(mate==null){close(portal,null);return;}long now=l.getServer().overworld().getGameTime();
  for(var traveler:l.getEntities(portal,portal.getBoundingBox(),t->t instanceof LivingEntity&&!(t instanceof ThresholdPortal)&&t.isAlive()&&!t.isPassenger()&&!t.isVehicle())){
   if(traveler.getPersistentData().getLong("kelmagicThresholdUntil")>now)continue;var target=l.getServer().getLevel(mate.dimension);if(target==null)continue;
   var center=BlockPos.containing(mate.at);target.getChunkSource().addRegionTicket(TicketType.PORTAL,new ChunkPos(center),3,center);target.getChunk(center);if(dev.kelmagic.artifact.ArtifactWorld.portalBlocked(target,mate.at))continue;Vec3 arrival=null;var normal=net.minecraft.core.Direction.fromYRot(mate.yaw);
   for(int vertical=0;vertical<=2&&arrival==null;vertical++)for(int side:new int[]{1,-1}){var pos=mate.at.add(normal.getStepX()*1.4*side,vertical,normal.getStepZ()*1.4*side);var moved=traveler.getBoundingBox().move(pos.subtract(traveler.position()));if(target.getWorldBorder().isWithinBounds(BlockPos.containing(pos))&&target.noCollision(traveler,moved)&&target.getBlockState(BlockPos.containing(pos).below()).isFaceSturdy(target,BlockPos.containing(pos).below(),Direction.UP)){arrival=pos;break;}}
   if(arrival==null)continue;sound(l,e.at,dev.kelmagic.MagicSounds.THRESHOLD_TRAVEL.get(),.55f);sound(target,mate.at,dev.kelmagic.MagicSounds.THRESHOLD_TRAVEL.get(),.55f);traveler.getPersistentData().putLong("kelmagicThresholdUntil",now+60);boolean cross=traveler.level()!=target;var moved=traveler.changeDimension(new DimensionTransition(target,arrival,Vec3.ZERO,mate.yaw,traveler.getXRot(),DimensionTransition.DO_NOTHING));if(moved!=null){moved.getPersistentData().putLong("kelmagicThresholdUntil",now+60);if(moved instanceof ServerPlayer p){dev.kelmagic.artifact.ArtifactRuntime.arrived(p);if(cross)MagicProgress.award(p,"threshold_dimensions");}}
  }
 }
 @SubscribeEvent public static void impact(net.neoforged.neoforge.event.entity.ProjectileImpactEvent event){if(event.getRayTraceResult() instanceof EntityHitResult hit&&hit.getEntity() instanceof ThresholdPortal portal&&!portal.level().isClientSide){close(portal,event.getProjectile().getOwner());event.getProjectile().discard();event.setCanceled(true);}}
}
