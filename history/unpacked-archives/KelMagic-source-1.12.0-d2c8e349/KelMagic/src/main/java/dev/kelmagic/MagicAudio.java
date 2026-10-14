package dev.kelmagic;
import dev.kelmagic.audio.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
public final class MagicAudio {
 public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,KelMagic.MODID);
 public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,KelMagic.MODID);
 public static final DeferredItem<CassetteItem> CASSETTE=KelMagic.ITEMS.register("cassette",()->new CassetteItem(new Item.Properties().stacksTo(1)));
 public static final DeferredBlock<DeckBlock> DECK=KelMagic.BLOCKS.register("cassette_deck",()->new DeckBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(2.5f).sound(net.minecraft.world.level.block.SoundType.METAL)));
 public static final DeferredItem<BlockItem> DECK_ITEM=KelMagic.ITEMS.registerSimpleBlockItem(DECK);
 public static final DeferredItem<TapePlayerItem> PLAYER=KelMagic.ITEMS.register("cassette_player",()->new TapePlayerItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).component(MagicComponents.ACTIVE_TAPE.get(),0).component(net.minecraft.core.component.DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.EMPTY)));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<DeckEntity>> DECK_ENTITY=ENTITIES.register("cassette_deck",()->BlockEntityType.Builder.of(DeckEntity::new,DECK.get()).build(null));
 public static final DeferredHolder<MenuType<?>,MenuType<DeckMenu>> DECK_MENU=MENUS.register("cassette_deck",()->IMenuTypeExtension.create((id,inv,b)->new DeckMenu(id,inv,b.readBlockPos())));
 public static final DeferredHolder<MenuType<?>,MenuType<TapeMenu>> PLAYER_MENU=MENUS.register("cassette_player",()->IMenuTypeExtension.create((id,inv,b)->new TapeMenu(id,inv,b.readBoolean()?net.minecraft.world.InteractionHand.OFF_HAND:net.minecraft.world.InteractionHand.MAIN_HAND)));
 public static void register(IEventBus b){ENTITIES.register(b);MENUS.register(b);}
 private MagicAudio(){}
}
