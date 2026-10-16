package dev.kelmagic.client.face;
import dev.kelmagic.KelMagic;
import dev.kelmagic.entity.RedMage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.*;
/** Client presentation only. One target and pupil-transform writer per mage.
 * Loaded-entity scans at 4 Hz; <=8 visibility rays/scan. No global structure/chunk searches.
 * Server head-look goals, blink and light compositor keep their existing ownership. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class MageGazeStates {
 public enum Kind {
  REST(0,0,0,0),MOB(35,8,1.6,4),PLAYER(60,12,2,5),HOSTILE(80,14,1.4,4),POINT(90,64,2,6),SUN(90,128,2,6),MOON(90,128,2,6),BOOK(100,4,0,1000);
  public final int priority;public final double range,minHold,maxHold;
  Kind(int p,double r,double min,double max){priority=p;range=r;minHold=min;maxHold=max;}
 }
 public record Reading(Kind kind,String label,int entityId,Vec3 target,float leftX,float leftY,float rightX,float rightY,float size,boolean withinSockets,double holdSeconds){}
 private static final WeakHashMap<RedMage,Entry> STATES=new WeakHashMap<>();
 private static final class Entry {
  final GazeController motion;final PupilSizeController pupil=new PupilSizeController();
  Kind kind=Kind.REST;Entity target;Vec3 selected;String label="rest";double selectedUntil,chosenAt,lastVisible;Kind selectedKind=Kind.POINT;Entity selectedEntity;
  int nextScan,lastTick=-1;Vec3 headOrigin,forward,bookPoint,bookCenter;double bookStart=-1;Reading reading=new Reading(Kind.REST,"rest",-1,null,0,0,0,0,1,true,0);
  Entry(RedMage e){motion=new GazeController(e.getUUID().getMostSignificantBits()^e.getUUID().getLeastSignificantBits());}
 }
 private static Entry entry(RedMage e){return STATES.computeIfAbsent(e,Entry::new);}
 private static double now(RedMage e){return e.tickCount/20.0;}
 /** Already-selected POI: block, structure anchor or arbitrary object point. Never searches structures. */
 public static void selectPoint(RedMage e,Vec3 point,String label,double holdSeconds){
  Objects.requireNonNull(point);if(!Double.isFinite(point.x+point.y+point.z)||!Double.isFinite(holdSeconds)||holdSeconds<=0)throw new IllegalArgumentException("point/hold");
  Entry v=entry(e);v.selected=point;v.selectedEntity=null;v.selectedKind=Kind.POINT;v.selectedUntil=now(e)+holdSeconds;v.chosenAt=now(e);v.label=Objects.requireNonNull(label);v.nextScan=0;
 }
 public static void selectBlock(RedMage e,BlockPos p,double seconds){selectPoint(e,p.getCenter(),"block "+p.toShortString(),seconds);}
 public static void selectStructure(RedMage e,BlockPos anchor,String name,double seconds){selectPoint(e,anchor.getCenter(),"structure: "+name,seconds);}
 public static void selectEntity(RedMage e,LivingEntity entity,double seconds){
  if(entity==e||!Double.isFinite(seconds)||seconds<=0)throw new IllegalArgumentException("entity/hold");Entry v=entry(e);v.selectedEntity=Objects.requireNonNull(entity);v.selected=null;v.selectedUntil=now(e)+seconds;v.chosenAt=now(e);v.lastVisible=now(e);v.selectedKind=kind(entity);v.label=entity.getName().getString();v.nextScan=0;
 }
 public static void selectCelestial(RedMage e,boolean moon,double seconds){
  if(!Double.isFinite(seconds)||seconds<=0)throw new IllegalArgumentException("hold");Entry v=entry(e);v.selected=null;v.selectedEntity=null;v.selectedKind=moon?Kind.MOON:Kind.SUN;v.selectedUntil=now(e)+seconds;v.chosenAt=now(e);v.label=moon?"moon":"sun";v.nextScan=0;
 }
 public static void clearSelection(RedMage e){Entry v=entry(e);v.selectedUntil=-1;v.selected=null;v.selectedEntity=null;v.nextScan=0;}
 public static void pupil(RedMage e,PupilSizeController.State state,double seconds,PupilSizeController.Intent intent){entry(e).pupil.target(now(e),state,seconds,intent);}
 public static Reading reading(RedMage e){return entry(e).reading;}
 private static Kind kind(Entity e){return e instanceof Player?Kind.PLAYER:e instanceof Enemy?Kind.HOSTILE:Kind.MOB;}
 private static Vec3 origin(RedMage e,Entry v){return v.headOrigin!=null?v.headOrigin:e.getEyePosition();}
 private static Vec3 forward(RedMage e,Entry v){return v.forward!=null?v.forward:e.getViewVector(1);}
 private static boolean inFront(RedMage e,Entry v,Vec3 p){Vec3 d=p.subtract(origin(e,v));return d.lengthSqr()>.04&&forward(e,v).dot(d.normalize())>.36;}
 private static BlockHitResult ray(RedMage e,Vec3 from,Vec3 to){
  if(!e.level().hasChunkAt(BlockPos.containing(to)))return null;
  Vec3 delta=to.subtract(from);int n=Math.max(1,(int)Math.ceil(delta.length()/4));
  for(int i=0;i<=n;i++)if(!e.level().hasChunkAt(BlockPos.containing(from.add(delta.scale((double)i/n)))))return null;
  return e.level().clip(new ClipContext(from,to,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,e));
 }
 private static boolean visible(RedMage e,Vec3 from,Vec3 to){var hit=ray(e,from,to);return hit!=null&&hit.getType()==HitResult.Type.MISS;}
 private static boolean pointVisible(RedMage e,Vec3 from,Vec3 to){var hit=ray(e,from,to);return hit!=null&&(hit.getType()==HitResult.Type.MISS||hit.getBlockPos().equals(BlockPos.containing(to)));}
 private static boolean eligible(RedMage e,Entry v,Entity t){return t!=null&&t!=e&&t.isAlive()&&!t.isRemoved()&&!t.isInvisible()&&(!(t instanceof Player p)||!p.isSpectator())&&t.level()==e.level()&&t.distanceToSqr(e)<=kind(t).range*kind(t).range&&inFront(e,v,t.getEyePosition());}
 private static void choose(Entry v,Entity t,Kind k,double tNow){if(v.target!=t||v.kind!=k){v.chosenAt=tNow;v.lastVisible=tNow;}v.target=t;v.kind=k;if(t!=null)v.label=t.getName().getString();}
 private static void scan(RedMage e,Entry v,double t){
  v.nextScan=e.tickCount+5;
  boolean current=eligible(e,v,v.target)&&visible(e,origin(e,v),v.target.getEyePosition());if(current)v.lastVisible=t;
  var list=e.level().getEntitiesOfClass(LivingEntity.class,e.getBoundingBox().inflate(14),a->a!=e&&(a instanceof Mob||a instanceof Player)&&!(a instanceof RedMage)&&eligible(e,v,a));
  list.sort(Comparator.<LivingEntity>comparingInt(a->-kind(a).priority).thenComparingDouble(a->a.distanceToSqr(e)).thenComparingInt(Entity::getId));
  LivingEntity best=null;int rays=0;
  for(var a:list){if(++rays>7)break;if(visible(e,origin(e,v),a.getEyePosition())){if(a==v.target&&current&&t-v.chosenAt>=v.kind.maxHold)continue;best=a;break;}}
  if(current&&t-v.chosenAt<v.kind.minHold)return;
  if(best!=null&&(!current||kind(best).priority>v.kind.priority||(t-v.chosenAt>=v.kind.maxHold&&kind(best).priority>=v.kind.priority)))choose(v,best,kind(best),t);
  else if(!current&&t-v.lastVisible>.6){choose(v,null,Kind.REST,t);v.label="rest";}
 }
 public static void tickEntity(RedMage e){
  Entry v=entry(e);if(v.lastTick==e.tickCount)return;v.lastTick=e.tickCount;double t=now(e);boolean book=e.scene()==2||e.scene()==25;
  if(book&&v.bookStart<0)v.bookStart=t;if(!book)v.bookStart=-1;
  Vec3 wanted;
  if(book){choose(v,null,Kind.BOOK,t);v.label="book lines";wanted=v.bookPoint!=null?v.bookPoint:e.position().add(forward(e,v).scale(.55)).add(0,1,0);}
  else if(t<v.selectedUntil){
   if(v.selectedEntity!=null){Entity a=v.selectedEntity;boolean ok=eligible(e,v,a)&&visible(e,origin(e,v),a.getEyePosition());if(ok)v.lastVisible=t;
    if(!ok&&t-v.lastVisible>.6){v.selectedUntil=-1;v.nextScan=0;tickFallback(e,v,t);wanted=fallback(e,v);}else{choose(v,a,kind(a),t);wanted=a.getEyePosition();}}
   else if(v.selectedKind==Kind.SUN||v.selectedKind==Kind.MOON){choose(v,null,v.selectedKind,t);var d=LightExposureMath.sunDirection(e.level().getTimeOfDay(1));Vec3 dir=new Vec3(d.x(),d.y(),d.z()).scale(v.kind==Kind.MOON?-1:1);wanted=origin(e,v).add(dir.scale(64));}
   else if(v.selected!=null&&v.selected.distanceToSqr(origin(e,v))<=64*64&&pointVisible(e,origin(e,v),v.selected)){choose(v,null,Kind.POINT,t);wanted=v.selected;}
   else{v.selectedUntil=-1;tickFallback(e,v,t);wanted=fallback(e,v);}
  }else{tickFallback(e,v,t);wanted=fallback(e,v);}
  if(e.kitten()){choose(v,null,Kind.REST,t);wanted=fallback(e,v);}
  v.motion.tick(t,wanted,book);
 }
 private static void tickFallback(RedMage e,Entry v,double t){if(v.kind==Kind.BOOK||v.kind==Kind.POINT||v.kind==Kind.SUN||v.kind==Kind.MOON){choose(v,null,Kind.REST,t);v.nextScan=0;}if(e.tickCount>=v.nextScan)scan(e,v,t);}
 private static Vec3 fallback(RedMage e,Entry v){return v.target!=null?v.target.getEyePosition():origin(e,v).add(forward(e,v).scale(8));}
 private static Vec3 world(Vector3f p,Vec3 at){return at.add(p.x,p.y,p.z);}
 private static Matrix4f boneChain(GeoBone b){var stack=new PoseStack();var chain=new ArrayList<GeoBone>();for(;b!=null;b=b.getParent())chain.add(b);Collections.reverse(chain);for(var a:chain)RenderUtil.prepMatrixForBone(stack,a);return new Matrix4f(stack.last().pose());}
 /** Every head draw, after HeadSeat. Cancels camera/presentation matrices, retaining animated
  * root/body/head pivots, rotations, entity scale/yaw and actual translations. */
 public static void apply(GeoModel<?> model,RedMage e,GeoBone head,PoseStack parentPose,Matrix4f entityBase,Matrix4f modelBase,float partial){
  Entry v=entry(e);var local=new PoseStack();local.mulPose(parentPose.last().pose());RenderUtil.prepMatrixForBone(local,head);
  Matrix4f headToEntity=new Matrix4f(entityBase).invert().mul(local.last().pose());Matrix4f entityToHead=new Matrix4f(headToEntity).invert();Vec3 at=e.getPosition(partial);
  var center=headToEntity.transformPosition(new Vector3f(0,28.2f/16,-3.91f/16));v.headOrigin=world(center,at);
  var fw=headToEntity.transformDirection(new Vector3f(0,0,-1)).normalize();v.forward=new Vec3(fw.x,fw.y,fw.z);
  boolean book=e.scene()==2||e.scene()==25;
  if(book){model.getBone("book").ifPresent(b->{double elapsed=Math.max(0,now(e)+partial/20.0-v.bookStart),line=elapsed/3.1;int row=((int)line)%5;double phase=line-Math.floor(line),u=phase<.90?phase/.90:1-(phase-.90)/.10;float x=(float)(-.20+.40*u),z=(float)(-.69+.055*row);
   Matrix4f m=new Matrix4f(entityBase).invert().mul(modelBase).mul(boneChain(b));v.bookPoint=world(m.transformPosition(new Vector3f(x,17.5f/16,z)),at);v.bookCenter=world(m.transformPosition(new Vector3f(0,17.5f/16,-.58f)),at);});}
  tickEntity(e);Vec3 target=v.motion.sample(partial);if(target==null)target=v.headOrigin.add(v.forward.scale(8));
  var t=entityToHead.transformPosition(target.subtract(at).toVector3f());float[] micro=v.motion.micro(now(e)+partial/20.0);double d=Math.max(.15,new Vector3f(t).sub(0,28.2f/16,-3.91f/16).length());t.add((float)(micro[0]*d),(float)(micro[1]*d),0);
  var expression=EyeExpressions.sample(e,partial);var lids=MageEyeStates.sample(e,partial);
  float size=Math.clamp(v.pupil.sample(now(e)+partial/20.0)*(1+expression.pupil()),PupilSizeController.MIN,PupilSizeController.MAX);float[][] offsets=new float[2][];int index=0;boolean safe=true;
  double dz=-3.91/16.0-t.z;float[] common=dz<=.015?new float[]{0,0}:GazeController.offset(t.x,t.y-28.2/16.0,Math.max(dz,.75),1,book);
  for(String side:new String[]{"left","right"}){
   float cx=side.equals("left")?-1.70f:1.70f;
   // Per-eye ray to the SAME target; bounded near-field vergence avoids a crossed-eye look.
   float[] o=dz<=.015?new float[]{0,0}:GazeController.offset(t.x-cx/16,t.y-28.2/16.0,Math.max(dz,.75),1,book);
   o[0]=Math.clamp(o[0],common[0]-.025f,common[0]+.025f);
   if(book&&v.bookCenter!=null){
    // Calibrate the tiny page-depth changes around a down-looking rest angle, so distinct
    // lines remain visible without pinning the eye at the lower socket stop.
    var bc=entityToHead.transformPosition(v.bookCenter.subtract(at).toVector3f());
    double angle=Math.atan2(t.y-28.2/16.0,Math.max(dz,.75));
    double centreAngle=Math.atan2(bc.y-28.2/16.0,Math.max(-3.91/16.0-bc.z,.75));
    o[1]=Math.clamp((float)(-.065+.80*(angle-centreAngle)),-.115f,.115f);
   }
   float w=expression.gazeWeight();
   o[0]=Math.clamp(o[0]*(1-w)+(expression.x()+(side.equals("left")?1:-1)*expression.vergence())*w,-.8f,.8f);
   o[1]=EyeTravel.visibleVertical(size,o[0],o[1]*(1-w)+expression.y()*w,side.equals("left")?lids.leftMask():lids.rightMask(),side.equals("left")?lids.lowerLeft():lids.lowerRight(),side.equals("left"));
   offsets[index++]=o;
   var b=model.getBone("pupil_"+side).orElse(null);if(b==null)continue;
   // Size belongs to the dark central pupil, not the coloured iris or off-centre bone pivot.
   PupilIrisRig.apply(b,size,o[0],o[1],side.equals("left")?lids.leftMask():lids.rightMask(),side.equals("left")?lids.lowerLeft():lids.lowerRight());b.setScaleX(1);b.setScaleY(1);b.setScaleZ(1);
   b.setPosX(b.getInitialSnapshot().getOffsetX());
   b.setPosY(b.getInitialSnapshot().getOffsetY());b.setPosZ(b.getInitialSnapshot().getOffsetZ());
   safe&=Float.isFinite(o[0]+o[1])&&Math.abs(o[0])<=.8f&&Math.abs(o[1])<=1.15f; // optical geometry is clipped, not its centre clamped to the old fully-visible range
  }
  v.reading=new Reading(v.kind,v.label,v.target==null?-1:v.target.getId(),target,offsets[0][0],offsets[0][1],offsets[1][0],offsets[1][1],size,safe,Math.max(0,now(e)-v.chosenAt));
 }
 @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post ev){STATES.entrySet().removeIf(x->x.getKey().isRemoved());for(var e:new ArrayList<>(STATES.keySet()))tickEntity(e);}
 @SubscribeEvent public static void unload(net.neoforged.neoforge.event.level.LevelEvent.Unload ev){if(ev.getLevel().isClientSide())STATES.clear();}
 private MageGazeStates(){}
}
