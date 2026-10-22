package dev.kelmagic.client;
import dev.kelmagic.KelMagic;
import dev.kelmagic.core.PhraseGeometry;
import dev.kelmagic.network.PhraseVisual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.ParticleStatus;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.util.*;
/** Fixed duration and bounded work regardless of spam or scale operators. */
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class PhraseEffects {
 private static final ArrayDeque<Cue> cues=new ArrayDeque<>();private static ClientLevel world;private static long clock;private static long receivedTick=Long.MIN_VALUE;private static int received;
 private static class Cue{final PhraseVisual p;final long born;Cue(PhraseVisual p,long tick){this.p=p;born=tick;}}
 static int spawnedLastTick,accepted;
 private static void reset(ClientLevel l){if(world!=l){cues.clear();world=l;clock=0;receivedTick=Long.MIN_VALUE;received=0;accepted=0;}}
 public static void accept(PhraseVisual p){
  var m=Minecraft.getInstance();reset(m.level);if(m.level==null||m.player==null||!p.valid()||m.player.distanceToSqr(p.x(),p.y(),p.z())>48*48)return;
  long t=clock;if(t!=receivedTick){receivedTick=t;received=0;}if(received++>=8)return;
  if(cues.size()>=12)cues.removeFirst();cues.addLast(new Cue(p,t));accepted++;
 }
 @SubscribeEvent public static void tick(ClientTickEvent.Post e){
  var m=Minecraft.getInstance();reset(m.level);spawnedLastTick=0;if(world==null||m.player==null||m.isPaused())return;
  long now=++clock;cues.removeIf(c->now-c.born>=PhraseGeometry.lifetime(c.p.kind())||now<c.born);
  int budget=m.options.particles().get()==ParticleStatus.MINIMAL?24:m.options.particles().get()==ParticleStatus.DECREASED?80:192;
  int total=cues.size();if(total==0)return;int allowance=Math.max(1,budget/total);
  for(var c:cues){Vec3 at=new Vec3(c.p.x(),c.p.y(),c.p.z());if(c.p.entityId()>=0){var entity=world.getEntity(c.p.entityId());if(entity==null||entity.isRemoved())continue;at=entity.position();}
   if(at.distanceToSqr(m.player.position())>48*48)continue;
   final Vec3 origin=at;var points=new ArrayList<double[]>(208);
   PhraseGeometry.draw(c.p.kind(),(int)(now-c.born),c.p.scale(),c.p.seed(),new PhraseGeometry.Sink(){
    public void point(double x,double y,double z,int style,double size,double alpha){points.add(new double[]{x,y,z,style,size,alpha});}
    public void line(double x,double y,double z,double bx,double by,double bz,int style,double width,double alpha){points.add(new double[]{x,y,z,style,width,alpha,bx,by,bz});}
   });
   // Stratified sampling preserves the complete silhouette on reduced particle settings.
   int n=Math.min(allowance,points.size());for(int i=0;i<n;i++){var a=points.get((int)((i+.5)*points.size()/n));if(a.length==9)PhraseMote.emitLine(world,origin.add(a[0],a[1],a[2]),origin.add(a[6],a[7],a[8]),(int)a[3],a[4],a[5]);else PhraseMote.emit(world,origin.x+a[0],origin.y+a[1],origin.z+a[2],(int)a[3],a[4],a[5]);spawnedLastTick++;}
  }
 }
 static int oldestAge(){return cues.isEmpty()?-1:(int)(clock-cues.getFirst().born);}
 static int active(){return cues.size();}
 private PhraseEffects(){}
}
