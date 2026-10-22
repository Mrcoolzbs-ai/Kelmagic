package dev.kelmagic.content;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.*;
@EventBusSubscriber(modid="kelmagic")
public final class DreamSleepEvents {
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGH) public static void sleep(CanPlayerSleepEvent e){if(!(e.getState().getBlock() instanceof DreamBedBlock))return;
  // Only lift vanilla dimension/day restrictions, never another mod's denial.
  var why=e.getProblem();if(why!=BedSleepingProblem.NOT_POSSIBLE_HERE&&why!=BedSleepingProblem.NOT_POSSIBLE_NOW)return;if(why!=e.getVanillaProblem())return;
  var p=e.getEntity();var pos=e.getPos();var l=e.getLevel();var s=e.getState();if(!DreamBedBlock.intact(l,pos)||p.isSleeping()||!p.isAlive())return;
  if(p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)>16){e.setProblem(BedSleepingProblem.TOO_FAR_AWAY);return;}
  var foot=pos.relative(s.getValue(DreamBedBlock.FACING).getOpposite());if(!l.getBlockState(pos.above()).getCollisionShape(l,pos.above()).isEmpty()||!l.getBlockState(foot.above()).getCollisionShape(l,foot.above()).isEmpty()){e.setProblem(BedSleepingProblem.OBSTRUCTED);return;}
  if(!p.isCreative()&&!l.getEntitiesOfClass(Monster.class,new AABB(pos).inflate(8,5,8),m->m.isPreventingPlayerRest(p)).isEmpty()){e.setProblem(BedSleepingProblem.NOT_SAFE);return;}
  // Ordinary overworld day still behaves like a bed; timeless dimensions permit a rest at any hour.
  if(why==BedSleepingProblem.NOT_POSSIBLE_NOW&&l.dimensionType().natural())return;e.setProblem(null);
 }
 @SubscribeEvent public static void continuing(CanContinueSleepingEvent e){if(e.getProblem()!=BedSleepingProblem.NOT_POSSIBLE_NOW)return;var p=e.getEntity();if(p.level().dimensionType().natural())return;p.getSleepingPos().ifPresent(pos->{if(p.level().getBlockState(pos).getBlock() instanceof DreamBedBlock&&DreamBedBlock.intact(p.level(),pos))e.setContinueSleeping(true);});}
 private DreamSleepEvents(){}
}
