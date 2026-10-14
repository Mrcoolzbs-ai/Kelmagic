package dev.kelmagic.server;
import dev.kelmagic.*;
import dev.kelmagic.core.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
/** All edits are bounded, loaded-chunk-only, preserve block entities and honor protection events. */
public final class WorldSpells {
 static BlockState state(String s){if(s.equals("torch"))return MagicLamps.TORCH.get().defaultBlockState();if(s.equals("sea_lantern"))return MagicLamps.LANTERN.get().defaultBlockState();return BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(s)).defaultBlockState();}
 public static boolean allowed(ServerPlayer p,BlockPos pos){var l=p.serverLevel();return !p.isSpectator()&&p.getAbilities().mayBuild&&l.hasChunkAt(pos)&&l.isInWorldBounds(pos)&&l.getWorldBorder().isWithinBounds(pos)&&l.mayInteract(p,pos)&&p.mayUseItemAt(pos,Direction.UP,p.getMainHandItem())&&l.getBlockEntity(pos)==null;}
 public static boolean place(ServerPlayer p,BlockPos pos,BlockState next,boolean replace){var l=p.serverLevel();if(!allowed(p,pos))return false;var old=l.getBlockState(pos);if(old.equals(next)||!replace&&!old.canBeReplaced()||old.getDestroySpeed(l,pos)<0||!next.canSurvive(l,pos))return false;if(!next.getCollisionShape(l,pos).isEmpty()&&!l.getEntities((Entity)null,new AABB(pos),e->e instanceof LivingEntity).isEmpty())return false;var snap=BlockSnapshot.create(l.dimension(),l,pos);if(!l.setBlock(pos,next,3))return false;if(EventHooks.onBlockPlace(p,snap,Direction.UP)){snap.restore();return false;}return true;}
 static boolean breakBlock(ServerPlayer p,BlockPos pos){if(!allowed(p,pos))return false;var s=p.serverLevel().getBlockState(pos);if(s.isAir()||s.getDestroySpeed(p.serverLevel(),pos)<0)return false;return p.gameMode.destroyBlock(pos);}
 static boolean grow(ServerPlayer p,BlockPos pos){if(!allowed(p,pos))return false;var l=p.serverLevel();var s=l.getBlockState(pos);if(!(s.getBlock() instanceof BonemealableBlock b)||!b.isValidBonemealTarget(l,pos,s))return false;var snap=BlockSnapshot.create(l.dimension(),l,pos);if(EventHooks.onBlockPlace(p,snap,Direction.UP))return false;return BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL),l,pos,p);}
 static BlockPos anchor(SpellEffects.Aim aim){return aim.block().getBlockPos().relative(aim.block().getDirection());}
 static Direction forward(ServerPlayer p){return p.getDirection();}
 static boolean water(ServerPlayer p,BlockPos pos){if(p.serverLevel().dimensionType().ultraWarm()){p.serverLevel().sendParticles(ParticleTypes.CLOUD,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,8,.2,.2,.2,0);return false;}return place(p,pos,Blocks.WATER.defaultBlockState(),false);}
 static boolean mine(ServerPlayer p,BlockPos start,String kind,int budget){var l=p.serverLevel();var first=l.getBlockState(start);var positions=new LinkedHashSet<BlockPos>();positions.add(start);
 if(kind.equals("cross")){for(var dir:List.of(Direction.UP,Direction.DOWN,Direction.EAST,Direction.WEST))positions.add(start.relative(dir));}
 if(kind.equals("tunnel")||kind.equals("soil"))for(int i=1;i<budget;i++)positions.add(start.relative(forward(p),i));
 if(kind.equals("wood")||kind.equals("vein")){var queue=new ArrayDeque<BlockPos>();queue.add(start);while(!queue.isEmpty()&&positions.size()<budget){var at=queue.removeFirst();for(var d:Direction.values()){var n=at.relative(d);if(l.hasChunkAt(n)&&l.getBlockState(n).is(first.getBlock())&&n.distSqr(start)<=64&&!positions.contains(n)){positions.add(n);queue.addLast(n);if(positions.size()==budget)break;}}}}
 int n=0;for(var pos:positions){if(n>=budget)break;var s=l.getBlockState(pos);if(!l.hasChunkAt(pos)||!s.is(first.getBlock())&&!kind.equals("soil"))continue;if(kind.equals("soft")&&s.getDestroySpeed(l,pos)>1.5)continue;if(kind.equals("wood")&&!s.is(net.minecraft.tags.BlockTags.LOGS))continue;if(kind.equals("soil")&&!s.is(net.minecraft.tags.BlockTags.DIRT)&&!s.is(Blocks.SAND)&&!s.is(Blocks.GRAVEL))continue;if(breakBlock(p,pos))n++;}return n>0;}
 public static boolean execute(ServerPlayer p,SpellEffects.Aim aim,SpellParser.Plan plan,SpellProfiles.Profile profile){var a=profile.action();var arg=profile.status();var l=p.serverLevel();if(a.equals("W_SOUND")||a.equals("W_PARTICLES")){particles(p,aim.point(),arg);return true;} if(aim.type()!=HitResult.Type.BLOCK)return false;var hit=aim.block().getBlockPos();if(!allowed(p,hit))return false;var at=anchor(aim);var old=l.getBlockState(hit);int limit=Math.max(1,Math.min(32,(int)Math.round((plan.area()?9:4)*plan.scale())));
 if(plan.area()&&Set.of("W_PLACE","W_PLANT","W_GROW","W_HARVEST","W_CONVERT","W_FREEZE","W_WARM","W_DRY","W_TICK","W_MOISTEN","W_PUDDLE").contains(a)){
  boolean any=false;var single=new SpellParser.Plan(plan.effect(),plan.actions(),plan.selector(),false,plan.negate(),Math.min(3,plan.scale()));
  for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){var pos=hit.offset(x,0,z);if(!l.hasChunkAt(pos))continue;var b=new BlockHitResult(Vec3.atCenterOf(pos),aim.block().getDirection(),pos,false);any|=execute(p,new SpellEffects.Aim(b.getLocation(),HitResult.Type.BLOCK,b,null),single,profile);}return any;
 }
 // World inverses are explicitly supported where meaningful; unsupported negatives fail safely.
 if(plan.negate()){
  if(a.equals("W_FREEZE"))return old.is(Blocks.ICE)&&place(p,hit,Blocks.WATER.defaultBlockState(),true);
  if(a.equals("W_PUDDLE"))return old.hasProperty(BlockStateProperties.WATERLOGGED)&&old.getValue(BlockStateProperties.WATERLOGGED)&&place(p,hit,old.setValue(BlockStateProperties.WATERLOGGED,false),true);
  if(a.equals("W_WATER")||a.equals("W_POOL"))return old.is(Blocks.WATER)&&place(p,hit,Blocks.AIR.defaultBlockState(),true);
  if(a.equals("W_PLACE"))return old.is(state(arg).getBlock())&&breakBlock(p,hit);
  if(a.equals("W_CONVERT")){var s=arg.split(">");return old.is(state(s[1]).getBlock())&&place(p,hit,state(s[0]),true);}
  return false;
 }
 switch(a){
 case "W_PLACE":{boolean any=false;int n=Math.max(1,Math.min(8,(int)Math.round(plan.scale())));for(int i=0;i<n;i++)any|=place(p,at.relative(forward(p),i),state(arg),false);return any;}
 case "W_LIGHT":return place(p,at,MagicLamps.torchState(aim.block().getDirection()),false);
 case "W_WATER":return water(p,at);
 case "W_PUDDLE":return old.hasProperty(BlockStateProperties.WATERLOGGED)&&!old.getValue(BlockStateProperties.WATERLOGGED)&&!l.dimensionType().ultraWarm()&&place(p,hit,old.setValue(BlockStateProperties.WATERLOGGED,true),true);
 case "W_PILLAR":{boolean any=false;for(int i=0;i<scaled(3,plan.scale(),12);i++)any|=place(p,at.above(i),state(arg),false);return any;}
 case "W_POOL":{boolean any=false;for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)any|=water(p,at.offset(x,0,z));return any;}
 case "W_CHANNEL":{boolean any=false;for(int i=0;i<scaled(3,plan.scale(),12);i++)any|=water(p,at.relative(forward(p),i));return any;}
 case "W_SHATTER":return !old.isAir()&&old.getDestroySpeed(l,hit)>=0&&old.getDestroySpeed(l,hit)<=1.5f&&!net.neoforged.neoforge.common.CommonHooks.fireBlockBreak(l,p.gameMode.getGameModeForPlayer(),p,hit,old).isCanceled()&&l.destroyBlock(hit,false,p);
 case "W_MINE":return mine(p,hit,arg,arg.equals("single")||arg.equals("soft")?1:arg.equals("wood")?Math.min(8,limit*2):arg.equals("cross")?5:3);
 case "W_CLEAR":return old.is(net.minecraft.tags.BlockTags.REPLACEABLE_BY_TREES)||old.is(net.minecraft.tags.BlockTags.FLOWERS)?breakBlock(p,hit):false;
 case "W_SHELTER":case "W_DOME":{boolean any=false;for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)any|=place(p,at.offset(x,2,z),state(arg),false);if(a.equals("W_DOME"))for(int y=0;y<2;y++)for(var d:Direction.Plane.HORIZONTAL)if(d!=forward(p).getOpposite())any|=place(p,at.relative(d).above(y),state(arg),false);return any;}
 case "W_TICK":if(old.getBlock() instanceof CropBlock c&&!c.isMaxAge(old))return place(p,hit,c.getStateForAge(c.getAge(old)+1),true);return false;
 case "W_PATH":case "W_TILL":{boolean any=false;int radius=plan.area()?1:0;for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){var pos=hit.offset(x,0,z);if(!allowed(p,pos))continue;var s=l.getBlockState(pos);if(s.is(Blocks.DIRT)||s.is(Blocks.GRASS_BLOCK)||s.is(Blocks.COARSE_DIRT))any|=place(p,pos,a.equals("W_TILL")?Blocks.FARMLAND.defaultBlockState():Blocks.DIRT_PATH.defaultBlockState(),true);}return any;}
 case "W_DOOR":return door(p,at);
 case "W_WALL":{boolean any=false;for(int i=-1;i<=1;i++)any|=place(p,at.relative(forward(p).getClockWise(),i),state(arg),false);return any;}
 case "W_RING":case "W_CIRCLE":{int radius=a.equals("W_CIRCLE")?2:1;boolean any=false;for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)if(Math.max(Math.abs(x),Math.abs(z))==radius&&!(x==0&&z==-radius))any|=place(p,at.offset(x,0,z),state(arg),false);return any;}
 case "W_ROAD":case "W_RAIL":case "W_LIGHT_LINE":{boolean any=false;for(int i=0;i<(a.equals("W_ROAD")?Math.min(5,limit):3);i++){var pos=at.relative(forward(p),i*(a.equals("W_LIGHT_LINE")?2:1));any|=place(p,pos,state(a.equals("W_RAIL")?"rail":arg),false);}return any;}
 case "W_PLANT":return place(p,hit.above(),state(arg),false);
 case "W_VALVE":{if(!(old.getBlock() instanceof LeverBlock)&&!(old.getBlock() instanceof ButtonBlock))return false;boolean result=place(p,hit,old.setValue(BlockStateProperties.POWERED,!old.getValue(BlockStateProperties.POWERED)),true);if(result&&old.getBlock() instanceof ButtonBlock)l.scheduleTick(hit,old.getBlock(),20);return result;}
 case "W_WARM":if(old.is(Blocks.ICE))return place(p,hit,Blocks.WATER.defaultBlockState(),true);if(old.is(Blocks.SNOW)||old.is(Blocks.SNOW_BLOCK))return place(p,hit,Blocks.AIR.defaultBlockState(),true);return false;
 case "W_FREEZE":return old.is(Blocks.WATER)&&old.getFluidState().isSource()&&place(p,hit,Blocks.ICE.defaultBlockState(),true);
 case "W_CONVERT":{var pair=arg.split(">");return old.is(state(pair[0]).getBlock())&&place(p,hit,state(pair[1]),true);}
 case "W_DRY":if(old.hasProperty(BlockStateProperties.WATERLOGGED)&&old.getValue(BlockStateProperties.WATERLOGGED))return place(p,hit,old.setValue(BlockStateProperties.WATERLOGGED,false),true);if(old.is(Blocks.WET_SPONGE))return place(p,hit,Blocks.SPONGE.defaultBlockState(),true);if(old.is(Blocks.MUD))return place(p,hit,Blocks.DIRT.defaultBlockState(),true);return false;
 case "W_STEAM":if(old.is(Blocks.WATER)&&place(p,hit,Blocks.AIR.defaultBlockState(),true)){particles(p,Vec3.atCenterOf(hit),"cloud");return true;}return false;
 case "W_MOISTEN":return old.is(Blocks.FARMLAND)&&place(p,hit,old.setValue(FarmBlock.MOISTURE,7),true);
 case "W_FROST":return place(p,hit.above(),Blocks.SNOW.defaultBlockState(),false);
 case "W_WAVE":{if(!old.is(Blocks.WATER)||!allowed(p,hit))return false;var dest=hit.relative(forward(p));if(!allowed(p,dest)||!l.getBlockState(dest).isAir())return false;var snap=BlockSnapshot.create(l.dimension(),l,hit);if(!place(p,hit,Blocks.AIR.defaultBlockState(),true))return false;if(water(p,dest))return true;snap.restore();return false;}
 case "W_PLATFORM":case "W_RAFT":case "W_LEDGE":{boolean any=false;for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)any|=place(p,at.offset(x,0,z),state(arg),false);return any;}
 case "W_LIGHTNING":if(!allowed(p,at))return false;var bolt=EntityType.LIGHTNING_BOLT.create(l);if(bolt==null)return false;bolt.moveTo(Vec3.atBottomCenterOf(at));bolt.setCause(p);return l.addFreshEntity(bolt);
 case "W_WASH":return old.is(net.minecraft.tags.BlockTags.WOOL)&&place(p,hit,Blocks.WHITE_WOOL.defaultBlockState(),true);
 case "W_GROW":return grow(p,hit);
 case "W_GROW_AREA":{boolean any=false;for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)any|=grow(p,hit.offset(x,0,z));return any;}
 case "W_HARVEST":return old.getBlock() instanceof CropBlock c&&c.isMaxAge(old)&&breakBlock(p,hit);
 case "W_STRIP":{String id=BuiltInRegistries.BLOCK.getKey(old.getBlock()).getPath();if(!old.is(net.minecraft.tags.BlockTags.LOGS)||id.startsWith("stripped_"))return false;var stripped=state("stripped_"+id);if(stripped.isAir())return false;if(old.hasProperty(BlockStateProperties.AXIS)&&stripped.hasProperty(BlockStateProperties.AXIS))stripped=stripped.setValue(BlockStateProperties.AXIS,old.getValue(BlockStateProperties.AXIS));return place(p,hit,stripped,true);}
 case "W_VACUUM":return EntitySpells.collect(p,Vec3.atCenterOf(hit),8,true)>0;
 case "W_THREAD":return place(p,at,Blocks.TRIPWIRE.defaultBlockState(),false);
 case "TURN":return old.hasProperty(BlockStateProperties.HORIZONTAL_FACING)&&place(p,hit,old.setValue(BlockStateProperties.HORIZONTAL_FACING,old.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()),true);
 default:throw new IllegalStateException("Unknown world action "+a);
 }
 }
 static boolean door(ServerPlayer p,BlockPos at){var l=p.serverLevel();if(!allowed(p,at)||!allowed(p,at.above())||!l.getBlockState(at).canBeReplaced()||!l.getBlockState(at.above()).canBeReplaced())return false;var a=BlockSnapshot.create(l.dimension(),l,at);var b=BlockSnapshot.create(l.dimension(),l,at.above());var state=Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,p.getDirection());if(!place(p,at,state,false))return false;if(place(p,at.above(),state.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),false))return true;b.restore();a.restore();return false;}
 static void particles(ServerPlayer p,Vec3 v,String kind){var l=p.serverLevel();switch(kind){case "note":case "drum":case "thunder":l.playSound(null,BlockPos.containing(v),kind.equals("thunder")?net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER:kind.equals("drum")?net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BASEDRUM.value():net.minecraft.sounds.SoundEvents.NOTE_BLOCK_HARP.value(),net.minecraft.sounds.SoundSource.PLAYERS,1,1);break;case "rainbow":for(int i=0;i<7;i++)l.sendParticles(new DustParticleOptions(new org.joml.Vector3f((float)Math.sin(i+1)*.5f+.5f,(float)Math.sin(i+3)*.5f+.5f,(float)Math.sin(i+5)*.5f+.5f),1),v.x-1.5+i*.5,v.y+1+Math.sin(i*Math.PI/6),v.z,5,.1,.1,.1,0);break;default:l.sendParticles(kind.equals("smoke")?ParticleTypes.LARGE_SMOKE:kind.equals("star")?ParticleTypes.END_ROD:ParticleTypes.CLOUD,v.x,v.y+1,v.z,kind.equals("star")?16:64,.7,1,.7,.01);}}
 public static int scaled(int base,double scale,int cap){return Math.max(1,Math.min(cap,(int)Math.round(base*scale)));}
 public static boolean synergy(ServerPlayer p,SpellParser.Plan plan,SpellEffects.Aim aim){
  String id=plan.actions().getFirst();double scale=plan.scale();var l=p.serverLevel();
  if(id.equals("guardian")){p.heal((float)Math.min(32,6*scale));p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,SpellProfiles.duration(400,scale),Math.min(4,Math.max(0,(int)scale-1))));EntitySpells.buff(p,"absorption",SpellProfiles.duration(400,scale));return true;}
  if(id.equals("spring")){EntitySpells.motion(p,p.getLookAngle().normalize().scale(Math.min(4,scale)).add(0,Math.min(1.8,.9*scale),0));EntitySpells.buff(p,"slow_falling",SpellProfiles.duration(200,scale));return true;}
  if(id.equals("magnet"))return EntitySpells.collect(p,p.position(),Math.max(1,Math.min(24,12*scale)),true)>0;
  if(id.equals("bridge")){boolean any=false;var v=EntitySpells.horizontal(p.getLookAngle());if(v.lengthSqr()<.01)v=net.minecraft.world.phys.Vec3.atLowerCornerOf(p.getDirection().getNormal());var seen=new java.util.HashSet<BlockPos>();for(int i=0;i<scaled(7,scale,32);i++){var pos=BlockPos.containing(p.position().add(v.scale(i+1.25)));if(seen.add(pos))any|=place(p,pos,Blocks.GLASS.defaultBlockState(),false);}return any;}
  if(plan.negate()||aim.type()!=HitResult.Type.BLOCK)return false;var hit=aim.block().getBlockPos();var at=anchor(aim);boolean any=false;int width=scaled(3,scale,15),height=scaled(2,scale,5),minimum=-width/2;
  switch(id){
   case "vein":return mine(p,hit,"vein",scaled(8,scale,32));
   case "grove":{int count=scaled(4,scale,12),n=0;for(int x=-2;x<=2&&n<count;x++)for(int z=-2;z<=2&&n<count;z++)if((x+z)%2==0){var pos=hit.offset(x,1,z);if(place(p,pos,Blocks.OAK_SAPLING.defaultBlockState(),false)){n++;any=true;grow(p,pos);}}return any;}
   case "garden":{int size=scaled(3,scale,7),lo=-size/2,budget=0;for(int x=lo;x<lo+size&&budget<96;x++)for(int z=lo;z<lo+size&&budget<96;z++){var pos=hit.offset(x,0,z);if(!allowed(p,pos))continue;var old=l.getBlockState(pos);if(old.is(Blocks.DIRT)||old.is(Blocks.GRASS_BLOCK)||old.is(Blocks.FARMLAND)){if(x==0&&z==0&&!l.dimensionType().ultraWarm()){if(place(p,pos,Blocks.WATER.defaultBlockState(),true)){any=true;budget++;}continue;}if(place(p,pos,Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE,7),true)){any=true;budget++;}if(budget<96&&place(p,pos.above(),Blocks.WHEAT.defaultBlockState(),false)){any=true;budget++;}}}return any;}
   case "replant":{int size=scaled(1,scale,5),lo=-size/2;for(int x=lo;x<lo+size;x++)for(int z=lo;z<lo+size;z++)any|=replant(p,hit.offset(x,0,z));return any;}
   case "ice_path":for(int i=0;i<scaled(7,scale,32);i++)any|=place(p,at.relative(forward(p),i),Blocks.ICE.defaultBlockState(),false);return any;
   case "bunker":{int size=scaled(3,scale,7),lo=-size/2,h=scaled(2,scale,4),budget=0;for(int x=lo;x<lo+size;x++)for(int z=lo;z<lo+size;z++)if(budget++<96)any|=place(p,at.offset(x,h,z),Blocks.STONE.defaultBlockState(),false);for(int x=lo;x<lo+size;x++)for(int z=lo;z<lo+size;z++)if(Math.max(Math.abs(x),Math.abs(z))>=size/2&&!(x==0&&z==lo))for(int y=0;y<h&&budget<96;y++,budget++)any|=place(p,at.offset(x,y,z),Blocks.STONE.defaultBlockState(),false);return any;}
   case "irrigate":{int radius=scaled(2,scale,4);for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){var pos=hit.offset(x,0,z);if(!allowed(p,pos))continue;var old=l.getBlockState(pos);if(old.is(Blocks.FARMLAND))any|=place(p,pos,old.setValue(FarmBlock.MOISTURE,7),true);any|=grow(p,pos);any|=grow(p,pos.above());}return any;}
   case "fire_ring":{int radius=scaled(2,scale,6),count=0;for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)if(Math.max(Math.abs(x),Math.abs(z))==radius&&count<32){if(SpellEffects.igniteBlockHit(p,new BlockHitResult(Vec3.atCenterOf(hit.offset(x,0,z)),Direction.UP,hit.offset(x,0,z),false),scale)){any=true;count++;}}return any;}
   case "beacon":{int h=scaled(3,scale,12);for(int i=0;i<h;i++)any|=place(p,at.above(i),Blocks.GLASS.defaultBlockState(),false);any|=place(p,at.above(h),MagicLamps.LANTERN.get().defaultBlockState(),false);return any;}
   case "storm":any=execute(p,aim,plan,SpellProfiles.ALL.get("dun"));for(var e:l.getEntitiesOfClass(LivingEntity.class,new AABB(hit).inflate(Math.min(12,5*scale)),e->e!=p&&!(e instanceof net.minecraft.world.entity.player.Player)&&p.hasLineOfSight(e)))EntitySpells.motion(e,EntitySpells.horizontal(e.position().subtract(Vec3.atCenterOf(hit))).scale(Math.min(4,2*scale)));return any;
   case "timber":for(int x=minimum;x<minimum+width;x++){any|=place(p,at.relative(forward(p).getClockWise(),x).above(height),Blocks.STRIPPED_OAK_LOG.defaultBlockState(),false);if(x==minimum||x==minimum+width-1)for(int y=0;y<height;y++)any|=place(p,at.relative(forward(p).getClockWise(),x).above(y),Blocks.STRIPPED_OAK_LOG.defaultBlockState(),false);}return any;
   case "ruin":for(int x=minimum;x<minimum+width;x++)for(int y=0;y<height;y++)any|=place(p,at.relative(forward(p).getClockWise(),x).above(y),Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),false);return any;
   default:throw new IllegalStateException("Unknown synergy "+id);
  }
 }
 private static boolean replant(ServerPlayer p,BlockPos hit){var l=p.serverLevel();if(!allowed(p,hit))return false;var old=l.getBlockState(hit);if(!(old.getBlock() instanceof CropBlock crop)||!crop.isMaxAge(old))return false;var before=new HashSet<java.util.UUID>();for(var e:l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(hit).inflate(2)))before.add(e.getUUID());if(!breakBlock(p,hit))return false;Item seed=old.is(Blocks.WHEAT)?Items.WHEAT_SEEDS:old.is(Blocks.CARROTS)?Items.CARROT:old.is(Blocks.POTATOES)?Items.POTATO:old.is(Blocks.BEETROOTS)?Items.BEETROOT_SEEDS:Items.AIR;for(var e:l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(hit).inflate(2)))if(!before.contains(e.getUUID())&&e.getItem().is(seed)&&place(p,hit,crop.getStateForAge(0),false)){e.getItem().shrink(1);if(e.getItem().isEmpty())e.discard();break;}return true;}
 private WorldSpells(){}
}
