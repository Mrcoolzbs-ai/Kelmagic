package dev.kelmagic.client;

import dev.kelmagic.KelMagic;
import dev.kelmagic.MagicComponents;
import dev.kelmagic.archive.ArchiveTableEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.*;
import java.util.*;

/** Crosshair input projects onto the physical page plane; all navigation is client-local. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class TableBookClient {
 private static final Map<BlockPos,TableBookView> VIEWS=new HashMap<>();
 private static Object level;
 private static TableBookView pressed;
 private static GLFWKeyCallback oldKey,keyCallback;
 private static GLFWCharModsCallback oldChar,charCallback;
 public record PageHit(TableBookView view,double x,double y,double distance){}
 public static TableBookView view(ArchiveTableEntity table){
  var existing=VIEWS.get(table.getBlockPos());String token=table.mountedCodex().getOrDefault(MagicComponents.CODEX_TOKEN.get(),"");
  if(existing!=null&&!existing.token.equals(token)){existing.close();VIEWS.remove(table.getBlockPos());existing=null;}
  if(existing==null){existing=new TableBookView(table);VIEWS.put(table.getBlockPos().immutable(),existing);}return existing;
 }
 private static void clear(){for(var v:VIEWS.values())v.close();VIEWS.clear();pressed=null;}
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){clear();level=null;}
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();if(mc.level!=level){clear();level=mc.level;}
  if(mc.level==null||mc.player==null)return;
  installKeyboard();
  // Only a small nearby cube; far-away rendered tables do not allocate page textures.
  if(mc.player.tickCount%5==0)for(var pos:BlockPos.betweenClosed(mc.player.blockPosition().offset(-3,-3,-3),mc.player.blockPosition().offset(3,2,3)))
   if(mc.level.getBlockEntity(pos) instanceof ArchiveTableEntity t&&t.hasCodex())view(t);
  var it=VIEWS.entrySet().iterator();while(it.hasNext()){
   var entry=it.next();var pos=entry.getKey();var v=entry.getValue();
   if(mc.player.position().distanceToSqr(center(pos))>100||!(mc.level.getBlockEntity(pos) instanceof ArchiveTableEntity t)||!t.hasCodex()){
    if(pressed==v)pressed=null;v.close();it.remove();continue;
   }
   v.syncBook(t.mountedCodex());
   boolean near=mc.level.players().stream().anyMatch(p->p.isAlive()&&p.position().distanceToSqr(center(pos))<=4);
   v.tick(near);if(mc.screen!=null)v.active().setFocused(null);
  }
 }
 @SubscribeEvent public static void frame(RenderFrameEvent.Pre event){
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;
  var hit=mc.screen==null?hit():null;float partial=event.getPartialTick().getGameTimeDeltaPartialTick(false);
  for(var v:VIEWS.values())if(v.open>.98f&&mc.player.position().distanceToSqr(center(v.pos))<=9){
   boolean hovered=hit!=null&&hit.view==v;
   if(hovered)v.hover(hit.x,hit.y);else if(pressed==v){v.release();pressed=null;}
   v.renderTexture(hovered?(int)hit.x:-1000,hovered?(int)hit.y:-1000,partial);
  }
 }
 private static Vec3 center(BlockPos pos){var mc=Minecraft.getInstance();return dev.kelmagic.archive.TableOrientation.bookCenter(pos,mc.level.getBlockState(pos));}
 public static PageHit hit(){
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return null;
  Vec3 eye=mc.player.getEyePosition(),direction=mc.player.getViewVector(1);
  PageHit closest=null;
  for(var v:VIEWS.values()){
   if(v.open<.99f||mc.player.position().distanceToSqr(center(v.pos))>4)continue;
   var state=mc.level.getBlockState(v.pos);var localEye=dev.kelmagic.archive.TableOrientation.blockToLocal(state,eye.subtract(v.pos.getX(),v.pos.getY(),v.pos.getZ()));
   var localDirection=dev.kelmagic.archive.TableOrientation.blockToLocal(state,eye.add(direction).subtract(v.pos.getX(),v.pos.getY(),v.pos.getZ())).subtract(localEye);
   var pageEye=dev.kelmagic.archive.TableOrientation.localToPage(localEye);
   var pageDirection=dev.kelmagic.archive.TableOrientation.localToPage(localEye.add(localDirection)).subtract(pageEye);
   if(pageDirection.y>=-.0001)continue;
   double d=(.001-pageEye.y)/pageDirection.y;
   if(d<0||d>mc.player.blockInteractionRange()||closest!=null&&d>=closest.distance)continue;
   Vec3 point=eye.add(direction.scale(d));
   var localPoint=pageEye.add(pageDirection.scale(d));
   double x=localPoint.x/TableBookView.PIXEL+TableBookView.WIDTH/2.0;
   double y=localPoint.z/TableBookView.PIXEL+TableBookView.HEIGHT/2.0;
   if(x<0||x>=TableBookView.WIDTH||y<0||y>=TableBookView.HEIGHT)continue;
   var wall=mc.level.clip(new ClipContext(eye,point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,mc.player));
   if(wall.getType()!=HitResult.Type.MISS&&!wall.getBlockPos().equals(v.pos))continue;
   closest=new PageHit(v,x,y,d);
  }
  return closest;
 }
 @SubscribeEvent public static void mouse(InputEvent.MouseButton.Pre e){
  var mc=Minecraft.getInstance();if(e.getButton()!=GLFW.GLFW_MOUSE_BUTTON_RIGHT)return;
  if(e.getAction()==GLFW.GLFW_RELEASE&&pressed!=null){pressed.release();pressed=null;e.setCanceled(true);return;}
  if(mc.screen!=null||mc.player==null||mc.player.isShiftKeyDown()||e.getAction()!=GLFW.GLFW_PRESS)return;
  var hit=hit();if(hit==null)return;e.setCanceled(true);pressed=hit.view;pressed.press(hit.x,hit.y);if(pressed.editing())KeyMapping.releaseAll();
 }
 @SubscribeEvent public static void interaction(InputEvent.InteractionKeyMappingTriggered e){
  var mc=Minecraft.getInstance();if(mc.screen!=null||mc.player==null||mc.player.isShiftKeyDown()||!e.isUseItem())return;
  var hit=hit();if(hit!=null){e.setCanceled(true);e.setSwingHand(false);hit.view.press(hit.x,hit.y);hit.view.release();}
 }
 @SubscribeEvent public static void scroll(InputEvent.MouseScrollingEvent e){
  var mc=Minecraft.getInstance();if(mc.screen!=null||mc.player==null||mc.player.isShiftKeyDown())return;
  var hit=hit();if(hit!=null){e.setCanceled(true);hit.view.scroll(hit.x,hit.y,e.getScrollDeltaX(),e.getScrollDeltaY());}
 }
 private static TableBookView editor(){
  var mc=Minecraft.getInstance();if(mc.screen!=null||mc.player==null)return null;
  for(var v:VIEWS.values())if(v.editing()&&v.open>.98f&&mc.player.position().distanceToSqr(center(v.pos))<=4)return v;return null;
 }
 private static void installKeyboard(){
  if(keyCallback!=null)return;long window=Minecraft.getInstance().getWindow().getWindow();
  keyCallback=GLFWKeyCallback.create((w,key,scan,action,mods)->{
   var editor=editor();if(editor!=null){if(action!=GLFW.GLFW_RELEASE){if(key==GLFW.GLFW_KEY_ESCAPE||key==GLFW.GLFW_KEY_ENTER){editor.active().setFocused(null);KeyMapping.releaseAll();}else editor.key(key,scan,mods);}return;}
   if(oldKey!=null)oldKey.invoke(w,key,scan,action,mods);
  });oldKey=GLFW.glfwSetKeyCallback(window,keyCallback);
  charCallback=GLFWCharModsCallback.create((w,code,mods)->{var editor=editor();if(editor!=null)editor.character(code,mods);else if(oldChar!=null)oldChar.invoke(w,code,mods);});
  oldChar=GLFW.glfwSetCharModsCallback(window,charCallback);
 }
 private TableBookClient(){}
}
