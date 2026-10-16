package dev.kelmagic.entity;
import dev.kelmagic.KelMagic;import net.minecraft.world.entity.monster.Monster;import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
/** Hostiles can recognise the mage as prey independently of their usual player target. */
@EventBusSubscriber(modid=KelMagic.MODID)
public final class MageHostility {
 private static final java.util.Set<Monster> INSTALLED=java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
 public static void install(Monster mob){if(mob.level().isClientSide||!INSTALLED.add(mob))return;mob.targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(mob,RedMage.class,10,true,false,m->m.isAlive()&&!m.isSpectator()));}
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(!e.getLevel().isClientSide&&e.getEntity() instanceof Monster m)install(m);}
 private MageHostility(){}
}
