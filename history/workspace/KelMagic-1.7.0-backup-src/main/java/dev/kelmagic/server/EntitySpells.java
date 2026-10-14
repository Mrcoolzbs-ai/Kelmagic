package dev.kelmagic.server;
import dev.kelmagic.*;
import dev.kelmagic.audio.*;
import dev.kelmagic.core.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
public final class EntitySpells {
 static void buff(LivingEntity e,String id,int time){e.addEffect(new MobEffectInstance(ExpandedSpellEffects.status(id),Math.max(1,time),0));}
 static void motion(LivingEntity e,Vec3 v){e.setDeltaMovement(v);e.hasImpulse=true;e.hurtMarked=true;}
 static boolean harm(ServerPlayer p,LivingEntity e,float n){return e.hurt(e==p?MagicDamage.source(p,"spell"):MagicDamage.spell(p,e),n);}
 public static boolean apply(ServerPlayer p,LivingEntity e,String a,int time,float power,double scale){
 switch(a){
 case "SCAN_PLAYERS":buff(e,"glowing",time);return true;
 case "PACIFY":if(e instanceof Mob m){m.setTarget(null);m.getNavigation().stop();return true;}return false;
 case "HOSTILE":if(e instanceof Mob m){m.setTarget(p);return true;}return false;
 case "COMMAND":if(e instanceof Mob m){if(m.isLeashed())m.dropLeash(true,true);m.setTarget(null);m.getNavigation().stop();return true;}return false;
 case "EAT":if(e instanceof Player pl){var s=pl.getMainHandItem();var food=s.get(net.minecraft.core.component.DataComponents.FOOD);if(food==null)return false;pl.getFoodData().eat(food.nutrition(),food.saturation());s.shrink(1);return true;}return false;
 case "DRINK":e.clearFire();buff(e,"water_breathing",time);return true;
 case "STEP":motion(e,e.getDeltaMovement().add(horizontal(e.getLookAngle()).scale(Math.min(2,.7*scale))));return true;
 case "GIVE":{var s=p.getOffhandItem();if(s.isEmpty()||e==p)return false;var one=s.copyWithCount(1);if(e instanceof Player pl){if(!pl.getInventory().add(one))return false;}else if(e instanceof Mob m){if(!m.getMainHandItem().isEmpty())return false;m.setItemSlot(EquipmentSlot.MAINHAND,one);m.setDropChance(EquipmentSlot.MAINHAND,1f);}else return false;s.shrink(1);return true;}
 case "COLLECT":return collect(p,e.position(),8,false)>0;
 case "ANCHOR":motion(e,Vec3.ZERO);buff(e,"slowness",Math.min(time,80));return true;
 case "LOVE":e.heal(power);buff(e,"regeneration",time);return true;
 case "FALL":motion(e,new Vec3(e.getDeltaMovement().x,-Math.min(1.5,.8*scale),e.getDeltaMovement().z));return true;
 case "LEAD":if(e instanceof Mob m){m.setTarget(null);boolean path=m.getNavigation().moveTo(p,1.3);if(!path)motion(m,horizontal(p.position().subtract(m.position())).scale(1.2));return true;}motion(e,horizontal(p.position().subtract(e.position())).scale(1.2));return true;
 case "REST":e.removeEffect(MobEffects.WEAKNESS);buff(e,"slow_falling",time);return true;
 case "BAKE":{var s=p.getOffhandItem();if(!s.is(Items.WHEAT)||s.getCount()<3)return false;s.shrink(3);give(p,new ItemStack(Items.BREAD));return true;}
 case "EXIT":return blink(p,p.position().subtract(horizontal(p.getLookAngle()).scale(Math.min(6,4*scale))));
 case "BLINK":{var hit=SpellEffects.aim(p);if(hit.type()!=HitResult.Type.BLOCK||p.distanceToSqr(hit.point())>144)return false;return blink(p,Vec3.atBottomCenterOf(hit.block().getBlockPos().above()));}
 case "FEAR":if(e instanceof Mob m){m.setTarget(null);var v=e.position().subtract(p.position()).normalize().scale(7);m.getNavigation().moveTo(e.getX()+v.x,e.getY(),e.getZ()+v.z,1.6);motion(e,horizontal(v).scale(1.1));return true;}return false;
 case "REVEAL":e.removeEffect(MobEffects.INVISIBILITY);buff(e,"glowing",time);return true;
 case "KNOCK":motion(e,horizontal(e.position().subtract(p.position())).scale(Math.min(3,1.5*scale)));return true;
 case "REEL":{var to=p.position().add(horizontal(p.getLookAngle()).scale(2)).subtract(e.position());motion(e,horizontal(to).scale(Math.min(3.8,2.2*scale)).add(0,.12,0));return true;}
 case "THROW":motion(e,horizontal(e.position().subtract(p.position())).scale(Math.min(2,scale)).add(0,Math.min(1.4,.8*scale),0));return true;
 case "CATCH":motion(e,new Vec3(e.getDeltaMovement().x,Math.max(0,e.getDeltaMovement().y),e.getDeltaMovement().z));e.fallDistance=0;buff(e,"slow_falling",Math.min(time,100));return true;
 case "LOW":motion(e,Vec3.ZERO);buff(e,"weakness",time);return true;
 case "JUMP":motion(e,e.getDeltaMovement().add(0,Math.min(1.5,.6*scale),0));return true;
 case "DASH":motion(e,horizontal(e.getLookAngle()).scale(Math.min(3.2,1.7*scale)).add(0,.08,0));buff(e,"speed",Math.min(time,100));return true;
 case "GLIDE":motion(e,e.getDeltaMovement().add(0,.7,0));buff(e,"slow_falling",time*2);return true;
 case "DROWSY":if(e instanceof Mob m){m.setTarget(null);m.getNavigation().stop();}buff(e,"slowness",time);return true;
 case "WAKE":return e.removeEffect(MobEffects.MOVEMENT_SLOWDOWN)|e.removeEffect(MobEffects.WEAKNESS)|e.removeEffect(MobEffects.DARKNESS);
 case "COOK":return cook(p);
 case "PIERCE":{boolean hit=harm(p,e,power);if(hit)motion(e,horizontal(e.position().subtract(p.position())).scale(.7));return hit;}
 case "DECAY":{boolean hit=harm(p,e,power);if(hit)buff(e,"wither",Math.min(time,80));return hit;}
 case "SLASH":{boolean hit=harm(p,e,power);if(hit)buff(e,"weakness",time);return hit;}
 case "ARCHER":buff(e,"night_vision",time);buff(e,"slowness",Math.min(time,100));return true;
 case "BOLT":{boolean hit=harm(p,e,power);if(hit)buff(e,"glowing",time);return hit;}
 case "TETHER":if(e instanceof Mob m)m.getNavigation().stop();motion(e,Vec3.ZERO);e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,Math.min(time,100),4));return true;
 case "MEND":{boolean any=false;for(var s:e.getArmorSlots())if(s.isDamaged()){s.setDamageValue(Math.max(0,s.getDamageValue()-(int)(8*scale)));any=true;}return any;}
 case "BOOTS":e.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);buff(e,"jump_boost",time);return true;
 case "HELMET":e.removeEffect(MobEffects.BLINDNESS);buff(e,"water_breathing",time);return true;
 case "AURA":buff(e,"absorption",time);buff(e,"glowing",time);return true;
 case "SKIN":e.removeEffect(MobEffects.POISON);buff(e,"absorption",time);return true;
 case "FOCUS":e.removeEffect(MobEffects.DARKNESS);e.removeEffect(MobEffects.WEAKNESS);buff(e,"haste",time);return true;
 case "SATIATE":if(e instanceof Player pl){pl.getFoodData().setSaturation(pl.getFoodData().getFoodLevel());return true;}return false;
 case "BACKSTEP":motion(e,horizontal(e.getLookAngle()).scale(-Math.min(2,scale)));return true;
 case "SHOULDER":motion(e,e.getDeltaMovement().add(0,.4,0));buff(e,"resistance",Math.min(time,100));return true;
 case "KNEE":motion(e,e.getDeltaMovement().add(0,-.4,0));buff(e,"slowness",Math.min(time,100));return true;
 case "ELBOW":{var look=horizontal(e.getLookAngle());motion(e,e.getDeltaMovement().add(-look.z,0,look.x).scale(Math.min(2,scale)));return true;}
 default:throw new IllegalStateException("Unknown entity action "+a);
 }
 }
 static Vec3 horizontal(Vec3 v){return new Vec3(v.x,0,v.z).normalize();}
 static void give(ServerPlayer p,ItemStack s){if(!p.getInventory().add(s))p.drop(s,false);}
 static boolean cook(ServerPlayer p){var s=p.getOffhandItem();Map<Item,Item> recipes=Map.of(Items.BEEF,Items.COOKED_BEEF,Items.PORKCHOP,Items.COOKED_PORKCHOP,Items.CHICKEN,Items.COOKED_CHICKEN,Items.MUTTON,Items.COOKED_MUTTON,Items.RABBIT,Items.COOKED_RABBIT,Items.COD,Items.COOKED_COD,Items.SALMON,Items.COOKED_SALMON,Items.POTATO,Items.BAKED_POTATO,Items.KELP,Items.DRIED_KELP);var cooked=recipes.get(s.getItem());if(cooked==null)return false;s.shrink(1);give(p,new ItemStack(cooked));return true;}
 static boolean blink(ServerPlayer p,Vec3 point){var l=p.serverLevel();var pos=BlockPos.containing(point);if(!l.hasChunkAt(pos)||!l.getWorldBorder().isWithinBounds(pos)||!l.mayInteract(p,pos)||!l.getBlockState(pos.below()).isCollisionShapeFullBlock(l,pos.below()))return false;var box=p.getBoundingBox().move(point.subtract(p.position()));if(!l.noCollision(p,box)||l.containsAnyLiquid(box)||!l.getBlockState(pos).isAir()||!l.getBlockState(pos.above()).isAir())return false;p.teleportTo(point.x,point.y,point.z);p.fallDistance=0;return true;}
 public static int collect(ServerPlayer p,Vec3 center,double radius,boolean inventory){int count=0;for(var e:p.serverLevel().getEntitiesOfClass(ItemEntity.class,new AABB(center,center).inflate(radius),e->e.isAlive()&&e.distanceToSqr(center)<=radius*radius)){if(count>=64)break;if(!p.hasLineOfSight(e))continue;var target=e.getTarget();if(target!=null&&!target.equals(p.getUUID()))continue;if(inventory){if(e.hasPickUpDelay())continue;var s=e.getItem().copy();int before=s.getCount();p.getInventory().add(s);if(s.getCount()<before){if(s.isEmpty())e.discard();else e.setItem(s);count++;}}else{e.setDeltaMovement(center.subtract(e.position()).normalize().scale(1.1));e.hasImpulse=true;count++;}}return count;}
 public static boolean utility(ServerPlayer p,SpellEffects.Aim aim,String a){switch(a){
 case "INSPECT":{String text=aim.entity()!=null?aim.entity().getName().getString()+": "+aim.entity().getHealth()+" / "+aim.entity().getMaxHealth()+" HP":p.serverLevel().getBlockState(aim.block().getBlockPos()).getBlock().getName().getString()+": "+p.serverLevel().getBlockState(aim.block().getBlockPos()).getDestroySpeed(p.serverLevel(),aim.block().getBlockPos());p.displayClientMessage(Component.literal(text),false);return true;}
 case "READ":{var s=p.getOffhandItem();var w=s.getOrDefault(MagicComponents.SPELL.get(),List.<String>of());if(w.isEmpty())return false;p.displayClientMessage(Component.literal(String.join(" ",w)),false);return true;}
 case "SPELL_INFO":{var w=p.getOffhandItem().getOrDefault(MagicComponents.CODEX_WORDS.get(),List.<String>of());if(w.isEmpty())return false;String word=w.get((int)Math.floorMod(p.serverLevel().getGameTime()/20,w.size()));p.displayClientMessage(Component.literal(word+": "+SpellProfiles.describe(word)),false);return true;}
 case "COUNT":{int emerald=0,gold=0,nuggets=0;for(var s:p.getInventory().items){if(s.is(Items.EMERALD))emerald+=s.getCount();if(s.is(Items.GOLD_INGOT))gold+=s.getCount();if(s.is(Items.GOLD_NUGGET))nuggets+=s.getCount();}p.displayClientMessage(Component.translatable("kelmagic.count",emerald,gold,nuggets),false);return true;}
 case "SCAN_ITEMS":{var list=p.serverLevel().getEntitiesOfClass(ItemEntity.class,p.getBoundingBox().inflate(12));int n=0;for(var e:list){if(n++>=64)break;p.serverLevel().sendParticles(ParticleTypes.END_ROD,e.getX(),e.getY()+.3,e.getZ(),6,.15,.3,.15,0);}return !list.isEmpty();}
 case "ECHO":{var list=p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(12),e->e!=p&&e.isAlive()&&p.hasLineOfSight(e));list.sort(Comparator.comparingDouble(p::distanceToSqr));if(list.isEmpty())return false;var e=list.getFirst();p.displayClientMessage(Component.translatable("kelmagic.echo",e.getName(),Math.round(p.distanceTo(e))),false);return true;}
 case "VOICE":p.serverLevel().playSound(null,BlockPos.containing(aim.point()),net.minecraft.sounds.SoundEvents.BELL_BLOCK,net.minecraft.sounds.SoundSource.PLAYERS,1,1);p.serverLevel().sendParticles(ParticleTypes.NOTE,aim.point().x,aim.point().y+1,aim.point().z,12,.5,.5,.5,0);return true;
 default:return false;
 }}
 private EntitySpells(){}
}
