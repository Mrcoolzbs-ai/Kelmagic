from pathlib import Path
r=Path('/data/KelMagic');j=r/'src/main/java/dev/kelmagic'
p=j/'server/ArchivistTrades.java';s=p.read_text().replace(' @SubscribeEvent public static void trades',' private static net.minecraft.world.item.ItemStack manuscript(net.minecraft.world.item.Rarity rarity){var s=MagicArchive.ANCIENT.toStack();s.set(net.minecraft.core.component.DataComponents.RARITY,rarity);return s;}\n @SubscribeEvent public static void trades');s=s.replace('t.get(5).add(new VillagerTrades.ItemsForEmeralds(MagicArchive.ANCIENT.get(),24,1,2,30));','t.get(3).add(new VillagerTrades.ItemsForEmeralds(manuscript(net.minecraft.world.item.Rarity.UNCOMMON),16,1,4,10));t.get(4).add(new VillagerTrades.ItemsForEmeralds(manuscript(net.minecraft.world.item.Rarity.RARE),32,1,3,15));t.get(5).add(new VillagerTrades.ItemsForEmeralds(manuscript(net.minecraft.world.item.Rarity.EPIC),48,1,2,30));');p.write_text(s)
p=j/'client/CodexScreen.java';s=p.read_text().replace('search.setHint(Component.literal("Слово..."));','search.setHint(Component.literal("Слово..."));search.setResponder(v->query=v);');p.write_text(s)
p=j/'mixin/ArchivistClothesMixin.java';s=p.read_text().replace('if(kind.equals("profession")','if(!java.util.Set.of("plains","desert","taiga","snow","snowy","savanna","jungle","swamp").contains(kelmagic$biome))kelmagic$biome="plains";if(kind.equals("profession")');p.write_text(s)
(j/'client/TrimAppearance.java').write_text('''package dev.kelmagic.client;
import net.minecraft.world.item.ItemStack;import net.minecraft.core.component.DataComponents;
public final class TrimAppearance {
 public static int color(ItemStack s){var t=s.get(DataComponents.TRIM);if(t==null)return 0xFFFFFFFF;String a=t.material().value().assetName();return 0xFF000000|switch(a){case "quartz"->0xE4DED4;case "iron"->0xD5D5D5;case "netherite"->0x625B60;case "redstone"->0xB33030;case "copper"->0xB66B46;case "gold"->0xDEB648;case "emerald"->0x35B86C;case "diamond"->0x57D1CB;case "lapis"->0x354CAF;case "amethyst"->0xA378D3;default->0xE4DED4;};}
 private TrimAppearance(){}
}''')
p=j/'client/MagicItemColors.java';s=p.read_text().replace('for(var item:MagicContent.ROBE_ITEMS)','for(var item:MagicContent.PLATE_ITEMS)e.register((stack,layer)->layer==2?TrimAppearance.color(stack):0xFFFFFFFF,item.get());\n        for(var item:MagicContent.ROBE_ITEMS)').replace(':0xFFFFFFFF,item.get());',':layer==2?TrimAppearance.color(stack):0xFFFFFFFF,item.get());');p.write_text(s)
p=j/'client/AudioScreens.java';s=p.read_text().replace('e.enqueueWork(()->{net.minecraft.client.renderer.item.ItemProperties.register','e.enqueueWork(()->{for(var item:java.util.stream.Stream.concat(MagicContent.PLATE_ITEMS.stream(),MagicContent.ROBE_ITEMS.stream()).toList())net.minecraft.client.renderer.item.ItemProperties.register(item.get(),ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"trimmed"),(s,l,p,seed)->s.has(net.minecraft.core.component.DataComponents.TRIM)?1:0);net.minecraft.client.renderer.item.ItemProperties.register');p.write_text(s)
# Extend current generator without changing original armor textures/models when not trimmed.
p=r/'tools/generate_revision_1_9.py';s=p.read_text();needle='# Structures: two distinct reading halls';extra='''# Inventory trim overlays: vanilla inventories distinguish material, not the pattern's full model.
save(a/'textures/item/empty_layer.png',Image.new('RGBA',(32,32)))
for prefix in ['arcane','mage']:
 for slot in ['helmet','chestplate','leggings','boots']:
  name=prefix+'_'+slot;im=Image.open(a/f'textures/item/{name}.png').convert('RGBA');mask=im.getchannel('A');trim=Image.new('RGBA',im.size);pixels=trim.load()
  for y in range(1,im.height-1):
   for x in range(1,im.width-1):
    if mask.getpixel((x,y))>0 and any(mask.getpixel((x+dx,y+dy))==0 for dx,dy in [(0,1),(0,-1),(1,0),(-1,0)]):pixels[x,y]=(255,255,255,255)
  save(a/f'textures/item/{name}_trim.png',trim)
  path=a/f'models/item/{name}.json';model=json.loads(path.read_text());model['overrides']=[{'predicate':{'kelmagic:trimmed':1},'model':f'kelmagic:item/{name}_trimmed'}];js(path,model)
  textures=dict(model['textures']);textures.setdefault('layer1','kelmagic:item/empty_layer');textures['layer2']=f'kelmagic:item/{name}_trim';js(a/f'models/item/{name}_trimmed.json',{'parent':'minecraft:item/generated','textures':textures})
# Structures: two distinct reading halls''';assert needle in s;s=s.replace(needle,extra);p.write_text(s)
