package dev.kelmagic.server;
import dev.kelmagic.KelMagic;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid=KelMagic.MODID)
public final class NeutralMagic {
 public static boolean neutralize(Mob m,boolean formerlyHostile){
  if(formerlyHostile && !(m instanceof Enemy)||!formerlyHostile&&m instanceof Enemy)return false;
  m.setData(MagicAttachments.NEUTRAL_KIND,formerlyHostile?1:2);m.setData(MagicAttachments.NEUTRAL_ANGER,0L);
  m.setTarget(null);m.setAggressive(false);m.setLastHurtByMob(null);m.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);m.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);m.getNavigation().stop();install(m);return true;
 }
 private static void install(Mob m){if(m.getData(MagicAttachments.NEUTRAL_KIND)==2&&!m.getData(MagicAttachments.NEUTRAL_INSTALLED)){m.goalSelector.addGoal(0,new RetaliateGoal(m));m.setData(MagicAttachments.NEUTRAL_INSTALLED,true);}}
 public static boolean mayTarget(Mob m,LivingEntity e){return e==null||m.getData(MagicAttachments.NEUTRAL_KIND)==0||e==m.getLastHurtByMob()&&m.level().getGameTime()<m.getData(MagicAttachments.NEUTRAL_ANGER);}
 @SubscribeEvent public static void target(net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent event){if(event.getEntity() instanceof Mob m&&!mayTarget(m,event.getNewAboutToBeSetTarget()))event.setCanceled(true);}
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void hurt(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event){if(event.getNewDamage()<=0||!(event.getEntity() instanceof Mob m)||m.getData(MagicAttachments.NEUTRAL_KIND)==0||!(event.getSource().getEntity() instanceof LivingEntity attacker)||attacker==m||attacker instanceof Player p&&p.getAbilities().invulnerable)return;m.setLastHurtByMob(attacker);m.setData(MagicAttachments.NEUTRAL_ANGER,m.level().getGameTime()+200);m.setTarget(attacker);install(m);}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event){if(!(event.getEntity() instanceof Mob m)||m.level().isClientSide||m.getData(MagicAttachments.NEUTRAL_KIND)==0)return;install(m);if(!mayTarget(m,m.getTarget())){m.setTarget(null);m.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);}if(m.level().getGameTime()>=m.getData(MagicAttachments.NEUTRAL_ANGER))m.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);}
 private static final class RetaliateGoal extends Goal {
  final Mob mob;int cooldown;RetaliateGoal(Mob m){mob=m;setFlags(java.util.EnumSet.of(Flag.MOVE,Flag.LOOK));}
  @Override public boolean canUse(){var t=mob.getTarget();return mob.getData(MagicAttachments.NEUTRAL_KIND)==2&&t!=null&&t.isAlive()&&mayTarget(mob,t)&&!(t instanceof Player p&&p.getAbilities().invulnerable);}
  @Override public boolean canContinueToUse(){return canUse();}
  @Override public void tick(){var t=mob.getTarget();if(t==null)return;mob.getLookControl().setLookAt(t,30,30);mob.getNavigation().moveTo(t,1.25);if(--cooldown<=0&&mob.distanceToSqr(t)<Math.pow(mob.getBbWidth()+t.getBbWidth()+.8,2)&&mob.hasLineOfSight(t)){t.hurt(mob.damageSources().mobAttack(mob),2);mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);cooldown=20;}}
  @Override public void stop(){mob.getNavigation().stop();}
 }
 private NeutralMagic(){}
}
