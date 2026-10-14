package dev.kelmagic;

import java.util.*;
import dev.kelmagic.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.alchemy.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class MagicContent {
    public static final DeferredRegister<ArmorMaterial> MATERIALS=DeferredRegister.create(Registries.ARMOR_MATERIAL,KelMagic.MODID);
    public static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,KelMagic.MODID);
    public static final DeferredRegister<Potion> POTIONS=DeferredRegister.create(Registries.POTION,KelMagic.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,KelMagic.MODID);
    public static final net.neoforged.neoforge.registries.DeferredBlock<dev.kelmagic.block.MagicFireBlock> MAGIC_FIRE=KelMagic.BLOCKS.register("magic_fire",()->new dev.kelmagic.block.MagicFireBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(s->13).replaceable().sound(net.minecraft.world.level.block.SoundType.WOOL)));
    public static final DeferredItem<Item> MANA_CORE=KelMagic.ITEMS.registerSimpleItem("mana_core",new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> MAGIC_INGOT=KelMagic.ITEMS.registerSimpleItem("magic_ingot");
    public static final DeferredItem<Item> MAGIC_CLOTH=KelMagic.ITEMS.registerSimpleItem("magic_cloth");
    public static final DeferredItem<DragonHeartItem> DRAGON_HEART=KelMagic.ITEMS.register("dragon_heart",()->new DragonHeartItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<ScripterItem> SCRIPTER=KelMagic.ITEMS.register("scripter",()->new ScripterItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).component(MagicComponents.SPELL.get(),List.of())));
    public static final DeferredHolder<ArmorMaterial,ArmorMaterial> PLATE=MATERIALS.register("arcane_plate",()->new ArmorMaterial(
        Map.of(ArmorItem.Type.HELMET,2,ArmorItem.Type.CHESTPLATE,6,ArmorItem.Type.LEGGINGS,5,ArmorItem.Type.BOOTS,2),9,SoundEvents.ARMOR_EQUIP_IRON,
        ()->Ingredient.of(MAGIC_INGOT.get()),List.of(new ArmorMaterial.Layer(id("arcane_plate"))),0,0));
    public static final DeferredHolder<ArmorMaterial,ArmorMaterial> ROBE=MATERIALS.register("mage_robes",()->new ArmorMaterial(
        Map.of(ArmorItem.Type.HELMET,1,ArmorItem.Type.CHESTPLATE,3,ArmorItem.Type.LEGGINGS,2,ArmorItem.Type.BOOTS,1),15,SoundEvents.ARMOR_EQUIP_LEATHER,
        ()->Ingredient.of(MAGIC_CLOTH.get()),List.of(new ArmorMaterial.Layer(id("mage_robes"),"",true),new ArmorMaterial.Layer(id("mage_robes"),"_overlay",false)),0,0));
    public static final List<DeferredItem<ManaArmorItem>> PLATE_ITEMS=armor("arcane",PLATE,1,false,15);
    public static final List<DeferredItem<ManaArmorItem>> ROBE_ITEMS=armor("mage",ROBE,2.5,true,5);
    private static List<DeferredItem<ManaArmorItem>> armor(String prefix,net.minecraft.core.Holder<ArmorMaterial> material,double bonus,boolean robe,int durability) {
        List<DeferredItem<ManaArmorItem>> items=new ArrayList<>();
        for(var type:List.of(ArmorItem.Type.HELMET,ArmorItem.Type.CHESTPLATE,ArmorItem.Type.LEGGINGS,ArmorItem.Type.BOOTS)) {
            items.add(KelMagic.ITEMS.register(prefix+"_"+type.getName(),()->new ManaArmorItem(material,type,new Item.Properties().durability(type.getDurability(durability)),bonus,robe)));
        }
        return List.copyOf(items);
    }
    public static final DeferredHolder<MobEffect,MobEffect> MANA_TIDE=EFFECTS.register("mana_tide",()->new FlowEffect());
    public static final DeferredHolder<MobEffect,MobEffect> WINGS=EFFECTS.register("spectral_wings",()->new FlowEffect());
    public static final DeferredHolder<MobEffect,MobEffect> FLAME_LIFE=EFFECTS.register("flame_life",()->new FlameLifeEffect());
    public static final DeferredHolder<MobEffect,MobEffect> FLOW=EFFECTS.register("mana_flow",()->new FlowEffect());
    public static final DeferredHolder<MobEffect,MobEffect> RESTORE=EFFECTS.register("mana_restore",()->new RestoreManaEffect());
    public static final DeferredHolder<Potion,Potion> MANA=POTIONS.register("mana",()->new Potion("mana",new MobEffectInstance(RESTORE,1),new MobEffectInstance(FLOW,1800)));
    public static final DeferredHolder<Potion,Potion> LONG_MANA=POTIONS.register("long_mana",()->new Potion("mana",new MobEffectInstance(RESTORE,1),new MobEffectInstance(FLOW,4800)));
    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> TAB=TABS.register("magic",()->CreativeModeTab.builder()
        .title(net.minecraft.network.chat.Component.translatable("itemGroup.kelmagic"))
        .icon(()->MANA_CORE.toStack()).displayItems((params,out)->{
            out.accept(MagicArchive.CODEX);out.accept(MagicArchive.CREATIVE_CODEX);out.accept(MagicArchive.ANCIENT);MagicArchive.PAGES.values().forEach(out::accept);
            out.accept(MagicLamps.TORCH_ITEM);out.accept(MagicLamps.LANTERN_ITEM);out.accept(MagicStudy.TABLE_ITEM);out.accept(MagicMaterials.GOLDEN_THREAD);out.accept(MagicMaterials.WOOL_ITEM);out.accept(MANA_CORE);out.accept(MAGIC_INGOT);out.accept(MAGIC_CLOTH);
            MagicCopper.ITEMS.values().forEach(out::accept);PLATE_ITEMS.forEach(out::accept);ROBE_ITEMS.forEach(out::accept);out.accept(DRAGON_HEART);out.accept(SCRIPTER);out.accept(MagicAudio.CASSETTE);out.accept(MagicAudio.DECK_ITEM);out.accept(MagicAudio.PLAYER);
            out.accept(PotionContents.createItemStack(Items.POTION,MANA));out.accept(PotionContents.createItemStack(Items.POTION,LONG_MANA));
        }).build());
    private static ResourceLocation id(String n){return ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,n);}
    public static void register(IEventBus bus) {MATERIALS.register(bus);EFFECTS.register(bus);POTIONS.register(bus);TABS.register(bus);MagicComponents.TYPES.register(bus);}
    private MagicContent() {}
    public static class FlowEffect extends MobEffect { public FlowEffect(){super(MobEffectCategory.BENEFICIAL,0x56DCCC);} }
}
