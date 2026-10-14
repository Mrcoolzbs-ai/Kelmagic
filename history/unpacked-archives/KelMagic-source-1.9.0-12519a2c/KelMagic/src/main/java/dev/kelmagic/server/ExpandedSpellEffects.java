package dev.kelmagic.server;
import dev.kelmagic.*;
import dev.kelmagic.core.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
/** Bounded server execution of data-defined word profiles. No worldgen or invented roots. */
public final class ExpandedSpellEffects {
 private record Operation(String action,String status,boolean harmful){}
 private static Operation operation(SpellProfiles.Profile p,boolean negate){
  if(!negate)return new Operation(p.action(),p.status(),p.harmful());
  if(p.action().equals("BUFF")){
   String inverse=switch(p.status()){
    case "speed"->"slowness";case "slowness"->"speed";case "strength"->"weakness";case "weakness"->"strength";
    case "night_vision"->"blindness";case "blindness"->"night_vision";case "regeneration"->"poison";case "poison"->"regeneration";
    case "luck"->"unluck";default->"";};
   return inverse.isEmpty()?new Operation("REMOVE",p.status(),true):new Operation("BUFF",inverse,Set.of("slowness","weakness","blindness","poison","unluck").contains(inverse));
  }
  return switch(p.action()){
   case "DAMAGE"->new Operation("HEAL","",false);case "HEAL"->new Operation("DAMAGE","",true);
   case "PUSH"->new Operation("PULL","",true);case "PULL"->new Operation("PUSH","",true);
   case "FEED"->new Operation("BUFF","hunger",true);case "EXTINGUISH"->new Operation("VANILLA_FIRE","",true);
   case "VANILLA_FIRE"->new Operation("EXTINGUISH","",false);case "CLEANSE"->new Operation("BUFF","poison",true);
   case "OPEN"->new Operation("CLOSE","",false);case "CLOSE"->new Operation("OPEN","",false);
   case "SCAN"->new Operation("REMOVE","glowing",true);default->new Operation("UNSUPPORTED","",false);};
 }
 public static boolean execute(ServerPlayer caster,SpellParser.Plan plan){
  if(plan.effect()==SpellParser.Effect.SYNERGY)return WorldSpells.synergy(caster,plan,SpellEffects.aim(caster));
  if(plan.effect()==SpellParser.Effect.FLAME_LIFE)return FlameLife.grant(caster,plan.scale());
  if(plan.effect()==SpellParser.Effect.RESIST_SELF){caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,SpellProfiles.duration(Config.RESIST_SECONDS.get()*20,plan.scale())));return true;}
  var aim=SpellEffects.aim(caster);boolean applied=false;
  // Ground fire retains its targeting semantics; scale is stored in the fire block state.
  if(plan.actions().equals(List.of("pir")) && plan.selector().isEmpty()){
   if(plan.negate()){caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,SpellProfiles.duration(1200,plan.scale())));return true;}
   return plan.area()?SpellEffects.areaFire(caster,aim,plan.scale()):aim.type()==HitResult.Type.BLOCK && SpellEffects.igniteBlockHit(caster,aim.block(),plan.scale());
  }
  for(String word:plan.actions()){
   if(word.equals("pir")){
    for(var target:targets(caster,aim,plan,true,false)){
     if(plan.negate()){target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,SpellProfiles.duration(1200,plan.scale())));target.clearFire();applied=true;}
     else if(canAffect(caster,target,true))applied|=ArcaneFire.ignite(target,SpellProfiles.duration(Config.FIRE_SECONDS.get()*60,plan.scale()),(float)plan.scale());
    }continue;
   }
   var profile=SpellProfiles.ALL.get(word);if(profile==null)continue;var op=operation(profile,plan.negate());
   if(op.action().equals("OPEN") || op.action().equals("CLOSE")){
    if(aim.type()==HitResult.Type.BLOCK)applied|=door(caster,aim.block().getBlockPos(),op.action().equals("OPEN"));continue;
   }
   if(profile.action().startsWith("W_") || profile.action().equals("TURN")){applied|=WorldSpells.execute(caster,aim,plan,profile);continue;}
   if(java.util.Set.of("INSPECT","READ","SPELL_INFO","COUNT","SCAN_ITEMS","ECHO","VOICE").contains(op.action())){applied|=EntitySpells.utility(caster,aim,op.action());continue;}
   if(op.action().equals("UNSUPPORTED"))continue;
   boolean scan=op.action().equals("SCAN")||op.action().equals("SCAN_PLAYERS");
   for(var target:targets(caster,aim,plan,op.harmful(),scan)){
    if(!canAffect(caster,target,op.harmful()))continue;
    if(op.action().equals("SCAN_PLAYERS") && !(target instanceof Player))continue;
    boolean done=apply(caster,target,profile,op,plan.scale(),plan.negate());applied|=done;if(done&&(op.action().equals("PACIFY")||op.action().equals("HOSTILE")))MagicProgress.award(caster,"neutral_magic");
   }
  }return applied;
 }
 private static boolean canAffect(ServerPlayer caster,LivingEntity e,boolean harmful){
  if(!e.isAlive()||e.isSpectator())return false;
  if(!harmful)return true;
  if(e instanceof Player p && p.getAbilities().invulnerable)return false;
  return !(e instanceof Player p) || p==caster || caster.server.isPvpAllowed() && caster.canHarmPlayer(p);
 }
 private static List<LivingEntity> targets(ServerPlayer caster,SpellEffects.Aim aim,SpellParser.Plan p,boolean harmful,boolean scan){
  if(p.selector().equals("mina") && !p.area() && !scan)return List.of(caster);
  if(!p.area() && !scan){
   if(p.selector().isEmpty() && !harmful)return List.of(caster);
   var e=aim.entity();if(e==null)return List.of();
   if(p.selector().equals("tuna") && !(e instanceof Player) || p.selector().equals("sila") && !(e instanceof Mob))return List.of();
   return List.of(e);
  }
  double radius=Math.max(1,Math.min(12,Config.AREA_RADIUS.get()*p.scale()));
  Vec3 center=(scan || p.selector().equals("mina"))?caster.position():aim.point();
  var list=caster.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(radius),e->e.isAlive()&&!e.isSpectator()&&e.distanceToSqr(center)<=radius*radius);
  list.removeIf(e->p.selector().equals("tuna")&&!(e instanceof Player)||p.selector().equals("sila")&&!(e instanceof Mob));
  if(!scan)list.removeIf(e->{var hit=caster.serverLevel().clip(new ClipContext(center.add(0,.5,0),e.getEyePosition(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,caster));return hit.getType()!=HitResult.Type.MISS && hit.getLocation().distanceToSqr(e.getEyePosition())>1;});
  list.sort(Comparator.<LivingEntity>comparingDouble(e->e.distanceToSqr(center)).thenComparing(e->e.getUUID().toString()));
  return list.subList(0,Math.min(Config.MAX_TARGETS.get(),list.size()));
 }
 static Holder<MobEffect> status(String id){return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.withDefaultNamespace(id)).orElseThrow();}
 private static boolean apply(ServerPlayer caster,LivingEntity target,SpellProfiles.Profile profile,Operation op,double scale,boolean reverse){
  int duration=SpellProfiles.duration(profile.duration(),scale);float power=(float)Math.max(.1,Math.min(32,profile.power()*scale));
  switch(op.action()){
   case "BUFF":{int amp=Math.max(0,Math.min(4,(int)Math.floor(scale)-1));for(String id:op.status().split(","))target.addEffect(new MobEffectInstance(status(id),duration,amp),caster);return true;}
   case "REMOVE":{boolean any=false;for(String id:op.status().split(","))any|=target.removeEffect(status(id));return any;}
   case "HEAL":{float old=target.getHealth();target.heal(power);return target.getHealth()>old;}
   case "DAMAGE":return target.hurt(target==caster?MagicDamage.source(caster,"spell"):MagicDamage.spell(caster,target),power);
   case "FEED":{if(!(target instanceof Player p))return false;p.getFoodData().eat(Math.max(1,(int)Math.round(power)),.4f);return true;}
   case "EXTINGUISH":{boolean burning=target.isOnFire();target.clearFire();return burning;}
   case "CLEANSE":return target.removeEffect(MobEffects.POISON)|target.removeEffect(MobEffects.HUNGER)|target.removeEffect(MobEffects.WEAKNESS);
   case "VANILLA_FIRE":{target.igniteForTicks(duration);return true;}
   case "SCAN":{target.addEffect(new MobEffectInstance(MobEffects.GLOWING,duration,0),caster);return true;}
   case "PUSH","PULL","SPIN":{
    Vec3 v=target.position().subtract(caster.position());if(v.lengthSqr()<.001)v=caster.getLookAngle();
    v=new Vec3(v.x,0,v.z).normalize();if(op.action().equals("PULL"))v=v.scale(-1);
    if(op.action().equals("SPIN"))v=new Vec3(-v.z,0,v.x).scale(reverse?-1:1);
    v=v.scale(op.action().equals("PULL")?Math.min(3.2,1.15*power):Math.min(1.5,.25*power));target.setDeltaMovement(target.getDeltaMovement().add(v.x,.12,v.z));target.hasImpulse=true;target.hurtMarked=true;return true;
   }
   default:return EntitySpells.apply(caster,target,op.action(),duration,power,scale);
  }
 }
 /** Both door halves are snapshotted and authorization is checked before either changes. */
 public static boolean door(ServerPlayer p,BlockPos hit,boolean open){
  var level=p.serverLevel();var state=level.getBlockState(hit);BlockPos lower=hit;
  boolean door=state.getBlock() instanceof DoorBlock;
  if(door && state.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER)lower=hit.below();
  var positions=door?List.of(lower,lower.above()):List.of(hit);
  if(!door && !(state.getBlock() instanceof TrapDoorBlock) && !(state.getBlock() instanceof FenceGateBlock))return false;
  if(!state.hasProperty(BlockStateProperties.OPEN) || state.getValue(BlockStateProperties.OPEN)==open)return false;
  var snapshots=new ArrayList<net.neoforged.neoforge.common.util.BlockSnapshot>();
  for(var pos:positions){
   if(!level.hasChunkAt(pos)||!level.getWorldBorder().isWithinBounds(pos)||!level.mayInteract(p,pos)||!p.mayUseItemAt(pos,Direction.UP,p.getMainHandItem()))return false;
   var st=level.getBlockState(pos);if(!st.hasProperty(BlockStateProperties.OPEN))return false;
   var snapshot=net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(),level,pos);
   snapshots.add(snapshot);
  }
  for(var snap:snapshots)level.setBlock(snap.getPos(),level.getBlockState(snap.getPos()).setValue(BlockStateProperties.OPEN,open),3);
  for(var snap:snapshots)if(net.neoforged.neoforge.event.EventHooks.onBlockPlace(p,snap,Direction.UP)){
   for(int i=snapshots.size()-1;i>=0;i--)snapshots.get(i).restore();return false;
  }
  level.playSound(null,hit,net.minecraft.sounds.SoundEvents.FLINTANDSTEEL_USE,net.minecraft.sounds.SoundSource.BLOCKS,.3f,open?1.2f:.8f);return true;
 }
 private ExpandedSpellEffects(){}
}
