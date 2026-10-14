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
 private int section=0,page=0,x,y;
 private static final String[] TABS={"words","origins","materials","sound","equipment","writing","great"};
 private static final String[] TAB_LABELS={"Слова","Начало","Материалы","Звук","Одеяния","Письменность","Великие"};
 private int panX,panY,detailPage,detailScroll;private String selected="";private boolean dragging,wordReading;private final java.util.Map<Integer,int[]> pans=new java.util.HashMap<>();private EditBox note;private String query="";
 private static final List<String> WORDS=new ArrayList<>(KelLexicon.WORDS.keySet());
 private static final List<String> ITEMS=List.of("mana_core","magic_ingot","magic_cloth","scripter","dragon_heart","arcane_helmet","arcane_chestplate","arcane_leggings","arcane_boots","mage_helmet","mage_chestplate","mage_leggings","mage_boots","archivists_codex","cassette","cassette_deck","cassette_player","golden_thread","golden_wool","archivist_table");
 public CodexScreen(CodexOpen p){super(Component.translatable("kelmagic.codex_title"));known=new HashSet<>(p.words());notes=new ArrayList<>(p.notes());while(notes.size()<WORDS.size())notes.add("");offhand=p.offhand();token=p.token();section=Math.clamp(p.section(),0,6);if(!tabVisible(section))section=1;panX=section==1?-110:0;page=Math.clamp(p.page(),0,total()-1);query=p.query();}
 public static void open(CodexOpen p){Minecraft.getInstance().setScreen(new CodexScreen(p));}
 @Override protected void init(){
  if(section!=0||!wordReading){initResearch();return;}
  x=(width-360)/2;y=(height-216)/2;
  addTabs(x+18,y+16,324);
  addRenderableWidget(style(Button.builder(Component.literal("К знакам"),b->{wordReading=false;rebuildWidgets();}).bounds(x+148,y+188,75,16).build()));
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
 private int total(){return WORDS.size();}
 private void text(GuiGraphics g,String text,int dx,int dy,int w){g.drawWordWrap(font,Component.literal(text),x+dx,y+dy,w,0x493427);}
 @Override public void render(GuiGraphics g,int mx,int my,float partial){
  if(section!=0||!wordReading){renderResearch(g,mx,my,partial);return;}
  renderBackground(g,mx,my,partial);g.blit(TEX,x,y,0,0,360,216,360,216);
  g.drawCenteredString(font,title,x+180,y+3,0xDCC28F);g.drawCenteredString(font,Component.literal((page+1)+" / "+total()),x+180,y+192,0xDCC28F);
  if(section==0){
   String w=WORDS.get(page);boolean has=known.contains(w);
   text(g,has?w:"Нерасшифрованная страница",25,47,135);
   if(has){
    var glyph=Component.literal(GlyphWriting.write(w)).withStyle(Style.EMPTY.withFont(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"kel")));
    g.drawString(font,glyph,x+48,y+67,0x386E78,false);
    text(g,"Значение: "+KelLexicon.WORDS.get(w).translation(),25,91,132);text(g,WordRarity.label(w),25,118,132);
    effectText(g,meaning(w));
    text(g,"Заметка архивариуса",196,49,133);text(g,"То, как я понимаю это слово:",196,73,133);g.fill(x+194,y+115,x+334,y+137,0xFFDBC392);g.fill(x+194,y+137,x+334,y+138,0xFFA88760);
    text(g,"Сохраняется в этой книге.",196,141,135);
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
  if(section==1&&mx>=x+25&&mx<x+157&&my>=y+81&&my<y+180)g.renderTooltip(font,Component.literal(lore(ITEMS.get(page))),mx,my);
  for(var widget:renderables)widget.render(g,mx,my,partial);
  if(section==0 && known.contains(WORDS.get(page)) && mx>=x+25 && mx<x+157 && my>=y+132 && my<y+180)g.renderTooltip(font,Component.literal(meaning(WORDS.get(page))),mx,my);
 }
 private void effectText(GuiGraphics g,String value){var lines=font.split(Component.literal(value),132);for(int i=0;i<Math.min(5,lines.size());i++){if(i==4&&lines.size()>5)g.drawString(font,"… наведите курсор",x+25,y+132+i*9,0x493427,false);else g.drawString(font,lines.get(i),x+25,y+132+i*9,0x493427,false);}}
 private static String meaning(String w){return SpellProfiles.describe(w);}
 private static String lore(String n){return Component.translatable("lore.kelmagic."+n).getString();}

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

 private String nodeName(dev.kelmagic.research.ResearchCatalog.Node n){if(n.id().startsWith("rite:"))return Component.translatable("advancements.kelmagic.rite_"+n.id().substring(5)+".title").getString();return n.id().startsWith("word:")?n.id().substring(5)+" — "+KelLexicon.WORDS.get(n.id().substring(5)).translation():dev.kelmagic.research.Research.name(n);}
 private boolean tabVisible(int index){return index==1||ResearchClient.state.tabs().contains(TABS[index]);}
 public void researchChanged(){if(note!=null&&wordReading&&known.contains(WORDS.get(page)))notes.set(page,note.getValue());if(!tabVisible(section))section=1;rebuildWidgets();}
 private void addTabs(int left,int top,int span){var visible=new ArrayList<Integer>();for(int i=0;i<TABS.length;i++)if(tabVisible(i))visible.add(i);int bw=Math.min(92,span/Math.max(1,visible.size()));for(int j=0;j<visible.size();j++){final int target=visible.get(j);addRenderableWidget(style(Button.builder(Component.literal(font.plainSubstrByWidth(TAB_LABELS[target],bw-8)),b->{if(note!=null&&section==0&&known.contains(WORDS.get(page)))PacketDistributor.sendToServer(new CodexNote(offhand,WORDS.get(page),note.getValue()));note=null;pans.put(section,new int[]{panX,panY});section=target;wordReading=false;page=0;selected="";detailPage=0;detailScroll=0;var old=pans.get(section);panX=old==null?(section==1?-110:0):old[0];panY=old==null?0:old[1];rebuildWidgets();}).bounds(left+j*bw,top,bw-2,18).build()));}}
 private int rw(){return width-24;}private int rh(){return height-30;}private int detailWidth(){return Math.max(120,Math.min(220,rw()*2/5));}private int graphRight(){return width-12-detailWidth()-8;}
 private void initResearch(){x=12;y=12;addTabs(x+8,y+18,rw()-16);if(selected.isEmpty())return;var n=dev.kelmagic.research.ResearchCatalog.BY_ID.get(selected);if(n==null)return;int dx=graphRight()+12;boolean learned=dev.kelmagic.research.ResearchCatalog.learned(ResearchClient.state,n.id());if(learned){String[] labels={"След","Мысль","Рецепт","Опыты"};int bw=(detailWidth()-14)/4;for(int i=0;i<4;i++){final int page=i;addRenderableWidget(style(Button.builder(Component.literal(labels[i]),b->{detailPage=page;detailScroll=0;}).bounds(dx+i*bw,y+66,bw-2,17).build()));}}else if(!n.sealed()){addRenderableWidget(style(Button.builder(Component.literal(ResearchClient.state.started().contains(n.id())?"Продолжить гипотезу":"Записать гипотезу"),b->PacketDistributor.sendToServer(new ResearchAction(offhand,token,n.id()))).bounds(dx,y+rh()-30,detailWidth()-18,18).build()));}}
 private java.util.List<dev.kelmagic.research.ResearchCatalog.Node> graphNodes(){if(section==0){var out=new ArrayList<dev.kelmagic.research.ResearchCatalog.Node>();int i=0;for(String word:WORDS)if(known.contains(word)){String icon=BuiltInRegistries.ITEM.getKey(MagicArchive.PAGES.get(word).get()).getPath();out.add(new dev.kelmagic.research.ResearchCatalog.Node("word:"+word,"words",List.of(),List.of(),(i%6)*95-240,(i/6)*80,icon,true,false,List.of(),"","",""));i++;}return out;}var state=ResearchClient.state;return dev.kelmagic.research.ResearchCatalog.NODES.stream().filter(n->n.in(TABS[section])&&n.parents().stream().allMatch(p->dev.kelmagic.research.ResearchCatalog.learned(state,p))).toList();}
 private int nx(dev.kelmagic.research.ResearchCatalog.Node n){return x+8+(graphRight()-x-16)/2+panX+n.x();}private int ny(dev.kelmagic.research.ResearchCatalog.Node n){return y+75+panY+n.y();}
 private void line(GuiGraphics g,int ax,int ay,int bx,int by,int color){int count=Math.max(1,Math.max(Math.abs(bx-ax),Math.abs(by-ay))/3);int phase=(int)(System.currentTimeMillis()/100%8);for(int i=0;i<=count;i++){if((i+phase)%8>=5)continue;int xx=ax+(bx-ax)*i/count,yy=ay+(by-ay)*i/count;g.fill(xx,yy,xx+2,yy+2,color);}}
 private void renderResearch(GuiGraphics g,int mx,int my,float partial){renderBackground(g,mx,my,partial);g.fill(x,y,x+rw(),y+rh(),0xF02D222C);g.fill(x+2,y+2,x+rw()-2,y+rh()-2,0xFF5E3C3E);g.fill(x+5,y+40,graphRight(),y+rh()-6,0xFF26393B);g.fill(graphRight()+6,y+40,x+rw()-5,y+rh()-6,0xFFE6D1A0);g.drawCenteredString(font,title,x+rw()/2,y+5,0xFFE6D1A0);g.drawString(font,"Потяните фон, чтобы переместить записи",x+10,y+rh()-17,0xFFB9C4BD,false);
  var nodes=graphNodes();var state=ResearchClient.state;g.enableScissor(x+6,y+42,graphRight()-1,y+rh()-21);for(var n:nodes)for(String parent:n.parents()){var from=nodes.stream().filter(v->v.id().equals(parent)||v.icon().equals(parent)).findFirst().orElse(null);if(from!=null)line(g,nx(from),ny(from),nx(n),ny(n),0xFF77B9AD);}
  dev.kelmagic.research.ResearchCatalog.Node hover=null;for(var n:nodes){int xx=nx(n),yy=ny(n);if(xx+20<x+6||xx-20>graphRight()||yy+32<y+42||yy-20>y+rh()-21)continue;boolean learned=n.id().startsWith("word:")||dev.kelmagic.research.ResearchCatalog.learned(state,n.id()),active=state.started().contains(n.id());int color=n.id().equals(selected)?0xFFFFDF83:learned?0xFFBFA66B:active?0xFF62B4B8:0xFF777475;g.fill(xx-17,yy-17,xx+17,yy+17,color);g.fill(xx-15,yy-15,xx+15,yy+15,0xFF3C2B32);var item=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("kelmagic",n.icon()));g.renderItem(new ItemStack(item),xx-8,yy-8);String fullLabel=nodeName(n);int labelWidth=section==4?64:section==0?85:100;String label=font.plainSubstrByWidth(fullLabel,labelWidth);if(!label.equals(fullLabel))label=font.plainSubstrByWidth(fullLabel,labelWidth-8)+"…";g.drawCenteredString(font,label,xx,yy+21,learned?0xFFE4D3AB:0xFFBAC7C3);if(active&&!learned){int done=dev.kelmagic.research.Research.progress(state,n);g.drawCenteredString(font,done+" / "+n.steps().size(),xx,yy+31,0xFF8FD9D1);}if(mx>=xx-18&&mx<=xx+18&&my>=yy-18&&my<=yy+18)hover=n;}
  g.disableScissor();drawDetail(g);for(var widget:renderables)widget.render(g,mx,my,partial);if(hover!=null&&mx<graphRight()&&my>y+42&&my<y+rh()-21)g.renderTooltip(font,Component.literal(nodeName(hover)),mx,my);
 }
 private void paragraph(GuiGraphics g,String value,int left,int top,int span,int bottom){var lines=font.split(Component.literal(value),span);g.enableScissor(left,top,left+span,bottom);for(int i=detailScroll;i<lines.size();i++)g.drawString(font,lines.get(i),left,top+(i-detailScroll)*10,0x493427,false);g.disableScissor();}
 private void drawDetail(GuiGraphics g){int dx=graphRight()+12,span=detailWidth()-18;if(selected.isEmpty()){paragraph(g,"Уцелевшие вещи не объясняют себя. Выберите знак. Лист бумаги позволит записать вопрос, но ответ придётся искать в мире.",dx,y+60,span,y+rh()-15);return;}var n=dev.kelmagic.research.ResearchCatalog.BY_ID.get(selected);if(n==null)return;paragraph(g,nodeName(n),dx,y+47,span,y+66);var state=ResearchClient.state;boolean learned=dev.kelmagic.research.ResearchCatalog.learned(state,n.id());if(!learned){if(n.sealed()){paragraph(g,"Эти строки ещё не открылись. Способ их прочтения пока неизвестен.",dx,y+80,span,y+rh()-40);return;}int step=dev.kelmagic.research.Research.progress(state,n);String hint=step<n.steps().size()?n.steps().get(step).hint():"Гипотеза завершена. Открытие нужно прочитать ПКМ, чтобы сохранить понимание.";paragraph(g,(state.started().contains(n.id())?"Наблюдений: "+step+" / "+n.steps().size()+"\n\n":"Один лист бумаги — начало гипотезы.\n\n")+hint,dx,y+80,span,y+rh()-40);return;}
  if(detailPage==0)paragraph(g,Component.translatable(n.lore()).getString(),dx,y+91,span,y+rh()-15);else if(detailPage==1)paragraph(g,n.thought(),dx,y+91,span,y+rh()-15);else if(detailPage==3)paragraph(g,n.example(),dx,y+91,span,y+rh()-15);else if(n.id().equals("copper_inscriptions")){paragraph(g,"На камнерезе доступны только знаки из кодексов, которые находятся в вашем инвентаре. Окисление и воск не меняют самого слова.",dx,y+91,span,y+rh()-15);}else if(n.id().startsWith("rite:")){paragraph(g,"Великие высказывания не собираются на верстаке. Их строки ещё ожидают своего источника.",dx,y+91,span,y+rh()-15);}else if(n.id().equals("mana_potion")){var awkward=net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION,net.minecraft.world.item.alchemy.Potions.AWKWARD);g.renderItem(awkward,dx,y+100);g.renderItem(MagicContent.MANA_CORE.toStack(),dx+23,y+100);g.drawString(font,"→",dx+46,y+104,0x493427,false);g.renderItem(net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION,MagicContent.MANA),dx+62,y+100);paragraph(g,"Варочная стойка. Красная пыль продлевает сохранённый отклик. Откройте стойку после исследования, чтобы начать работу.",dx,y+127,span,y+rh()-15);}else drawGraphRecipe(g,n.icon(),dx,y+94,span);}
 private void drawGraphRecipe(GuiGraphics g,String id,int left,int top,int span){var mc=Minecraft.getInstance();if(mc.level==null)return;var match=mc.level.getRecipeManager().getRecipes().stream().filter(r->r.value() instanceof net.minecraft.world.item.crafting.CraftingRecipe&&!r.value().getResultItem(mc.level.registryAccess()).isEmpty()&&BuiltInRegistries.ITEM.getKey(r.value().getResultItem(mc.level.registryAccess()).getItem()).equals(ResourceLocation.fromNamespaceAndPath("kelmagic",id))).findFirst().orElse(null);if(match==null){paragraph(g,"Для этой записи пока нет отдельной схемы изготовления.",left,top,span,y+rh()-15);return;}var recipe=(net.minecraft.world.item.crafting.CraftingRecipe)match.value();var ing=recipe.getIngredients();int columns=recipe instanceof net.minecraft.world.item.crafting.ShapedRecipe shaped?shaped.getWidth():3;for(int i=0;i<ing.size()&&i<9;i++){int xx=left+(i%columns)*21,yy=top+(i/columns)*21;g.fill(xx,yy,xx+19,yy+19,0x50634732);var options=ing.get(i).getItems();if(options.length>0)g.renderItem(options[(int)(System.currentTimeMillis()/1300%options.length)],xx+1,yy+1);}g.drawString(font,"→",left+68,top+26,0x493427,false);var result=recipe.getResultItem(mc.level.registryAccess());g.renderItem(result,left+82,top+22);g.renderItemDecorations(font,result,left+82,top+22);paragraph(g,recipeCaption(id),left,top+72,span,y+rh()-15);}
 @Override public boolean mouseClicked(double mx,double my,int button){if(super.mouseClicked(mx,my,button))return true;if((section!=0||!wordReading)&&button==0&&mx>=x+6&&mx<graphRight()&&my>=y+42&&my<y+rh()-21){for(var n:graphNodes())if(Math.abs(mx-nx(n))<=18&&Math.abs(my-ny(n))<=18){if(n.id().startsWith("word:")){wordReading=true;page=WORDS.indexOf(n.id().substring(5));rebuildWidgets();return true;}selected=n.id();detailPage=0;detailScroll=0;if(!n.free()&&!n.sealed()&&!dev.kelmagic.research.ResearchCatalog.learned(ResearchClient.state,n.id())&&!ResearchClient.state.started().contains(n.id()))PacketDistributor.sendToServer(new ResearchAction(offhand,token,n.id()));rebuildWidgets();return true;}dragging=true;return true;}return false;}
 @Override public boolean mouseDragged(double mx,double my,int button,double dx,double dy){if((section!=0||!wordReading)&&dragging&&button==0){panX=Math.clamp(panX+(int)dx,-1200,1200);panY=Math.clamp(panY+(int)dy,-1200,1200);return true;}return super.mouseDragged(mx,my,button,dx,dy);}
 @Override public boolean mouseReleased(double mx,double my,int button){dragging=false;return super.mouseReleased(mx,my,button);}
 @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){if(section!=0||!wordReading){if(mx>graphRight()){detailScroll=Math.max(0,detailScroll-(int)(vertical*3));return true;}panY=Math.clamp(panY+(int)(vertical*22),-1200,1200);return true;}return super.mouseScrolled(mx,my,horizontal,vertical);}

 @Override public void removed(){if(note!=null&&section==0&&known.contains(WORDS.get(page))){notes.set(page,note.getValue());PacketDistributor.sendToServer(new CodexNote(offhand,WORDS.get(page),note.getValue()));}PacketDistributor.sendToServer(new CodexPosition(offhand,token,section,page,query));super.removed();}
 @Override public boolean isPauseScreen(){return false;}
}
