from pathlib import Path
exec(Path('/data/patch19.py').read_text().split("p=r/'gradle.properties'")[0])
put('MagicLamps.java','''package dev.kelmagic;
import net.neoforged.neoforge.registries.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.state.BlockBehaviour;import net.minecraft.core.particles.ParticleTypes;
public final class MagicLamps {
 public static final DeferredBlock<TorchBlock> TORCH=KelMagic.BLOCKS.register("magic_torch",()->new TorchBlock(ParticleTypes.END_ROD,BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(s->14).noLootTable().sound(SoundType.WOOD)){
 public void animateTick(net.minecraft.world.level.block.state.BlockState s,net.minecraft.world.level.Level l,net.minecraft.core.BlockPos p,net.minecraft.util.RandomSource r){l.addParticle(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.76f,.60f,.96f),.7f),p.getX()+.5,p.getY()+.7,p.getZ()+.5,0,.005,0);}});
 public static final DeferredBlock<Block> LANTERN=KelMagic.BLOCKS.register("magic_lantern",()->new Block(BlockBehaviour.Properties.of().strength(.5f).lightLevel(s->15).noLootTable().sound(SoundType.LANTERN)){
 public void animateTick(net.minecraft.world.level.block.state.BlockState s,net.minecraft.world.level.Level l,net.minecraft.core.BlockPos p,net.minecraft.util.RandomSource r){if(r.nextInt(4)==0)l.addParticle(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.76f,.60f,.96f),.6f),p.getX()+.5,p.getY()+.8,p.getZ()+.5,0,.01,0);}});
 public static void init(){}private MagicLamps(){}
}''')
sub('server/WorldSpells.java','return BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(s)).defaultBlockState();','if(s.equals("torch"))return MagicLamps.TORCH.get().defaultBlockState();if(s.equals("sea_lantern"))return MagicLamps.LANTERN.get().defaultBlockState();return BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(s)).defaultBlockState();')
sub('server/WorldSpells.java','Blocks.TORCH.defaultBlockState()','MagicLamps.TORCH.get().defaultBlockState()')
sub('server/WorldSpells.java','Math.min(6,limit)','scaled(3,plan.scale(),12)')
sub('MagicMaterials.java','new BlockItem(GOLDEN_WOOL.get(),new Item.Properties().fireResistant())','new WoolItem(GOLDEN_WOOL.get(),new Item.Properties().fireResistant())')
sub('MagicMaterials.java',' public static void init(){}',' public static final class WoolItem extends BlockItem {public WoolItem(Block b,Item.Properties p){super(b,p);}public boolean isPiglinCurrency(ItemStack s){return true;}}\n public static void init(){}')
sub('server/MagicAttachments.java','    private MagicAttachments() {}','    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Boolean>> GOLDEN_EXCHANGE=TYPES.register("golden_exchange",()->AttachmentType.builder(()->false).build());\n    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Long>> MANA_BOOST_END=TYPES.register("mana_boost_end",()->AttachmentType.builder(()->0L).serialize(Codec.LONG).build());\n    private MagicAttachments() {}')
put('server/WoolBarter.java','''package dev.kelmagic.server;
import dev.kelmagic.*;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.entity.monster.piglin.Piglin;import net.minecraft.world.entity.ai.memory.MemoryModuleType;import net.minecraft.world.item.ItemStack;import net.minecraft.world.level.storage.loot.*;import net.minecraft.world.level.storage.loot.parameters.*;import net.minecraft.core.registries.Registries;import net.minecraft.resources.*;import net.neoforged.bus.api.*;import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid=KelMagic.MODID)
public final class WoolBarter {
 public static final ResourceKey<LootTable> LOOT=ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"gameplay/golden_wool_barter"));
 public static boolean trade(ServerPlayer p,Piglin pig,ItemStack stack){if(!p.isAlive()||p.isSpectator()||!stack.is(MagicMaterials.WOOL_ITEM)||pig.isBaby()||pig.isAggressive()||pig.getBrain().hasMemoryValue(MemoryModuleType.ANGRY_AT)||pig.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM)||!pig.getOffhandItem().isEmpty()||p.distanceToSqr(pig)>25)return false;pig.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,stack.copyWithCount(1));stack.consume(1,p);pig.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM,true,120);pig.getNavigation().stop();pig.swing(net.minecraft.world.InteractionHand.OFF_HAND);return true;}
 public static java.util.List<ItemStack> rewards(Piglin pig){var l=(net.minecraft.server.level.ServerLevel)pig.level();var params=new LootParams.Builder(l).withParameter(LootContextParams.THIS_ENTITY,pig).create(LootContextParamSets.PIGLIN_BARTER);var rewards=l.getServer().reloadableRegistries().getLootTable(LOOT).getRandomItems(params);pig.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).filter(p->p instanceof ServerPlayer).ifPresent(p->MagicProgress.award((ServerPlayer)p,"wool_barter"));return rewards;}
 @SubscribeEvent(priority=EventPriority.LOWEST)public static void interact(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract e){if(!e.isCanceled()&&e.getEntity() instanceof ServerPlayer p&&e.getTarget() instanceof Piglin pig&&trade(p,pig,e.getItemStack())){e.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);e.setCanceled(true);}}
}''')
put('mixin/PiglinWoolMixin.java','''package dev.kelmagic.mixin;
import dev.kelmagic.*;import dev.kelmagic.server.*;import net.minecraft.world.entity.monster.piglin.*;import net.minecraft.world.item.ItemStack;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(PiglinAi.class)
public abstract class PiglinWoolMixin {
 @Inject(method="stopHoldingOffHandItem",at=@At("HEAD"))private static void kelmagic$currency(Piglin p,boolean barter,CallbackInfo ci){p.setData(MagicAttachments.GOLDEN_EXCHANGE,p.getOffhandItem().is(MagicMaterials.WOOL_ITEM));}
 @Inject(method="getBarterResponseItems",at=@At("HEAD"),cancellable=true)private static void kelmagic$rewards(Piglin p,CallbackInfoReturnable<java.util.List<ItemStack>> ci){if(p.getData(MagicAttachments.GOLDEN_EXCHANGE))ci.setReturnValue(WoolBarter.rewards(p));}
 @Inject(method="stopHoldingOffHandItem",at=@At("RETURN"))private static void kelmagic$reset(Piglin p,boolean barter,CallbackInfo ci){p.setData(MagicAttachments.GOLDEN_EXCHANGE,false);}
}''')
p=r/'src/main/resources/kelmagic.mixins.json';import json;v=json.loads(p.read_text());v['mixins'].append('PiglinWoolMixin');p.write_text(json.dumps(v,indent=2))
p=r/'src/main/resources/data/kelmagic/loot_table/gameplay/golden_wool_barter.json';v=json.loads(p.read_text());v['pools'][0]['entries']=[x for x in v['pools'][0]['entries'] if x['name']!='kelmagic:golden_thread'];p.write_text(json.dumps(v,indent=2))
p=r/'src/main/resources/data/minecraft/tags/item/piglin_loved.json';p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps({'replace':False,'values':['kelmagic:golden_wool']}))
