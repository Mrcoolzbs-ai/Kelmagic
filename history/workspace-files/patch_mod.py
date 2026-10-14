from pathlib import Path
import json
from PIL import Image,ImageOps
r=Path('/data/KelMagic')
def edit(p,old,new):
 p=r/p;s=p.read_text();assert old in s,(p,old);p.write_text(s.replace(old,new))
# Fixed block lifetime; entity fireSeconds is deliberately untouched.
p='src/main/java/dev/kelmagic/block/MagicFireBlock.java'
edit(p,'private static int delay(int step){int total=Config.FIRE_SECONDS.get()*60;','public static final int BLOCK_BURN_TICKS=100;\n private static int delay(int step){int total=BLOCK_BURN_TICKS;')
edit(p,'  int step=s.getValue(AGE)*4+s.getValue(PULSE);','  // Defer portal creation until after the placement protection hook can cancel.\n  if(tryOpenPortal(l,p))return;\n  int step=s.getValue(AGE)*4+s.getValue(PULSE);')
edit(p,' public static boolean lightCampfire', ''' public static boolean tryOpenPortal(Level l,BlockPos p){
  if(l.isClientSide || (l.dimension()!=Level.OVERWORLD && l.dimension()!=Level.NETHER))return false;
  var shape=net.minecraft.world.level.portal.PortalShape.findEmptyPortalShape(l,p,Direction.Axis.X);
  if(shape.isEmpty())return false;
  shape.get().createPortalBlocks();return true;
 }
 public static boolean lightCampfire''')
# PortalShape recognizes tagged fire, not just Blocks.FIRE.
f=r/'src/main/resources/data/minecraft/tags/block/fire.json';f.parent.mkdir(parents=True,exist_ok=True);f.write_text(json.dumps({'replace':False,'values':['kelmagic:magic_fire']},indent=2)+'\n')
# Target the campfire itself (all faces), with identical permission/protection checks.
p='src/main/java/dev/kelmagic/server/SpellEffects.java'
edit(p,'case FIRE_BLOCK -> aim.type==HitResult.Type.BLOCK && placeFire(p,aim.block.getBlockPos().relative(aim.block.getDirection()));','case FIRE_BLOCK -> aim.type==HitResult.Type.BLOCK && igniteBlockHit(p,aim.block);')
edit(p,'    private static boolean placeFire(ServerPlayer p,BlockPos pos) {','''    public static boolean igniteBlockHit(ServerPlayer p,BlockHitResult hit) {
        if(net.minecraft.world.level.block.CampfireBlock.canLight(p.serverLevel().getBlockState(hit.getBlockPos())))
            return placeFire(p,hit.getBlockPos());
        return placeFire(p,hit.getBlockPos().relative(hit.getDirection()));
    }
    private static boolean placeFire(ServerPlayer p,BlockPos pos) {''')
edit(p,' || !level.isEmptyBlock(pos)) return false;\n        var state=dev.kelmagic.MagicContent.MAGIC_FIRE.get().stateAt(level,pos);\n        if(!state.canSurvive(level,pos)) return false;', ''' ) return false;
        var old=level.getBlockState(pos);
        boolean campfire=net.minecraft.world.level.block.CampfireBlock.canLight(old);
        if(!campfire && !level.isEmptyBlock(pos))return false;
        var state=campfire?old.setValue(net.minecraft.world.level.block.CampfireBlock.LIT,true):dev.kelmagic.MagicContent.MAGIC_FIRE.get().stateAt(level,pos);
        if(!campfire && !state.canSurvive(level,pos)) return false;''')
edit(p,'            snapshot.restore(); return false;\n        }\n        return true;', '''            snapshot.restore(); return false;
        }
        if(campfire)level.playSound(null,pos,net.minecraft.sounds.SoundEvents.FLINTANDSTEEL_USE,net.minecraft.sounds.SoundSource.BLOCKS,1,1);
        return true;''')
# Teach vanilla entity-properties loot predicates about arcane fire without giving vanilla fire ticks.
f=r/'src/main/java/dev/kelmagic/mixin/ArcaneFireLootMixin.java';f.write_text('''package dev.kelmagic.mixin;
import dev.kelmagic.server.MagicAttachments;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
/** Vanilla cooked-loot and on-fire predicates recognize the independent magic burn. */
@Mixin(EntityFlagsPredicate.class)
public abstract class ArcaneFireLootMixin {
 @Redirect(method="matches",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;isOnFire()Z"))
 private boolean kelmagic$burningForLoot(Entity e){
  return e.isOnFire() || e instanceof LivingEntity live && live.getData(MagicAttachments.ARCANE_BURN)>0;
 }
}
''')
f=r/'src/main/resources/kelmagic.mixins.json';o=json.loads(f.read_text());o['mixins']=['ArcaneFireLootMixin'];f.write_text(json.dumps(o,indent=2)+'\n')
# Mirror paired front panels, not the entire player or canon glyphs.
for p in ['src/main/java/dev/kelmagic/client/MagicArmorModels.java','tools/generate_stage3_assets.py']:
 edit(p,'CubeListBuilder.create().texOffs(u,v).addBox(', 'CubeListBuilder.create().texOffs(u,v).mirror(name.equals("lapel1") || name.equals("front1")).addBox(')
# Two tabs, artifact lore left / corresponding recipe right.
p='src/main/java/dev/kelmagic/client/CodexScreen.java'
edit(p,'for(int i=0;i<3;i++)','for(int i=0;i<2;i++)')
edit(p,'new String[]{"kelmagic.codex_words","kelmagic.codex_items","kelmagic.codex_recipes"}','new String[]{"kelmagic.codex_words","kelmagic.codex_items"}')
edit(p,'.bounds(x+22+i*106,y+16,101,18)', '.bounds(x+22+i*160,y+16,154,18)')
edit(p,'''   if(section==1){text(g,lore(id),25,81,132);text(g,purpose(id),196,55,135);text(g,"Схема изготовления — вкладка «Рецепты».",196,151,135);}
   else{drawRecipe(g,id);text(g,"Схема верстака",196,49,132);text(g,recipeCaption(id),196,78,132);}''', '''   text(g,lore(id),25,81,132);
   text(g,"Схема изготовления",196,49,135);
   drawRecipe(g,id);
   text(g,recipeCaption(id),196,147,135);''')
edit(p,'int xx=x+40+col*22,yy=y+83+row*22','int xx=x+198+col*22,yy=y+73+row*22')
edit(p,'''  g.renderItem(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,n))),x+64,y+157);''','''  g.drawString(font,"→",x+269,y+99,0x493427,false);
  var result=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,n))),n.equals("magic_ingot") || n.equals("magic_cloth")?8:1);
  g.fill(x+287,y+95,x+311,y+119,0x50634732);
  g.renderItem(result,x+291,y+99);g.renderItemDecorations(font,result,x+291,y+99);''')
# Correct the generated ItemStack constructor expression.
edit(p,'ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,n))),n.equals', 'ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,n)),n.equals')
edit(p,'"Без формы: ядро + книга + перо + чернила. Подходят обычные и светящиеся чернила. Кодекс + лист слова: добавление рукописи."','"Без формы. Обычные или светящиеся чернила. Выход: 1."')
edit(p,'"Выход: 8. Расположение ингредиентов соответствует схеме слева."','"Выход: 8. Верстак 3×3."')
edit(p,'"Выход: 1. Расположение ингредиентов соответствует схеме слева."','"Выход: 1. Верстак 3×3."')
# Reverse playback, preserving every existing frame pixel-for-pixel.
for kind in [0,1]:
 f=r/f'src/main/resources/assets/kelmagic/textures/block/magic_fire_{kind}.png';im=Image.open(f).convert('RGBA');w=im.width;new=Image.new('RGBA',im.size);n=im.height//w
 for i in range(n):new.paste(im.crop((0,i*w,w,(i+1)*w)),(0,(n-1-i)*w))
 new.save(f)
edit('tools/generate_revision15_fire.py','frame*32+y','(15-frame)*32+y')
edit('tools/generate_stage3_assets.py',"from PIL import Image,ImageDraw", "from PIL import Image,ImageDraw,ImageOps")
edit('tools/generate_stage3_assets.py'," save(im,Path(f'item/{n}.png'));textures=", " if n.startswith(('arcane_','mage_')):\n  im=symmetric_icon(im)\n  if robe:save(symmetric_icon(overlay),Path(f'item/{n}_overlay.png'))\n save(im,Path(f'item/{n}.png'));textures=")
edit('tools/generate_stage3_assets.py',"# Detailed new hand pixel-art icons.", '''def symmetric_icon(im):
 # Reuse the existing left-half pixels; do not draw or replace the icon.
 left=im.crop((0,0,im.width//2,im.height));im=im.copy()
 im.paste(ImageOps.mirror(left),(im.width//2,0));return im
# Detailed new hand pixel-art icons.''')
for f in (r/'src/main/resources/assets/kelmagic/textures/item').glob('*.png'):
 if f.stem.startswith(('arcane_','mage_')):
  im=Image.open(f).convert('RGBA');half=im.crop((0,0,16,32));im.paste(ImageOps.mirror(half),(16,0));im.save(f)
# In-game wording only; preserve mod id, language name and dictionary.
for lang in ['ru_ru','en_us']:
 f=r/f'src/main/resources/assets/kelmagic/lang/{lang}.json';o=json.loads(f.read_text())
 if lang=='ru_ru':
  o['death.attack.kelmagic.mana_debt.4']='Магия забрала последние силы %1$s';o['death.attack.kelmagic.arcane_fire']='%1$s сгорел в магическом пламени';o['itemGroup.kelmagic']='Магия'
 else:
  o['death.attack.kelmagic.mana_debt.4']='Magic claimed the last of %1$s';o['death.attack.kelmagic.arcane_fire']='%1$s burned in magical flames';o['itemGroup.kelmagic']='Magic'
 o.pop('kelmagic.codex_recipes',None);f.write_text(json.dumps(o,ensure_ascii=False,indent=2)+'\n')
edit('src/main/templates/META-INF/neoforge.mods.toml','Example mod description.', '''Магия произнесённых слов: собирайте заклинания в обычном чате и направляйте их на себя, существ или мир.
Управляйте маной, создавайте магические материалы, броню и окрашиваемые одеяния. Записывайте заклинания в скриптер, изучайте рукописи с архивариусным кодексом и пробуждайте сердце дракона.
Магическое пламя не сдерживается огнеупорностью, зажигает костры и открывает порталы в Незер.

Word-based magic: compose spells in chat, manage mana, craft magical equipment, record spells and explore manuscripts with the Archivist's Codex.''')
edit('gradle.properties','mod_version=1.5.0','mod_version=1.5.1')
print('Patched 9 requested areas; platform, entity duration and core texture unchanged.')
