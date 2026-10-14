package dev.kelmagic.server;
import dev.kelmagic.core.*;
import dev.kelmagic.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import java.util.*;
public final class PairEffects {
 public static boolean execute(ServerPlayer p,SpellParser.Plan pair){var w=pair.actions();String a=w.get(0),b=w.get(1);double scale=pair.scale();
  if(a.equals("rak")&&b.equals("rap")){var v=p.getLookAngle().normalize().scale(Math.min(4.5,2.4*scale));EntitySpells.motion(p,v);p.fallDistance=0;EntitySpells.buff(p,"slow_falling",SpellProfiles.duration(60,scale));return true;}
  if(a.equals(b)&&(!PairRules.actors(a)&&!Set.of("no","lu","muk","nis").contains(a))){var one=SpellParser.corePlan(List.of(a));var amplified=new SpellParser.Plan(one.effect(),one.actions(),one.selector(),one.area(),one.negate(),Math.max(.1,Math.min(11.4,2*scale)));return SpellEffects.execute(p,amplified);}
  // Explicit protective combinations for pairs that used to be incomplete/contradictory.
  int duration=SpellProfiles.duration(240,scale);var targets=new ArrayList<LivingEntity>();targets.add(p);
  if(PairRules.actors(a)||PairRules.actors(b)){var aim=SpellEffects.aim(p);if(aim.entity()!=null&&((w.contains("tuna")&&aim.entity() instanceof Player)||(w.contains("sila")&&aim.entity() instanceof Mob)))targets.add(aim.entity());}
  if(w.contains("lu")){var center=p.position();for(var e:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(Math.min(12,Config.AREA_RADIUS.get()*scale)),e->e.isAlive()&&!e.isSpectator()&&p.hasLineOfSight(e))){if(targets.size()>=Config.MAX_TARGETS.get())break;if(w.contains("tuna")&&!(e instanceof Player)||w.contains("sila")&&!(e instanceof Mob))continue;if(!targets.contains(e))targets.add(e);}}
  for(var target:targets){if(w.contains("no")){target.removeEffect(MobEffects.POISON);target.removeEffect(MobEffects.WEAKNESS);}target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,duration,Math.min(4,Math.max(0,(int)scale-1))),p);}
  p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,p.getX(),p.getY()+1,p.getZ(),8,.3,.5,.3,.1);return true;
 }
 private PairEffects(){}
}
