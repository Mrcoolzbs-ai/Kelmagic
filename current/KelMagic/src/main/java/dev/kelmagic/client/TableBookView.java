package dev.kelmagic.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.kelmagic.MagicComponents;
import dev.kelmagic.archive.ArchiveTableEntity;
import dev.kelmagic.item.CodexItem;
import dev.kelmagic.network.*;
import dev.kelmagic.research.ResearchState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;
import java.util.List;

/** One local player's private navigation and render target, never a shared block state. */
public final class TableBookView implements AutoCloseable {
 public static final int WIDTH=416,HEIGHT=224,SCALE=3;
 public static final double PIXEL=.0021,BOOK_Y=dev.kelmagic.archive.TableOrientation.BOOK_Y,BOOK_Z=dev.kelmagic.archive.TableOrientation.BOOK_Z;
 public final BlockPos pos;
 public final String token;
 public float open,previousOpen;
 private final CodexScreen codex;
 private Screen active;
 private TextureTarget target;
 private ResourceLocation texture;
 private final ByteBufferBuilder bytes=new ByteBufferBuilder(262144);
 private final MultiBufferSource.BufferSource buffers=MultiBufferSource.immediate(bytes);
 private ResearchState lastResearch;
 private long flipStart;
 private int flipDirection=1,flipCount=1;
 private Runnable pending;
 private String pendingResearch;
 private double lastX,lastY;
 private boolean dragging;
 private boolean ready;
 public TableBookView(ArchiveTableEntity table){
  pos=table.getBlockPos().immutable();ItemStack book=table.mountedCodex();
  token=book.getOrDefault(MagicComponents.CODEX_TOKEN.get(),"");
  codex=new CodexScreen(new CodexOpen(CodexItem.knownWords(book),book.getOrDefault(MagicComponents.CODEX_NOTES.get(),List.of()),false,1,0,"",token,0,true,book.is(dev.kelmagic.MagicArchive.CREATIVE_CODEX)),this);
  active=codex;active.init(Minecraft.getInstance(),WIDTH,HEIGHT);lastResearch=ResearchClient.state;
 }
 public void syncBook(ItemStack book){codex.updateTableBook(book);}
 public void tick(boolean near){previousOpen=open;open=Math.clamp(open+(near?.2f:-.2f),0,1);if(open==0)release();}
 public float openness(float partial){return previousOpen+(open-previousOpen)*partial;}
 public boolean ready(){return ready;}
 public Screen active(){return active;}
 public void showCodex(CodexScreen screen){active=screen;}
 public ResourceLocation texture(){return texture;}
 public void turn(boolean many,int direction,Runnable apply){if(pending!=null)return;release();flipCount=many?5:1;flipDirection=direction<0?-1:1;flipStart=System.nanoTime();pending=apply;Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN,1));}
 public float flipProgress(){if(pending==null)return -1;return Math.min(1,(System.nanoTime()-flipStart)/(flipCount==1?180_000_000f:420_000_000f));}
 public int flipCount(){return flipCount;}
 public int flipDirection(){return flipDirection;}
 public void openEntry(CodexScreen parent,dev.kelmagic.research.ResearchCatalog.Node node){turn(true,1,()->{active=new ResearchEntryScreen(parent,node);active.init(Minecraft.getInstance(),WIDTH,HEIGHT);});}
 public void back(CodexScreen parent){turn(true,-1,()->active=parent);}
 public void begin(String id){pendingResearch=id;PacketDistributor.sendToServer(new TableResearchAction(pos,token,id));}
 public boolean editing(){return active.getFocused() instanceof EditBox;}
 public void key(int key,int scan,int mods){if(editing())active.keyPressed(key,scan,mods);}
 public void character(int codepoint,int mods){if(editing())for(char c:Character.toChars(codepoint))active.charTyped(c,mods);}
 public void press(double x,double y){if(pending!=null)return;lastX=x;lastY=y;if(active!=codex&&codex.clickTableBookmark(x,y))return;active.mouseClicked(x,y,0);dragging=true;}
 public void hover(double x,double y){if(dragging){active.mouseDragged(x,y,0,x-lastX,y-lastY);lastX=x;lastY=y;}}
 public void release(){if(dragging)active.mouseReleased(lastX,lastY,0);dragging=false;}
 public void scroll(double x,double y,double dx,double dy){if(pending==null)active.mouseScrolled(x,y,dx,dy);}
 public void renderTexture(int mouseX,int mouseY,float partial){
  var mc=Minecraft.getInstance();if(target==null){
   target=new TextureTarget(WIDTH*SCALE,HEIGHT*SCALE,true,Minecraft.ON_OSX);target.setClearColor(0,0,0,0);
   texture=ResourceLocation.fromNamespaceAndPath("kelmagic","table_codex/"+pos.asLong());
   mc.getTextureManager().register(texture,new TargetTexture(target.getColorTextureId()));
  }
  if(pending!=null&&flipProgress()>=1){var apply=pending;pending=null;apply.run();}
  if(lastResearch!=ResearchClient.state){lastResearch=ResearchClient.state;codex.researchChanged();if(active instanceof ResearchEntryScreen entry)entry.researchChanged();if(pendingResearch!=null&&ResearchClient.state.started().contains(pendingResearch)){var node=dev.kelmagic.research.ResearchCatalog.BY_ID.get(pendingResearch);pendingResearch=null;if(node!=null)openEntry(codex,node);}}
  var projection=new Matrix4f(RenderSystem.getProjectionMatrix());var sorting=RenderSystem.getVertexSorting();
  var modelView=RenderSystem.getModelViewStack();modelView.pushMatrix();
  try{
   target.clear(Minecraft.ON_OSX);target.bindWrite(true);
   RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,WIDTH,HEIGHT,0,1000,21000),VertexSorting.ORTHOGRAPHIC_Z);
   modelView.identity().translate(0,0,-11000);RenderSystem.applyModelViewMatrix();RenderSystem.setShaderColor(1,1,1,1);
   if(pending!=null){mouseX=-1000;mouseY=-1000;}
   var gui=new GuiGraphics(mc,buffers);active.render(gui,mouseX,mouseY,partial);if(active!=codex)codex.renderTableBookmarks(gui,mouseX,mouseY,partial);gui.flush();ready=true;
  }finally{
   RenderSystem.disableScissor();modelView.popMatrix();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(projection,sorting);mc.getMainRenderTarget().bindWrite(true);
  }
 }
 public static void tooltip(GuiGraphics gui,net.minecraft.client.gui.Font font,List<net.minecraft.network.chat.Component> text,int x,int y){
  var lines=new java.util.ArrayList<net.minecraft.util.FormattedCharSequence>();for(var line:text)lines.addAll(font.split(line,220));
  gui.renderTooltip(font,lines,(sw,sh,mx,my,w,h)->new org.joml.Vector2i(Math.clamp(mx+8,8,Math.max(8,WIDTH-w-8)),Math.clamp(my+8,8,Math.max(8,HEIGHT-h-8))),x,y);
 }
 @Override public void close(){release();if(target!=null){Minecraft.getInstance().getTextureManager().release(texture);target.destroyBuffers();target=null;}bytes.close();}
 private static final class TargetTexture extends AbstractTexture {
  TargetTexture(int id){this.id=id;}
  public void load(ResourceManager resources){}
  @Override public void releaseId(){id=-1;}
  public static void tooltip(GuiGraphics gui,net.minecraft.client.gui.Font font,List<net.minecraft.network.chat.Component> text,int x,int y){
  var lines=new java.util.ArrayList<net.minecraft.util.FormattedCharSequence>();for(var line:text)lines.addAll(font.split(line,220));
  gui.renderTooltip(font,lines,(sw,sh,mx,my,w,h)->new org.joml.Vector2i(Math.clamp(mx+8,8,Math.max(8,WIDTH-w-8)),Math.clamp(my+8,8,Math.max(8,HEIGHT-h-8))),x,y);
 }
 @Override public void close(){} // FBO owns the GL allocation, not TextureManager.
 }
}
