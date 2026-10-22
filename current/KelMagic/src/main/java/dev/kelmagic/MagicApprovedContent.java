package dev.kelmagic;
import dev.kelmagic.item.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.*;
import java.util.*;
public final class MagicApprovedContent {
 public static final DeferredItem<PickaxeItem> AUTO_SMELTING_PICKAXE=KelMagic.ITEMS.register("ember_pickaxe",()->new PickaxeItem(Tiers.DIAMOND,new Item.Properties().durability(1561)));
 public static final DeferredItem<PickaxeItem> VEIN_PICKAXE=KelMagic.ITEMS.register("vein_pickaxe",()->new PickaxeItem(Tiers.DIAMOND,new Item.Properties().durability(1561)));
 public static final DeferredItem<LargeFieldHoeItem> LARGE_FIELD_HOE=KelMagic.ITEMS.register("large_field_hoe",()->new LargeFieldHoeItem(Tiers.DIAMOND,new Item.Properties().durability(1561)));
 public static final DeferredItem<SurveyorShovelItem> SURVEYOR_SHOVEL=KelMagic.ITEMS.register("surveyor_shovel",()->new SurveyorShovelItem(Tiers.DIAMOND,new Item.Properties().durability(1561)));
 public static final DeferredItem<GardenerSickleItem> GARDENER_SICKLE=KelMagic.ITEMS.register("gardener_sickle",()->new GardenerSickleItem(Tiers.IRON,new Item.Properties().durability(700).attributes(SwordItem.createAttributes(Tiers.IRON,2,-2.2f))));
 public static final DeferredItem<ReturnStoneItem> RETURN_STONE=KelMagic.ITEMS.register("return_stone",()->new ReturnStoneItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
 public static final DeferredItem<SeekerLanternItem> SEEKER_LANTERN=KelMagic.ITEMS.register("seeker_lantern",()->new SeekerLanternItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
 public static final DeferredItem<ArmorItem> GARDENER_BOOTS=KelMagic.ITEMS.register("gardener_boots",()->new ArmorItem(ArmorMaterials.LEATHER,ArmorItem.Type.BOOTS,new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(5))));
 public static final DeferredItem<Item> BUILDER_GLOVES=KelMagic.ITEMS.registerSimpleItem("builder_gloves",new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
 public static final DeferredItem<Item> COLLECTOR_RING=KelMagic.ITEMS.registerSimpleItem("collector_ring",new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
 public static final DeferredItem<Item> LAST_BREATH_PENDANT=KelMagic.ITEMS.registerSimpleItem("last_breath_pendant",new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
 public static final DeferredItem<Item> FADING_HEAT_CLOAK=KelMagic.ITEMS.registerSimpleItem("fading_heat_cloak",new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
 public static final DeferredItem<Item> GUARDIAN_BROOCH=KelMagic.ITEMS.registerSimpleItem("guardian_brooch",new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
 public static final BooleanProperty POWERED=BlockStateProperties.POWERED;public static final IntegerProperty TONE=IntegerProperty.create("tone",0,3);
 public static final DeferredBlock<Block> ECHO_RECEIVER=KelMagic.BLOCKS.register("echo_receiver",()->new Block(BlockBehaviour.Properties.of().strength(1.6f).sound(SoundType.AMETHYST)){{registerDefaultState(stateDefinition.any().setValue(POWERED,false));}@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(POWERED);}@Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,net.minecraft.world.entity.player.Player player,BlockHitResult h){if(!l.isClientSide)player.displayClientMessage(net.minecraft.network.chat.Component.literal("Приёмник ждёт завершённой фразы."),true);return InteractionResult.sidedSuccess(l.isClientSide);}@Override protected void tick(BlockState s,ServerLevel l,BlockPos p,net.minecraft.util.RandomSource r){if(s.getValue(POWERED))l.setBlock(p,s.setValue(POWERED,false),3);}@Override protected boolean isSignalSource(BlockState s){return true;}@Override protected int getSignal(BlockState s,BlockGetter l,BlockPos p,Direction d){return s.getValue(POWERED)?15:0;}});
 public static final DeferredItem<BlockItem> ECHO_RECEIVER_ITEM=KelMagic.ITEMS.registerSimpleBlockItem(ECHO_RECEIVER);
 public static final DeferredBlock<Block> ECHO_ISOLATOR=KelMagic.BLOCKS.register("echo_isolator",()->new Block(BlockBehaviour.Properties.of().strength(1.2f).sound(SoundType.GLASS).noOcclusion()));
 public static final DeferredItem<BlockItem> ECHO_ISOLATOR_ITEM=KelMagic.ITEMS.registerSimpleBlockItem(ECHO_ISOLATOR);
 public static final DeferredBlock<Block> MUSICAL_RESONATOR=KelMagic.BLOCKS.register("musical_resonator",()->new Block(BlockBehaviour.Properties.of().strength(1.8f).sound(SoundType.AMETHYST)){{registerDefaultState(stateDefinition.any().setValue(TONE,0).setValue(POWERED,false));}@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(TONE,POWERED);}@Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,net.minecraft.world.entity.player.Player player,BlockHitResult h){if(player.isShiftKeyDown()){if(!l.isClientSide){int n=(s.getValue(TONE)+1)%4;l.setBlock(p,s.setValue(TONE,n),3);playResonator(l,p,n);player.displayClientMessage(net.minecraft.network.chat.Component.literal("Тембр: "+switch(n){case 0->"арфа";case 1->"колокол";case 2->"флейта";default->"перезвон";}).withStyle(ChatFormatting.DARK_AQUA),true);}return InteractionResult.sidedSuccess(l.isClientSide);}if(!l.isClientSide)playResonator(l,p,s.getValue(TONE));return InteractionResult.sidedSuccess(l.isClientSide);}@Override protected void neighborChanged(BlockState s,Level l,BlockPos p,Block b,BlockPos from,boolean piston){boolean pow=l.hasNeighborSignal(p);if(pow!=s.getValue(POWERED)){if(pow)playResonator(l,p,s.getValue(TONE));l.setBlock(p,s.setValue(POWERED,pow),3);}}});
 public static final DeferredItem<BlockItem> MUSICAL_RESONATOR_ITEM=KelMagic.ITEMS.registerSimpleBlockItem(MUSICAL_RESONATOR);
 private static final ResourceLocation BUILDER_RANGE_ID=ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"builder_gloves_range");private static final Set<UUID> VEINING=new HashSet<>();private static final Map<UUID,Long> HEAT_COOLDOWN=new HashMap<>(),BROOCH_COOLDOWN=new HashMap<>();
 public static void init(){}
 public static void heard(Level l,Vec3 center){var root=BlockPos.containing(center);for(BlockPos p:BlockPos.betweenClosed(root.offset(-16,-8,-16),root.offset(16,8,16))){if(!l.hasChunkAt(p)||!l.getBlockState(p).is(ECHO_RECEIVER.get()))continue;var s=l.getBlockState(p);if(!s.getValue(POWERED)){l.setBlock(p,s.setValue(POWERED,true),3);l.scheduleTick(p,ECHO_RECEIVER.get(),20);l.updateNeighborsAt(p,ECHO_RECEIVER.get());if(l instanceof ServerLevel sl)sl.playSound(null,p,SoundEvents.AMETHYST_BLOCK_RESONATE,SoundSource.BLOCKS,.5f,1.4f);}}}
 public static void playResonator(Level l,BlockPos p,int tone){var s=switch(tone){case 1->SoundEvents.NOTE_BLOCK_BELL.value();case 2->SoundEvents.NOTE_BLOCK_FLUTE.value();case 3->SoundEvents.NOTE_BLOCK_CHIME.value();default->SoundEvents.NOTE_BLOCK_HARP.value();};l.playSound(null,p,s,SoundSource.BLOCKS,.85f,1+tone*.12f);}
 static boolean has(net.minecraft.world.entity.player.Player p,ItemLike item){for(var s:p.getInventory().items)if(s.is(item.asItem()))return true;for(var s:p.getInventory().offhand)if(s.is(item.asItem()))return true;return false;}
 static boolean ore(BlockState s){return BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath().contains("ore")&&s.is(BlockTags.MINEABLE_WITH_PICKAXE);}
 @EventBusSubscriber(modid=KelMagic.MODID)public static final class Events {
  @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p))return;var range=p.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);if(range!=null){if(has(p,BUILDER_GLOVES.get()))range.addOrUpdateTransientModifier(new AttributeModifier(BUILDER_RANGE_ID,1.5,AttributeModifier.Operation.ADD_VALUE));else range.removeModifier(BUILDER_RANGE_ID);}if(has(p,COLLECTOR_RING.get()))for(var item:p.serverLevel().getEntitiesOfClass(ItemEntity.class,p.getBoundingBox().inflate(10),it->it.isAlive()&&p.getUUID().equals(it.getTarget()))){Vec3 v=p.position().add(0,.8,0).subtract(item.position());double d=Math.max(.01,v.length());item.setDeltaMovement(v.scale(Math.min(.55,.12+.9/d)));item.hasImpulse=true;}if(has(p,LAST_BREATH_PENDANT.get())&&p.isUnderWater()&&p.getAirSupply()<p.getMaxAirSupply()&&p.tickCount%80==0)p.setAirSupply(Math.min(p.getMaxAirSupply(),p.getAirSupply()+20));long now=p.serverLevel().getGameTime();if(has(p,FADING_HEAT_CLOAK.get())&&(p.isOnFire()||p.isInLava())&&now>=HEAT_COOLDOWN.getOrDefault(p.getUUID(),0L)){HEAT_COOLDOWN.put(p.getUUID(),now+200);p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,60,0,false,true,true));p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.FIRE_EXTINGUISH,SoundSource.PLAYERS,.5f,1.2f);}}
  @SubscribeEvent public static void trample(BlockEvent.FarmlandTrampleEvent e){if(e.getEntity() instanceof net.minecraft.world.entity.player.Player p&&p.getItemBySlot(EquipmentSlot.FEET).is(GARDENER_BOOTS.get()))e.setCanceled(true);}
  @SubscribeEvent public static void drops(BlockDropsEvent e){if(!(e.getBreaker() instanceof ServerPlayer p))return;for(var entity:e.getDrops())entity.setTarget(p.getUUID());if(!p.getMainHandItem().is(AUTO_SMELTING_PICKAXE.get()))return;float xp=0;for(var entity:e.getDrops()){var stack=entity.getItem();var in=new SingleRecipeInput(stack.copyWithCount(1));var recipe=p.serverLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING,in,p.serverLevel());if(recipe.isEmpty())continue;var cooked=recipe.get().value().assemble(in,p.serverLevel().registryAccess());if(cooked.isEmpty())continue;cooked.setCount(cooked.getCount()*stack.getCount());entity.setItem(cooked);if(recipe.get().value() instanceof AbstractCookingRecipe c)xp+=c.getExperience()*stack.getCount();}if(xp>0)e.setDroppedExperience((int)Math.ceil(xp));}
  @SubscribeEvent public static void vein(BlockEvent.BreakEvent e){if(!(e.getPlayer() instanceof ServerPlayer p)||VEINING.contains(p.getUUID())||p.isShiftKeyDown()||!p.getMainHandItem().is(VEIN_PICKAXE.get())||!ore(e.getState()))return;VEINING.add(p.getUUID());try{var l=p.serverLevel();var start=e.getPos();var first=e.getState().getBlock();var todo=new ArrayDeque<BlockPos>();var seen=new HashSet<BlockPos>();todo.add(start);seen.add(start);while(!todo.isEmpty()&&seen.size()<32){var at=todo.removeFirst();for(Direction d:Direction.values()){var n=at.relative(d);if(!l.hasChunkAt(n)||seen.contains(n)||n.distSqr(start)>64)continue;var s=l.getBlockState(n);if(s.is(first)&&ore(s)){seen.add(n);todo.addLast(n);p.gameMode.destroyBlock(n);if(seen.size()>=32)break;}}}}finally{VEINING.remove(p.getUUID());}}
  @SubscribeEvent public static void hurt(LivingIncomingDamageEvent e){if(!(e.getEntity() instanceof ServerPlayer p)||!has(p,GUARDIAN_BROOCH.get()))return;long now=p.serverLevel().getGameTime();if(now<BROOCH_COOLDOWN.getOrDefault(p.getUUID(),0L)||e.getAmount()<4)return;BROOCH_COOLDOWN.put(p.getUUID(),now+400);e.setAmount(Math.max(0,e.getAmount()-Math.min(6,e.getAmount()*.55f)));e.setInvulnerabilityTicks(10);p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,60,0,false,true,true));p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,120,1,false,true,true));p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.SHIELD_BLOCK,SoundSource.PLAYERS,.8f,.8f);}
  @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)cleanup(p);}@SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)cleanup(p);}static void cleanup(ServerPlayer p){var a=p.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);if(a!=null)a.removeModifier(BUILDER_RANGE_ID);}
 }
 private MagicApprovedContent(){}
}
