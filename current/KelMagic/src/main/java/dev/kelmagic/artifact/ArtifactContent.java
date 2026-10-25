package dev.kelmagic.artifact;
import dev.kelmagic.*;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
public final class ArtifactContent {
    public static final Map<String,DeferredItem<? extends Item>> ITEMS=new LinkedHashMap<>();
    public static final Map<String,String> SLOTS=new LinkedHashMap<>();
    public static final List<DeferredBlock<ArtifactBlock>> BLOCKS=new ArrayList<>();
    public static final Map<String,List<DeferredItem<ArtifactArmorItem>>> ARMOR=new LinkedHashMap<>();
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,KelMagic.MODID);
    public static final DeferredItem<Item> CAUTIOUS=curio("cautious_word_ring","ring"), FAR=curio("far_address_pendant","necklace"), DEEP=curio("deep_well_ring","ring"), RESERVE=curio("spare_heart_medallion","necklace"), CALM=curio("calm_pulse_bracelet","bracelet"), EVEN=curio("even_flow_amulet","necklace"), WITNESS=curio("witness_earring","kelmagic_earring"), SAMPLE=curio("obedient_sample_gloves","hands"), SILENT=curio("silent_step_clasp","charm"), ROOT=curio("root_hearing_brooch","charm"), FOREST=curio("unbroken_forest_belt","belt"), DEW=curio("dusk_dew_ring","ring"), ARRIVAL=curio("unnoticed_arrival_cloak","back");
    public static final DeferredItem<Item> ECHO=tool("reverse_echo_bracelet"), CHALK=tool("wayback_chalk"), SECATEURS=tool("stopped_season_secateurs"), WATER=tool("living_water_can"), BOTTLE=tool("quiet_flame_bottle"), BRIDGE_STAFF=tool("hanging_path_staff");
    public static final DeferredItem<Item> RAPIER=weapon("precise_name_rapier",4,-1.9f), HAMMER=weapon("mute_verdict_hammer",8,-3.3f), SPEAR=weapon("short_threshold_spear",5,-2.7f), WHIP=weapon("living_fiber_whip",2,-2.1f), SHIELD=special("alien_phrase_shield"), DAGGER=weapon("unspoken_dagger",2,-1.5f), MACE=weapon("heavy_thought_mace",7,-3.1f), OATH=weapon("shared_oath_sword",4,-2.4f), FAN=special("reflected_path_fan"), ORB=special("still_volley_orb");
    public static final DeferredBlock<ArtifactBlock> BOWL_SMALL=block("mana_cup",ArtifactBlock.Kind.BOWL,32),BOWL=block("mana_bowl",ArtifactBlock.Kind.BOWL,128),BOWL_DEEP=block("mana_basin",ArtifactBlock.Kind.BOWL,512),ACCESS=block("admission_obelisk",ArtifactBlock.Kind.ACCESS,0),WARD=block("forbidden_threshold_base",ArtifactBlock.Kind.WARD,0),CRYSTAL=block("delayed_sound_crystal",ArtifactBlock.Kind.CRYSTAL,0),STRIKER=block("crystal_striker",ArtifactBlock.Kind.STRIKER,0),NIGHT=block("night_shadow_catcher",ArtifactBlock.Kind.NIGHT,64),GROVE=block("grove_mana_bowl",ArtifactBlock.Kind.GROVE,0),MUTE=block("mute_sky_obelisk",ArtifactBlock.Kind.MUTE,128),BRIDGE=block("fleeting_path",ArtifactBlock.Kind.BRIDGE,0),ROOT_WALL=block("fleeting_root_wall",ArtifactBlock.Kind.ROOT_WALL,0);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ArtifactBlockEntity>> ENTITY=ENTITIES.register("artifact_device",()->BlockEntityType.Builder.of(ArtifactBlockEntity::new,BLOCKS.stream().map(DeferredBlock::get).toArray(Block[]::new)).build(null));
    public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
    static {
        for(String s:List.of("artifact_chime","mana_transfer","crystal_strike","spell_absorb","root_bind","silence_fall","threshold_lock","dream_guard","path_weave"))SOUNDS.put(s,MagicSounds.SOUNDS.register(s,()->SoundEvent.createVariableRangeEvent(id(s))));
        armor("deep_source",true,4,8,Map.of(ArmorItem.Type.HELMET,2,ArmorItem.Type.CHESTPLATE,4,ArmorItem.Type.LEGGINGS,3,ArmorItem.Type.BOOTS,1));
        armor("steady_anchor",false,0,24,Map.of(ArmorItem.Type.HELMET,3,ArmorItem.Type.CHESTPLATE,7,ArmorItem.Type.LEGGINGS,6,ArmorItem.Type.BOOTS,3));
        armor("grove_keeper",true,1.5,14,Map.of(ArmorItem.Type.HELMET,2,ArmorItem.Type.CHESTPLATE,5,ArmorItem.Type.LEGGINGS,4,ArmorItem.Type.BOOTS,2));
        armor("strict_negation",false,0,22,Map.of(ArmorItem.Type.HELMET,3,ArmorItem.Type.CHESTPLATE,7,ArmorItem.Type.LEGGINGS,6,ArmorItem.Type.BOOTS,3));
    }
    private static DeferredItem<Item> curio(String id,String slot){SLOTS.put(id,slot.startsWith("kelmagic_")?slot:"kelmagic_"+slot);return add(id,()->new ArtifactItem(id,new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));}
    private static DeferredItem<Item> tool(String id){return add(id,()->new ArtifactItem(id,new Item.Properties().stacksTo(1).durability(512).rarity(Rarity.RARE)));}
    private static DeferredItem<Item> special(String id){return add(id,()->new ArtifactItem(id,new Item.Properties().durability(768).rarity(Rarity.RARE)));}
    private static DeferredItem<Item> weapon(String id,int damage,float speed){return add(id,()->new ArtifactWeapon(id,new Item.Properties().durability(1024).rarity(Rarity.RARE).attributes(SwordItem.createAttributes(Tiers.DIAMOND,damage,speed))));}
    private static DeferredItem<Item> add(String id,java.util.function.Supplier<Item> supplier){var v=KelMagic.ITEMS.register(id,supplier);ITEMS.put(id,v);return v;}
    private static DeferredBlock<ArtifactBlock> block(String id,ArtifactBlock.Kind kind,int capacity){var v=KelMagic.BLOCKS.register(id,()->new ArtifactBlock(kind,capacity,BlockBehaviour.Properties.of().strength(kind==ArtifactBlock.Kind.BRIDGE?-1:3,kind==ArtifactBlock.Kind.BRIDGE?3600000:10).sound(SoundType.AMETHYST).noOcclusion().lightLevel(s->kind==ArtifactBlock.Kind.CRYSTAL?9:kind==ArtifactBlock.Kind.BRIDGE?7:4)));BLOCKS.add(v);if(kind!=ArtifactBlock.Kind.BRIDGE&&kind!=ArtifactBlock.Kind.ROOT_WALL){var item=KelMagic.ITEMS.registerSimpleBlockItem(v);ITEMS.put(id,item);}return v;}
    private static void armor(String name,boolean robe,double mana,int durability,Map<ArmorItem.Type,Integer> defence){var mat=MagicContent.MATERIALS.register(name,()->new ArmorMaterial(defence,18,robe?SoundEvents.ARMOR_EQUIP_LEATHER:SoundEvents.ARMOR_EQUIP_IRON,()->Ingredient.of(robe?MagicContent.MAGIC_CLOTH.get():MagicContent.MAGIC_INGOT.get()),List.of(new ArmorMaterial.Layer(id(name))),robe?0:2,robe?0:.08f));var list=new ArrayList<DeferredItem<ArtifactArmorItem>>();for(var type:List.of(ArmorItem.Type.HELMET,ArmorItem.Type.CHESTPLATE,ArmorItem.Type.LEGGINGS,ArmorItem.Type.BOOTS)){String id=name+"_"+type.getName();var it=KelMagic.ITEMS.register(id,()->new ArtifactArmorItem(name,mat,type,new Item.Properties().durability(type.getDurability(durability)).rarity(Rarity.RARE),mana,robe));list.add(it);ITEMS.put(id,it);}ARMOR.put(name,List.copyOf(list));}
    public static ResourceLocation id(String id){return ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,id);}
    public static void init(IEventBus bus){ArtifactSpellBolt.TYPE.getId();ENTITIES.register(bus);ArtifactAccessories.init(bus);}
    private ArtifactContent(){}
}
