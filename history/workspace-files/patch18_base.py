from pathlib import Path
r=Path('/data/KelMagic');j=r/'src/main/java/dev/kelmagic'
def edit(f,old,new):p=j/f;s=p.read_text();assert old in s,(f,old);p.write_text(s.replace(old,new))
p=r/'gradle.properties';p.write_text(p.read_text().replace('mod_version=1.7.0','mod_version=1.8.0'))
edit(Path('core/ManaMath.java'),'record Spend(int remaining, int missing','record Spend(double remaining, double missing')
edit(Path('core/ManaMath.java'),'spend(int mana, int cost)','spend(double mana, double cost)')
edit(Path('core/ManaMath.java'),'int missing = Math.max','double missing = Math.max')
edit(Path('core/ManaMath.java'),'missing * 2f','(float)(missing * 2)')
edit(Path('core/ManaMath.java'),'if (mana < 0 || cost < 0)','if (!Double.isFinite(mana) || !Double.isFinite(cost) || mana < 0 || cost < 0)')
edit(Path('core/ManaMath.java'),'public static int regenerate(int mana, int max)','public static double regenerate(double mana, double max)')
edit(Path('core/ManaMath.java'),'public static int maximum(boolean dragonUpgrade, int equipmentBonus)','public static double maximum(boolean dragonUpgrade, double equipmentBonus)')
edit(Path('core/SpellSession.java'),'private int syncedMana=-1,syncedMaximum=-1;','private double syncedMana=-1,syncedMaximum=-1;')
edit(Path('core/SpellSession.java'),'needsManaSync(int mana,int maximum)','needsManaSync(double mana,double maximum)')
edit(Path('server/MagicAttachments.java'),'record Mana(int current','record Mana(double current')
edit(Path('server/MagicAttachments.java'),'Codec.intRange(0, 1024).fieldOf("current")','Codec.doubleRange(0, 1024).fieldOf("current")')
edit(Path('server/MagicAttachments.java'),'withCurrent(int amount)','withCurrent(double amount)')
edit(Path('server/MagicAttachments.java'),'new Mana(Math.max(0, Math.min(1024,amount))','new Mana(Double.isFinite(amount)?Math.max(0, Math.min(1024,amount)):0')
edit(Path('server/MagicServer.java'),'public static int maximum','public static double maximum')
edit(Path('server/MagicServer.java'),'public static int equipmentBonus','public static double equipmentBonus')
edit(Path('server/MagicServer.java'),'int bonus=0;','double bonus=0;')
edit(Path('server/MagicServer.java'),'int max=maximum(p)','double max=maximum(p)')
edit(Path('item/ManaArmorItem.java'),'public final int manaBonus','public final double manaBonus')
edit(Path('item/ManaArmorItem.java'),'Properties props,int bonus','Properties props,double bonus')
edit(Path('item/ManaArmorItem.java'),'"kelmagic.equipment_bonus",manaBonus','"kelmagic.equipment_bonus",dev.kelmagic.core.ManaFormat.number(manaBonus)')
edit(Path('MagicContent.java'),'armor("mage",ROBE,2,true','armor("mage",ROBE,2.5,true')
edit(Path('MagicContent.java'),'material,int bonus,boolean robe','material,double bonus,boolean robe')
edit(Path('network/SpellSnapshot.java'),'int revision, int mana, int maximum','int revision, double mana, double maximum')
edit(Path('network/SpellSnapshot.java'),'buf.writeVarInt(p.mana); buf.writeVarInt(p.maximum);','buf.writeDouble(p.mana); buf.writeDouble(p.maximum);')
edit(Path('network/SpellSnapshot.java'),'int mana=buf.readVarInt(), maximum=buf.readVarInt(), kind=buf.readVarInt(), size=buf.readVarInt();','double mana=buf.readDouble(), maximum=buf.readDouble();int kind=buf.readVarInt(), size=buf.readVarInt();')
edit(Path('network/SpellSnapshot.java'),'if(size<0','if(!Double.isFinite(mana)||!Double.isFinite(maximum)||size<0')
edit(Path('network/SpellNetwork.java'),'event.registrar("4")','event.registrar("5")')
edit(Path('client/ManaHud.java'),'width(int maximum,int screen)','width(double maximum,int screen)')
edit(Path('client/ManaHud.java'),'40+Math.max(0,Math.min(maximum,1024)-5)*3','(int)Math.round(40+Math.max(0,Math.min(maximum,1024)-5)*3)')
edit(Path('client/ManaHud.java'),'s.mana()+" / "+s.maximum()','dev.kelmagic.core.ManaFormat.number(s.mana())+" / "+dev.kelmagic.core.ManaFormat.number(s.maximum())')
(j/'core/ManaFormat.java').write_text('''package dev.kelmagic.core;
public final class ManaFormat {public static String number(double x){return x==Math.rint(x)?Long.toString((long)x):String.format(java.util.Locale.ROOT,"%.1f",x);}private ManaFormat(){}}
''')
# The default doubles; old explicit server configuration remains intentional.
edit(Path('Config.java'),'defineInRange("targetRange",24,4,48)','defineInRange("targetRange",48,4,96)')
# Consume writer only on successful completion; input removal/cancel never consumes it.
edit(Path('audio/DeckEntity.java'),'inventory.removeItem(0,1);progress=total=0;','inventory.removeItem(0,1);inventory.removeItem(1,1);progress=total=0;')
# DataSource codec remains backward-readable: old integer current fields decode as a number.
p=r/'src/test/java/dev/kelmagic/core/CoreTests.java';s=p.read_text().replace('int mana = 5','double mana = 5');p.write_text(s)
p=j/'gametest/KelGameTests.java';s=p.read_text().replace('maximum(p)==13,"four robes give +8 mana"','maximum(p)==15,"four robes give +10 mana"').replace('maximum(p)==23,"dragon heart','maximum(p)==25,"dragon heart')
s=s.replace('d.inventory.getItem(1)==writer,"cassette consumed, scripter unchanged"','d.inventory.getItem(1).isEmpty(),"cassette and writer consumed on successful completion"').replace('!reload.inventory.getItem(1).isEmpty(),"writer remains after reload"','reload.inventory.getItem(1).isEmpty(),"writer consumed after resumed success"');p.write_text(s)
# New materials register through the existing block/item registries.
(j/'MagicMaterials.java').write_text('''package dev.kelmagic;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.*;
public final class MagicMaterials {
 public static final DeferredItem<Item> GOLDEN_THREAD=KelMagic.ITEMS.registerSimpleItem("golden_thread");
 public static final DeferredBlock<Block> GOLDEN_WOOL=KelMagic.BLOCKS.register("golden_wool",()->new Block(BlockBehaviour.Properties.of().strength(4f,12f).sound(SoundType.WOOL)));
 public static final DeferredItem<BlockItem> WOOL_ITEM=KelMagic.ITEMS.register("golden_wool",()->new BlockItem(GOLDEN_WOOL.get(),new Item.Properties().fireResistant()));
 public static void init(){}
 private MagicMaterials(){}
}
''')
edit(Path('KelMagic.java'),'MagicContent.register(modEventBus);','MagicMaterials.init();MagicContent.register(modEventBus);')
edit(Path('MagicContent.java'),'out.accept(MANA_CORE);','out.accept(MagicMaterials.GOLDEN_THREAD);out.accept(MagicMaterials.WOOL_ITEM);out.accept(MANA_CORE);')
# Pattern identifier uses the registered item id, not untrusted client imagery.
edit(Path('MagicComponents.java'),'    private MagicComponents()', '''    public static final DeferredHolder<DataComponentType<?>,DataComponentType<String>> TAPE_PATTERN=TYPES.register("tape_pattern",()->DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    private MagicComponents()''')
edit(Path('MagicArchive.java'),' public static final DeferredItem<CodexItem> CREATIVE_CODEX=', ''' public static final DeferredHolder<RecipeSerializer<?>,SimpleCraftingRecipeSerializer<dev.kelmagic.audio.DecorateTapeRecipe>> DECORATE=RECIPES.register("decorate_tape",()->new SimpleCraftingRecipeSerializer<>(dev.kelmagic.audio.DecorateTapeRecipe::new));
 public static final DeferredItem<CodexItem> CREATIVE_CODEX=''')
(j/'audio/DecorateTapeRecipe.java').write_text('''package dev.kelmagic.audio;
import dev.kelmagic.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
public final class DecorateTapeRecipe extends CustomRecipe {
 public DecorateTapeRecipe(CraftingBookCategory c){super(c);}
 private record Inputs(ItemStack tape,DyeItem dye,BannerPatternItem pattern){}
 private Inputs scan(CraftingInput in){ItemStack tape=ItemStack.EMPTY;DyeItem dye=null;BannerPatternItem pattern=null;boolean slime=false;
 for(int i=0;i<in.size();i++){var s=in.getItem(i);if(s.isEmpty())continue;
 if(CassetteItem.recorded(s)&&tape.isEmpty())tape=s;
 else if(s.is(Items.SLIME_BALL)&&!slime)slime=true;
 else if(s.getItem() instanceof DyeItem d&&dye==null)dye=d;
 else if(s.getItem() instanceof BannerPatternItem b&&pattern==null)pattern=b;
 else return null;
 }return !tape.isEmpty()&&slime&&(dye!=null||pattern!=null)?new Inputs(tape,dye,pattern):null;}
 @Override public boolean matches(CraftingInput in,Level l){return scan(in)!=null;}
 @Override public ItemStack assemble(CraftingInput in,HolderLookup.Provider regs){var x=scan(in);if(x==null)return ItemStack.EMPTY;var s=x.tape.copyWithCount(1);
 if(x.dye!=null)s.set(DataComponents.DYED_COLOR,new DyedItemColor(x.dye.getDyeColor().getTextureDiffuseColor(),false));
 if(x.pattern!=null){String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(x.pattern).toString();s.set(MagicComponents.TAPE_PATTERN.get(),id);}return s;}
 @Override public NonNullList<ItemStack> getRemainingItems(CraftingInput in){var out=NonNullList.withSize(in.size(),ItemStack.EMPTY);if(scan(in)!=null)for(int i=0;i<in.size();i++)if(in.getItem(i).getItem() instanceof BannerPatternItem)out.set(i,in.getItem(i).copyWithCount(1));return out;}
 @Override public boolean canCraftInDimensions(int w,int h){return w*h>=3;}
 @Override public RecipeSerializer<?> getSerializer(){return MagicArchive.DECORATE.get();}
}
''')
edit(Path('audio/CassetteItem.java'),'@Override public void appendHoverText', '''public static int patternIndex(ItemStack s){String id=s.getOrDefault(MagicComponents.TAPE_PATTERN.get(),"");if(id.isEmpty())return 0;return switch(id){case "minecraft:flower_banner_pattern"->1;case "minecraft:creeper_banner_pattern"->2;case "minecraft:skull_banner_pattern"->3;case "minecraft:mojang_banner_pattern"->4;case "minecraft:globe_banner_pattern"->5;case "minecraft:piglin_banner_pattern"->6;case "minecraft:flow_banner_pattern"->7;case "minecraft:guster_banner_pattern"->8;default->9;};}
 @Override public void appendHoverText''')
edit(Path('audio/CassetteItem.java'),'var w=words(s);t.add', '''String icon=s.getOrDefault(MagicComponents.TAPE_PATTERN.get(),"");if(!icon.isEmpty()){var parsed=net.minecraft.resources.ResourceLocation.tryParse(icon);if(parsed!=null)t.add(Component.translatable("kelmagic.tape.pattern",net.minecraft.core.registries.BuiltInRegistries.ITEM.get(parsed).getDescription()));}
 var w=words(s);t.add''')
edit(Path('client/MagicItemColors.java'),'for(var item:MagicContent.ROBE_ITEMS)', '''e.register((stack,layer)->layer==0?net.minecraft.world.item.component.DyedItemColor.getOrDefault(stack,0x443347)|0xFF000000:0xFFFFFFFF,MagicAudio.CASSETTE.get());
        for(var item:MagicContent.ROBE_ITEMS)''')
edit(Path('client/AudioScreens.java'),'e.enqueueWork(()->net.minecraft.client.renderer.item.ItemProperties.register','e.enqueueWork(()->{net.minecraft.client.renderer.item.ItemProperties.register')
edit(Path('client/AudioScreens.java'),'(s,l,p,seed)->CassetteItem.recorded(s)?1:0));','''(s,l,p,seed)->CassetteItem.recorded(s)?1:0);
 net.minecraft.client.renderer.item.ItemProperties.register(MagicAudio.CASSETTE.get(),ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"pattern"),(s,l,p,seed)->CassetteItem.patternIndex(s));});''')
print('1.8 base, fractional mana and decoration recipe written')
