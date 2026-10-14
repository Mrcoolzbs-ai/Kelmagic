package dev.kelmagic.server;
import dev.kelmagic.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid=KelMagic.MODID)
public final class WoolBarter {
 public static final ResourceKey<LootTable> LOOT=ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"gameplay/golden_wool_barter"));
 public static boolean trade(ServerPlayer p,Piglin pig,ItemStack stack){
  if(!p.isAlive()||p.isSpectator()||!stack.is(MagicMaterials.WOOL_ITEM)||pig.isBaby()||pig.isAggressive()||pig.getBrain().hasMemoryValue(MemoryModuleType.ANGRY_AT)||!pig.getOffhandItem().isEmpty()||pig.getData(MagicAttachments.BARTER_READY)>pig.level().getGameTime()||p.distanceToSqr(pig)>25)return false;
  var l=p.serverLevel();var params=new LootParams.Builder(l).withParameter(LootContextParams.THIS_ENTITY,pig).create(LootContextParamSets.PIGLIN_BARTER);var rewards=l.getServer().reloadableRegistries().getLootTable(LOOT).getRandomItems(params);if(rewards.isEmpty())return false;
  stack.consume(1,p);pig.setData(MagicAttachments.BARTER_READY,l.getGameTime()+100);
  for(var reward:rewards){var item=pig.spawnAtLocation(reward,.4f);if(item!=null){item.setTarget(p.getUUID());item.setPickUpDelay(10);var v=p.position().subtract(pig.position()).normalize().scale(.3);item.setDeltaMovement(v.x,.2,v.z);}}
  pig.swing(net.minecraft.world.InteractionHand.MAIN_HAND);MagicProgress.award(p,"wool_barter");return true;
 }
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void interact(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event){if(event.isCanceled()||!(event.getEntity() instanceof ServerPlayer p)||!(event.getTarget() instanceof Piglin pig)||!event.getItemStack().is(MagicMaterials.WOOL_ITEM))return;if(trade(p,pig,event.getItemStack())){event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);event.setCanceled(true);}}
 private WoolBarter(){}
}
