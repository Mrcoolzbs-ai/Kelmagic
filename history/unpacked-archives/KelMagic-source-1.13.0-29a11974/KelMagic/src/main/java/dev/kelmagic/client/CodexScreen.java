package dev.kelmagic.client;
import dev.kelmagic.*;
import dev.kelmagic.core.*;
import dev.kelmagic.network.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.network.PacketDistributor;
public final class CodexScreen extends Screen {
 private static final ResourceLocation TEX=ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"textures/gui/codex.png");
 private final Set<String> known;private final List<String> notes;private final boolean offhand;private final String token;
 private int section=0,page=0,x,y;private EditBox note;private String query="";
 private static final List<String> WORDS=new ArrayList<>(KelLexicon.WORDS.keySet());
 private static final List<String> ITEMS=List.of("mana_core","magic_ingot","magic_cloth","scripter","dragon_heart","arcane_helmet","arcane_chestplate","arcane_leggings","arcane_boots","mage_helmet","mage_chestplate","mage_leggings","mage_boots","archivists_codex","cassette","cassette_deck","cassette_player","golden_thread","golden_wool","archivist_table");
 public CodexScreen(CodexOpen p){super(Component.translatable("kelmagic.codex_title"));known=new HashSet<>(p.words());notes=new ArrayList<>(p.notes());while(notes.size()<WORDS.size())notes.add("");offhand=p.offhand();token=p.token();section=Math.clamp(p.section(),0,1);page=Math.clamp(p.page(),0,total()-1);query=p.query();}
 public static void open(CodexOpen p){Minecraft.getInstance().setScreen(new CodexScreen(p));}
 @Override protected void init(){
  x=(width-360)/2;y=(height-216)/2;
  for(int i=0;i<2;i++){final int s=i;addRenderableWidget(style(Button.builder(Component.translatable(new String[]{"kelmagic.codex_words","kelmagic.codex_items"}[i]),b->{section=s;page=0;rebuildWidgets();}).bounds(x+22+i*160,y+16,154,18).build()));}
  addRenderableWidget(style(Button.builder(Component.literal("<"),b->{page=Math.floorMod(page-1,total());rebuildWidgets();}).bounds(x+20,y+188,28,16).build()));
  addRenderableWidget(style(Button.builder(Component.literal(">"),b->{page=(page+1)%total();rebuildWidgets();}).bounds(x+312,y+188,28,16).build()));
  if(section==0){
   var search=new EditBox(font,x+60,y+188,80,16,Component.literal("Найти слово"));search.setMaxLength(40);search.setValue(query);search.setHint(Component.literal("Слово..."));search.setResponder(v->query=v);addRenderableWidget(search);
   addRenderableWidget(style(Button.builder(Component.literal("Найти"),b->{query=search.getValue().strip().toLowerCase(Locale.ROOT);for(int i=0;i<WORDS.size();i++){var w=WORDS.get(i);if(w.equals(query)||KelLexicon.WORDS.get(w).translation().toLowerCase(Locale.ROOT).contains(query)){page=i;rebuildWidgets();return;}}search.setTextColor(0xFFAA3333);}).bounds(x+240,y+188,60,16).build()));
  }
  if(section==0 && known.contains(WORDS.get(page))){
   note=new EditBox(font,x+196,y+119,136,18,Component.literal("Моя интерпретация"));note.setMaxLength(240);note.setBordered(false);note.setTextColor(0xFF493427);note.setValue(notes.get(page));addRenderableWidget(note);
   addRenderableWidget(style(Button.builder(Component.literal("Записать мысль"),b->{notes.set(page,note.getValue());PacketDistributor.sendToServer(new CodexNote(offhand,WORDS.get(page),note.getValue()));b.setMessage(Component.literal("Записано"));}).bounds(x+215,y+159,116,18).build()));
  }
 }
 private Button style(Button original){return new Button(original.getX(),original.getY(),original.getWidth(),original.getHeight(),original.getMessage(),b->{original.onPress();b.setMessage(original.getMessage());},supplier->supplier.get()){
  @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial){
   int xx=getX(),yy=getY(),w=getWidth(),h=getHeight();boolean hover=isHoveredOrFocused();
   g.fill(xx,yy,xx+w,yy+h,0xFF39232A);g.fill(xx+1,yy+1,xx+w-1,yy+h-1,hover?0xFF96654A:0xFF6A4240);
   g.fill(xx+2,yy+1,xx+w-2,yy+2,0xFFD3AC6D);g.fill(xx+2,yy+h-2,xx+w-2,yy+h-1,0xFF3F2725);
   g.drawCenteredString(font,getMessage(),xx+w/2,yy+(h-8)/2,hover?0xFFFFE5B2:0xFFE1CBA7);
  }
 };}
 private int total(){return section==0?WORDS.size():ITEMS.size();}
 private void text(GuiGraphics g,String text,int dx,int dy,int w){g.drawWordWrap(font,Component.literal(text),x+dx,y+dy,w,0x493427);}
 @Override public void render(GuiGraphics g,int mx,int my,float partial){
  renderBackground(g,mx,my,partial);g.blit(TEX,x,y,0,0,360,216,360,216);
  g.drawCenteredString(font,title,x+180,y+3,0xDCC28F);g.drawCenteredString(font,Component.literal((page+1)+" / "+total()),x+180,y+192,0xDCC28F);
  if(section==0){
   String w=WORDS.get(page);boolean has=known.contains(w);
   text(g,has?w:"Нерасшифрованная страница",25,47,135);
   if(has){
    var glyph=Component.literal(GlyphWriting.write(w)).withStyle(Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"kel")));
    g.drawString(font,glyph,x+48,y+67,0x386E78,false);
    text(g,"Значение: "+KelLexicon.WORDS.get(w).translation(),25,91,132);text(g,WordRarity.label(w),25,118,132);
    effectText(g,hasShiftDown()?SpellProfiles.mechanics(w):meaning(w));
    text(g,"Заметка архивариуса",196,49,133);text(g,"То, как я понимаю это слово:",196,73,133);g.fill(x+194,y+115,x+334,y+137,0xFFDBC392);g.fill(x+194,y+137,x+334,y+138,0xFFA88760);
    text(g,"Shift: заметки о применении.",196,141,135);
   }else{
    text(g,"Найдите древнюю рукопись и раскройте её ПКМ. Затем соедините лист со словом и кодекс на столе архивариуса.",25,80,132);
    text(g,"Здесь пока лишь следы чернил. Слова обретают смысл в руках читателя.",196,62,135);
   }
  }else{
   String id=ITEMS.get(page);var item=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,id));
   g.renderItem(new ItemStack(item),x+26,y+49);text(g,item.getDescription().getString(),49,48,112);
   text(g,lore(id),25,81,132);
   text(g,"Схема изготовления",196,49,135);
   drawRecipe(g,id);
   text(g,recipeCaption(id),196,147,135);
  }
  if(section==1&&mx>=x+25&&mx<x+157&&my>=y+81&&my<y+180)g.renderTooltip(font,Component.literal(hasShiftDown()?purpose(ITEMS.get(page)):lore(ITEMS.get(page))),mx,my);
  for(var widget:renderables)widget.render(g,mx,my,partial);
  if(section==0 && known.contains(WORDS.get(page)) && mx>=x+25 && mx<x+157 && my>=y+132 && my<y+180)g.renderTooltip(font,Component.literal(hasShiftDown()?SpellProfiles.mechanics(WORDS.get(page)):meaning(WORDS.get(page))),mx,my);
 }
 private void effectText(GuiGraphics g,String value){var lines=font.split(Component.literal(value),132);for(int i=0;i<Math.min(5,lines.size());i++){if(i==4&&lines.size()>5)g.drawString(font,"… наведите курсор",x+25,y+132+i*9,0x493427,false);else g.drawString(font,lines.get(i),x+25,y+132+i*9,0x493427,false);}}
 private static String meaning(String w){return SpellProfiles.describe(w);}
 private static String lore(String n){return Component.translatable("lore.kelmagic."+n).getString();}

 private static String purpose(String n){return switch(n){case "archivist_table"->"Кодекс и страницы, а также копии книги соединяются только на столе архивариуса.";case "cassette"->"Чистая кассета и записанный скриптер в деке; скриптер расходуется после завершения записи.";case "cassette_deck"->"Запись занимает пять секунд на слово. Скриптер расходуется только после готовой записи.";case "cassette_player"->"8 кассет. ПКМ — воспроизвести, Shift+ПКМ — инвентарь, Shift+колесо — выбор.";case "golden_thread"->"Материал ткани и золотой шерсти, восемь нитей за рецепт.";case "golden_wool"->"Огнестойкий блок; дар взрослому спокойному пиглину.";case "scripter"->"ПКМ: начать запись. Следующее завершённое заклинание фиксируется. ПКМ: сохранить. Shift+ПКМ: применить за половину цены.";case "dragon_heart"->"Однократное улучшение: базовая мана 15, восстановление раз в 5 секунд. Повторное сердце не расходуется.";case "archivists_codex"->"ПКМ открывает книгу. Кодекс + лист слова на столе архивариуса дополняет словарь. Здесь можно записать своё толкование.";case "mana_core"->"Основа материалов и кодекса. Неловкое зелье + ядро в варочной стойке даёт бутылку маны.";case "magic_ingot"->"Материал магических пластин; 8 слитков за один рецепт.";case "magic_cloth"->"Материал окрашиваемых одеяний; 8 отрезов за рецепт.";default->n.startsWith("mage")?"Каждая часть добавляет 2,5 к максимуму маны. Одеяние можно покрасить обычным красителем.":"Каждая часть добавляет 1 к максимуму маны. Защита соответствует железной броне.";};}
 private static String[] pattern(String n){
  if(n.equals("archivist_table"))return new String[]{"BLB","CLC","PPP"};
  if(n.equals("cassette"))return new String[]{"IPI","SRS","STS"};
  if(n.equals("cassette_deck"))return new String[]{"IPI","RCR","IQI"};
  if(n.equals("cassette_player"))return new String[]{"IQI","BCB","MRS"};
  if(n.equals("golden_thread"))return new String[]{"SSS","SGS","SSS"};
  if(n.equals("golden_wool"))return new String[]{"TT ","TT ","   "};
  if(n.equals("mana_core"))return new String[]{"LRL","RDR","LRL"};
  if(n.equals("magic_ingot"))return new String[]{"III","ICI","III"};
  if(n.equals("magic_cloth"))return new String[]{"LSL","SCS","LSL"};
  if(n.equals("scripter"))return new String[]{"IRI","CJC","IRI"};
  if(n.equals("dragon_heart"))return new String[]{"NCA","HES","ACN"};
  if(n.equals("archivists_codex"))return new String[]{"CBF","K  ","   "};
  if(n.endsWith("helmet"))return new String[]{"MMM","M M","   "};
  if(n.endsWith("chestplate"))return new String[]{"M M","MMM","MMM"};
  if(n.endsWith("leggings"))return new String[]{"MMM","M M","M M"};return new String[]{"M M","M M","   "};
 }
 private static String ingredient(String n,char c){
  if(n.equals("archivist_table"))return switch(c){case 'B'->"minecraft:book";case 'L'->"minecraft:lantern";case 'C'->"kelmagic:mana_core";default->"minecraft:lectern";};
  if(n.equals("cassette"))return switch(c){case 'I'->"minecraft:iron_nugget";case 'P'->"minecraft:paper";case 'S'->"minecraft:slime_ball";case 'R'->"minecraft:redstone";default->"minecraft:string";};
  if(n.equals("cassette_deck"))return switch(c){case 'I'->"minecraft:iron_ingot";case 'P'->"minecraft:paper";case 'R'->"minecraft:redstone_block";case 'C'->"kelmagic:cassette";default->"minecraft:quartz";};
  if(n.equals("cassette_player"))return switch(c){case 'I'->"minecraft:iron_ingot";case 'Q'->"minecraft:quartz";case 'B'->"minecraft:stone_button";case 'C'->"kelmagic:mana_core";case 'M'->"minecraft:copper_ingot";case 'R'->"minecraft:redstone_block";default->"minecraft:slime_ball";};
  if(n.equals("golden_thread"))return c=='S'?"minecraft:string":"minecraft:gold_ingot";
  if(n.equals("golden_wool"))return "kelmagic:golden_thread";
  if(c=='M')return "kelmagic:"+(n.startsWith("mage")?"magic_cloth":"magic_ingot");
  if(n.equals("mana_core"))return switch(c){case 'L'->"minecraft:lapis_lazuli";case 'R'->"minecraft:redstone";default->"minecraft:diamond";};
  if(n.equals("magic_ingot"))return c=='I'?"minecraft:iron_ingot":"kelmagic:mana_core";
  if(n.equals("magic_cloth"))return c=='L'?"minecraft:leather":c=='S'?"kelmagic:golden_thread":"kelmagic:mana_core";
  if(n.equals("scripter"))return switch(c){case 'I'->"minecraft:iron_ingot";case 'R'->"minecraft:redstone_block";case 'J'->"minecraft:jukebox";default->"kelmagic:mana_core";};
  if(n.equals("archivists_codex"))return switch(c){case 'C'->"kelmagic:mana_core";case 'B'->"minecraft:book";case 'F'->"minecraft:feather";default->"minecraft:ink_sac";};
  return switch(c){case 'N'->"minecraft:netherite_ingot";case 'C'->"minecraft:end_crystal";case 'A'->"minecraft:amethyst_block";case 'H'->"minecraft:heart_of_the_sea";case 'E'->"minecraft:dragon_egg";default->"minecraft:nether_star";};
 }
 private void drawRecipe(GuiGraphics g,String n){
  var rows=pattern(n);for(int row=0;row<3;row++)for(int col=0;col<3;col++){
   int xx=x+198+col*22,yy=y+73+row*22;g.fill(xx,yy,xx+20,yy+20,0x50634732);char c=rows[row].charAt(col);
   if(c!=' ')g.renderItem(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(ingredient(n,c)))),xx+2,yy+2);
  }
  g.drawString(font,"→",x+269,y+99,0x493427,false);
  var result=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,n)),n.equals("magic_ingot") || n.equals("magic_cloth") || n.equals("golden_thread")?8:1);
  g.fill(x+287,y+95,x+311,y+119,0x50634732);
  g.renderItem(result,x+291,y+99);g.renderItemDecorations(font,result,x+291,y+99);
 }
 private static String recipeCaption(String n){return n.equals("archivists_codex")?"Без формы. Обычные или светящиеся чернила. Выход: 1.":n.equals("magic_ingot") || n.equals("magic_cloth") || n.equals("golden_thread")?"Выход: 8. Верстак 3×3.":"Выход: 1. Верстак 3×3.";}
 @Override public void removed(){if(note!=null&&section==0&&known.contains(WORDS.get(page))){notes.set(page,note.getValue());PacketDistributor.sendToServer(new CodexNote(offhand,WORDS.get(page),note.getValue()));}PacketDistributor.sendToServer(new CodexPosition(offhand,token,section,page,query));super.removed();}
 @Override public boolean isPauseScreen(){return false;}
}
