package dev.kelmagic.client;
import dev.kelmagic.*;
import dev.kelmagic.archive.ArchiveTableEntity;
import dev.kelmagic.core.KelLexicon;
import dev.kelmagic.network.CodexOpen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import java.nio.file.*;
import java.util.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="kelmagic",value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class QaR4Search {
 static int frame,checks,warmup;static boolean started;static TableBookView view;static CodexScreen table,hand;static final Path OUT=Path.of(System.getProperty("kelmagic.qaOutput","qa-r4"));
 static void ok(boolean b,String text){checks++;if(!b)throw new AssertionError("R4 "+text);}
 static Object field(Object o,String n){try{var f=CodexScreen.class.getDeclaredField(n);f.setAccessible(true);return f.get(o);}catch(Exception e){throw new RuntimeException(e);}}
 static void call(CodexScreen c,String name,int value){try{var m=CodexScreen.class.getDeclaredMethod(name,int.class);m.setAccessible(true);m.invoke(c,value);}catch(Exception e){throw new RuntimeException(e);}}
 static long buttons(CodexScreen c,String text){return c.children().stream().filter(w->w instanceof Button b&&b.getMessage().getString().equals(text)).count();}
 static void checkTable(){ok(table.children().stream().noneMatch(w->w instanceof EditBox),"table has no text inputs");ok(buttons(table,"Найти")==0,"table has no search button");ok(buttons(table,"‹")==1&&buttons(table,"›")==1,"page arrows retained");ok(buttons(table,"А–Я")+buttons(table,"Редкость")==1,"sort retained");ok(buttons(table,"Все слова")+buttons(table,"Известные")==1,"known filter retained");}
 static final class Preview extends Screen {
  final CodexScreen child;
  Preview(CodexScreen c){super(Component.literal("R4 QA"));child=c;}
  public void render(GuiGraphics g,int x,int y,float p){g.fill(0,0,width,height,0xFF202020);child.render(g,-1000,-1000,p);}
  public boolean isPauseScreen(){return false;}
 }
 @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
  if(!Boolean.getBoolean("kelmagic.r4QA"))return;var m=Minecraft.getInstance();
  if(m.getOverlay()!=null)return;
  if(!started){if(!(m.screen instanceof TitleScreen)||++warmup<30)return;started=true;m.options.guiScale().set(2);m.resizeDisplay();m.options.enableVsync().set(false);m.options.framerateLimit().set(60);try{Files.createDirectories(OUT);}catch(Exception e){throw new RuntimeException(e);}
   var entity=new ArchiveTableEntity(BlockPos.ZERO,MagicStudy.TABLE.get().defaultBlockState());entity.mount(MagicArchive.CREATIVE_CODEX.toStack());view=new TableBookView(entity);table=(CodexScreen)view.active();call(table,"selectSectionNow",0);checkTable();
   int x=(int)field(table,"x"),y=(int)field(table,"y");ok(!table.mouseClicked(x+70,y+195,0),"former input hotspot inactive");ok(!view.editing(),"table input not captured");ok(!table.mouseClicked(x+260,y+195,0),"former search button hotspot inactive");
   int page=(int)field(table,"page");call(table,"turnWordNow",1);ok((int)field(table,"page")!=page,"page turn retained");checkTable();call(table,"selectSectionNow",1);call(table,"selectSectionNow",0);checkTable();
   hand=new CodexScreen(new CodexOpen(new ArrayList<>(KelLexicon.WORDS.keySet()),List.of("QA note"),false,0,0,"","",0,false));hand.init(m,m.getWindow().getGuiScaledWidth(),m.getWindow().getGuiScaledHeight());call(hand,"selectSectionNow",0);
   ok(hand.children().stream().filter(w->w instanceof EditBox).count()==2,"handheld search and note retained");ok(buttons(hand,"Найти")==1,"handheld search button retained");ok(buttons(hand,"Записать мысль")==1,"handheld notes retained");
   m.setScreen(new Preview(table));System.out.println("R4_SEARCH_PASS checks="+checks+" table=noSearch handheld=search+notes arrows/sort/filter/rebuild/hotspots=passed");return;
  }
  frame++;
  if(frame==20){view.renderTexture(-1000,-1000,0);ok(view.ready(),"real table framebuffer rendered");Screenshot.grab(OUT.toFile(),"table-words-no-search.png",m.getMainRenderTarget(),c->{});}
  if(frame==35)m.setScreen(new Preview(hand));
  if(frame==55)Screenshot.grab(OUT.toFile(),"handheld-words-search-retained.png",m.getMainRenderTarget(),c->{});
  if(frame==75){view.close();System.out.println("R4_QA_COMPLETE checks="+checks+" realCodexWidgets+framebuffer softwareGL noWorld");m.stop();}
 }
}
