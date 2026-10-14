package dev.kelmagic.client.face;
import dev.kelmagic.entity.RedMage;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.GeoBone;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
/** Per-entity client sensor. Bounded loaded-block cache, no render-time world scans/queries.
 * Snapshot head direction only; does not modify head, pupils, clips, geometry or lighting. */
public final class LightExposureSensor {
 public record Reading(float sun,float lava,float raw,float filtered,int testedRays,int lavaCandidates,Vec3 direction){}
 private final List<BlockPos> lavaBlocks=new ArrayList<>();
 private Vec3 headDirection;private int poseTick=-100,nextSense,nextScan,lastSense=-1;
 private BlockPos scanCenter;private double filtered;
 private Reading reading=new Reading(0,0,0,0,0,0,Vec3.ZERO);
 public Reading reading(){return reading;}
 /** Matches GeckoLib ZYX bone rotations and normal entity-body yaw; current approved hierarchy only.
  * Head is captured before eyelid composition, after existing clips/procedural head rotation. */
 public void captureHead(RedMage e,GeoBone head,float partial){
  Vector3f v=new Vector3f(0,0,-1);
  for(GeoBone b=head;b!=null;b=b.getParent())new Quaternionf().rotationZYX(b.getRotZ(),b.getRotY(),b.getRotX()).transform(v);
  float body=Mth.rotLerp(partial,e.yBodyRotO,e.yBodyRot);
  v.rotateY((float)Math.toRadians(180-body));v.normalize();
  headDirection=new Vec3(v.x,v.y,v.z);poseTick=e.tickCount;
 }
 private void scan(RedMage e,Vec3 origin){
  lavaBlocks.clear();scanCenter=BlockPos.containing(origin);var level=e.level();int r=6;
  for(BlockPos p:BlockPos.betweenClosed(scanCenter.offset(-r,-r,-r),scanCenter.offset(r,r,r))){
   if(level.isOutsideBuildHeight(p)||!level.hasChunkAt(p)||p.getCenter().distanceToSqr(origin)>36)continue;
   if(level.getFluidState(p).is(FluidTags.LAVA))lavaBlocks.add(p.immutable());
  }
  nextScan=e.tickCount+20;
 }
 /** Reject unloaded ray segments instead of loading chunks. Outside build height is empty sky. */
 private boolean visible(RedMage e,Vec3 from,Vec3 to){
  var level=e.level();Vec3 delta=to.subtract(from);int steps=Math.max(1,(int)Math.ceil(delta.length()/4));
  for(int i=0;i<=steps;i++){BlockPos p=BlockPos.containing(from.add(delta.scale((double)i/steps)));
   if(!level.isOutsideBuildHeight(p)&&!level.hasChunkAt(p))return false;
  }
  return level.clip(new ClipContext(from,to,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,e)).getType()==HitResult.Type.MISS;
 }
 public boolean tick(RedMage e){
  if(e.tickCount<nextSense)return false;nextSense=e.tickCount+5;
  Vec3 origin=e.getEyePosition();Vec3 gaze=poseTick>=e.tickCount-10&&headDirection!=null?headDirection:e.getViewVector(1);
  var level=e.level();if(scanCenter==null||e.tickCount>=nextScan||scanCenter.distManhattan(BlockPos.containing(origin))>=2)scan(e,origin);
  double sun=0,lava=0;int rays=0;
  if(e.isAlive()&&!e.kitten()){
   if(level.dimensionType().hasSkyLight()&&!level.dimensionType().hasCeiling()){
    var d=LightExposureMath.sunDirection(level.getTimeOfDay(1));Vec3 direction=new Vec3(d.x(),d.y(),d.z());
    double strength=LightExposureMath.sun(gaze.dot(direction),d.y(),level.getRainLevel(1),level.getThunderLevel(1));
    if(strength>.001){rays++;double length=Math.min(64,Math.max(1,(level.getMaxBuildHeight()+1-origin.y)/Math.max(.04,d.y())));
     if(visible(e,origin,origin.add(direction.scale(length))))sun=strength;
    }
   }
   record Candidate(Vec3 point,double strength){}List<Candidate> candidates=new ArrayList<>();
   for(BlockPos pos:lavaBlocks){
    if(!level.hasChunkAt(pos))continue;var fluid=level.getFluidState(pos);if(!fluid.is(FluidTags.LAVA))continue;
    Vec3 point=new Vec3(pos.getX()+.5,pos.getY()+fluid.getHeight(level,pos)*.8,pos.getZ()+.5);
    Vec3 delta=point.subtract(origin);double distance=delta.length();if(distance<.01)continue;
    double strength=LightExposureMath.lava(gaze.dot(delta.scale(1/distance)),distance);
    if(strength>.001)candidates.add(new Candidate(point,strength));
   }
   candidates.sort(Comparator.comparingDouble(Candidate::strength).reversed());
   for(int i=0;i<Math.min(12,candidates.size());i++){
    Candidate c=candidates.get(i);rays++;if(visible(e,origin,c.point)){lava=c.strength;break;}
   }
  }
  double raw=LightExposureMath.combine(sun,lava);double dt=lastSense<0?.25:Math.max(0,(e.tickCount-lastSense)/20.0);lastSense=e.tickCount;
  filtered=LightExposureMath.approach(filtered,raw,dt);if(raw==0&&filtered<.0005)filtered=0;
  reading=new Reading((float)sun,(float)lava,(float)raw,(float)filtered,rays,lavaBlocks.size(),gaze);return true;
 }
}
