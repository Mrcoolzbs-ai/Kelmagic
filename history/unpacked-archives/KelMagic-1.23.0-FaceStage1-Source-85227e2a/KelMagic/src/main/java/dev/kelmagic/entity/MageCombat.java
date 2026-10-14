package dev.kelmagic.entity;
import dev.kelmagic.*;import dev.kelmagic.server.*;import net.minecraft.world.entity.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.entity.projectile.Projectile;import net.minecraft.world.entity.monster.Monster;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.phys.*;import net.minecraft.core.*;import net.minecraft.core.particles.ParticleTypes;import net.minecraft.world.level.*;import net.minecraft.world.level.block.*;import net.minecraft.world.damagesource.*;import net.minecraft.resources.*;import net.minecraft.core.registries.Registries;import net.minecraft.world.effect.*;import net.minecraft.nbt.CompoundTag;
/** Health-latched boss stages; independent per-entity action/evasion/support cooldowns. */
public final class MageCombat {
 public static final class State {
  public long nextAction,nextDodge,nextBuff,nextHeal,nextLaugh;public int action;
  public void save(CompoundTag n,long now){n.putInt("BossAction",action);n.putLong("BossActionWait",Math.max(0,nextAction-now));n.putLong("BossDodgeWait",Math.max(0,nextDodge-now));n.putLong("BossBuffWait",Math.max(0,nextBuff-now));n.putLong("BossHealWait",Math.max(0,nextHeal-now));}
  public void load(CompoundTag n,long now){action=Math.max(0,n.getInt("BossAction"));nextAction=now+Math.clamp(n.getLong("BossActionWait"),0,160);nextDodge=now+Math.clamp(n.getLong("BossDodgeWait"),0,100);nextBuff=now+Math.clamp(n.getLong("BossBuffWait"),0,200);nextHeal=now+Math.clamp(n.getLong("BossHealWait"),0,300);nextLaugh=now+100;}
 }
 public static DamageSource magic(RedMage m){var key=ResourceKey.<DamageType>create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"spell"));return new DamageSource(m.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key),m,m);}
 static boolean legal(LivingEntity t){return t.isAlive()&&!t.isSpectator()&&!(t instanceof Player p&&(p.isCreative()||p.getAbilities().invulnerable));}
 public static boolean teleport(RedMage m,Vec3 v){var l=(ServerLevel)m.level();var p=BlockPos.containing(v);if(!l.hasChunkAt(p)||!l.getWorldBorder().isWithinBounds(p)||l.isOutsideBuildHeight(p))return false;var old=m.position();if(m.randomTeleport(v.x,v.y,v.z,true)){m.setDeltaMovement(Vec3.ZERO);m.fallDistance=0;l.sendParticles(ParticleTypes.PORTAL,old.x,old.y+1,old.z,14,.2,.5,.2,.05);return true;}return false;}
 public static boolean dodge(RedMage m,LivingEntity target){Vec3 toward=target.position().subtract(m.position());Vec3 side=new Vec3(-toward.z,0,toward.x).normalize();if(side.lengthSqr()<.1)side=new Vec3(1,0,0);for(int i=0;i<8;i++){double sign=i%2==0?1:-1;var v=m.position().add(side.scale(sign*(3+i/2))).add(0,1,0);if(teleport(m,v)){m.announce("mina","lok");return true;}}return false;}
 public static int barricade(RedMage m,LivingEntity target){var l=(ServerLevel)m.level();if(!l.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))return 0;var f=Direction.getNearest(target.getX()-m.getX(),0,target.getZ()-m.getZ());if(!f.getAxis().isHorizontal())f=Direction.NORTH;var across=f.getClockWise();var center=m.blockPosition().relative(f,2);int n=0;for(int dx=-1;dx<=1;dx++)for(int dy=0;dy<2;dy++){var p=center.relative(across,dx).above(dy);if(!l.hasChunkAt(p)||!l.getWorldBorder().isWithinBounds(p)||l.isOutsideBuildHeight(p)||!l.getBlockState(p).isAir()||!l.getEntities((Entity)null,new AABB(p),e->e instanceof LivingEntity).isEmpty())continue;l.setBlockAndUpdate(p,MagicMascot.BARRICADE.get().defaultBlockState());l.scheduleTick(p,MagicMascot.BARRICADE.get(),200);n++;}if(n>0)m.announce("den");return n;}
 public static void selfCare(RedMage m){if(m.level().isClientSide||!m.isAlive())return;if(m.isOnFire()||m.hasData(MagicAttachments.ARCANE_BURN)&&m.getData(MagicAttachments.ARCANE_BURN)>0){m.clearFire();ArcaneFire.extinguish(m);m.announce("mina","no","pir");}}
 public static boolean fireProtected(LivingEntity target){return target.hasEffect(MagicContent.FLAME_LIFE)||target.hasEffect(MobEffects.FIRE_RESISTANCE);}
 public static boolean stripFireProtection(RedMage m,LivingEntity target){boolean had=target.removeEffect(MagicContent.FLAME_LIFE);had=target.removeEffect(MobEffects.FIRE_RESISTANCE)||had;if(had){target.setData(MagicAttachments.FLAME_HEAL,0f);ArcaneFire.extinguish(target);m.announce(SpellEffects.human(target)?"tuna":"sila","no","pir","liv");}return had;}
 private static void bolt(RedMage m,LivingEntity t,float amount){t.hurt(magic(m),amount);((ServerLevel)m.level()).sendParticles(ParticleTypes.ENCHANT,t.getX(),t.getY()+1,t.getZ(),12,.2,.3,.2,.04);m.announce(SpellEffects.human(t)?"tuna":"sila","but");}
 public static void attack(RedMage m,LivingEntity t){if(!m.isAlive()||!legal(t))return;int phase=m.combatPhase(),a=m.combat.action++;if(phase==2&&a%3==2){m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,160,0));m.announce("mina","rap");return;}if(!m.hasLineOfSight(t))return;
  if(phase==1){if(a%4==0&&m.distanceToSqr(t)<6.25)m.doHurtTarget(t);else if(a%3==0)barricade(m,t);else m.laugh();return;}
  if(phase==2){if(a%3==0)bolt(m,t,4);else if(a%3==1)barricade(m,t);else{m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,160,0));m.announce("mina","rap");}return;}
  switch(a%6){
   case 0->{if(!fireProtected(t)){ArcaneFire.ignite(t,100,1.5f);m.announce(SpellEffects.human(t)?"tuna":"sila","pir");}else bolt(m,t,5);}
   case 1,5->bolt(m,t,5);
   case 2->{((ServerLevel)m.level()).explode(m,magic(m),new net.minecraft.world.level.ExplosionDamageCalculator(){@Override public boolean shouldDamageEntity(net.minecraft.world.level.Explosion e,Entity victim){return victim==t;}@Override public float getKnockbackMultiplier(Entity victim){return victim==t?1:0;}},t.position().add(0,.5,0),1.05f,false,Level.ExplosionInteraction.NONE);m.announce("pir","tup","dun","por");}
   case 3->{t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,0));t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100,0));m.announce(SpellEffects.human(t)?"tuna":"sila","len","nem");}
   case 4->{if(!stripFireProtection(m,t))bolt(m,t,4);}
  }
 }
 public static void tick(RedMage m){selfCare(m);if(!m.isAlive()||m.throne().isPresent()||m.isNoAi())return;var l=(ServerLevel)m.level();var t=m.getTarget();if(t==null||!legal(t)||m.distanceToSqr(t)>1600){m.setTarget(null);t=l.getEntitiesOfClass(Monster.class,m.getBoundingBox().inflate(16),MageCombat::legal).stream().min(java.util.Comparator.comparingDouble(m::distanceToSqr)).orElse(null);m.setTarget(t);}if(t==null)return;m.updateCombatPhase();int phase=m.combatPhase();var c=m.combat;long now=l.getGameTime();m.getLookControl().setLookAt(t,35,35);
  boolean projectile=!l.getEntitiesOfClass(Projectile.class,m.getBoundingBox().inflate(phase==1?3:5),p->p.getOwner()!=m&&p.getDeltaMovement().lengthSqr()>.01&&p.getDeltaMovement().dot(m.position().subtract(p.position()))>0).isEmpty();
  if(now>=c.nextDodge&&(projectile||phase>1&&m.distanceToSqr(t)<12||!m.hasLineOfSight(t)||phase==1&&now>=c.nextLaugh)){if(dodge(m,t))c.nextDodge=now+(phase==1?75:phase==2?38:24);else c.nextDodge=now+15;}
  if(phase==1){if(now>=c.nextLaugh){m.laugh();c.nextLaugh=now+140;}m.getNavigation().moveTo(t,.65);}
  else {if(m.distanceToSqr(t)>144||!m.hasLineOfSight(t))m.getNavigation().moveTo(t,phase==2?1.05:1.25);else m.getNavigation().stop();}
  if(now>=c.nextBuff){m.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,100,0));m.announce("mina","kon");if(phase>1){m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,160,phase==3?1:0));m.announce("mina","rap");}c.nextBuff=now+180;}
  if(phase==3&&now>=c.nextHeal&&m.getHealth()<20){m.heal(3);m.announce("mina","kor");c.nextHeal=now+240;}
  if(now>=c.nextAction){attack(m,t);c.nextAction=now+(phase==1?80:phase==2?60:36);}
 }
 private MageCombat(){}
}
