package dev.kelmagic.client;
import dev.kelmagic.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
/** Short translucent, emissive sprite; no lights, collision, terrain changes or camera state. */
public final class PhraseMote extends TextureSheetParticle {
 private static SpriteSet sparks,sigils,threads;private final float strength;
 private PhraseMote(ClientLevel level,double x,double y,double z,int style,double size,double opacity){
  super(level,x,y,z,0,0,0);hasPhysics=false;xd=yd=zd=0;lifetime=style>=4?10:5;quadSize=(float)size;strength=(float)opacity;
  float[][] colors={{.82f,.23f,1f},{1f,.71f,.22f},{.13f,.8f,.91f},{.82f,.98f,1f},{1f,.74f,.27f},{.35f,.94f,.91f}};
  var c=colors[Math.max(0,Math.min(colors.length-1,style))];setColor(c[0],c[1],c[2]);alpha=strength;
  pickSprite(style>=4?sigils:sparks);roll=oRoll=style>=4?(float)(Math.PI*.25):0;
 }
 public static void emit(ClientLevel l,double x,double y,double z,int style,double size,double opacity){
  if(sparks!=null&&sigils!=null)net.minecraft.client.Minecraft.getInstance().particleEngine.add(new PhraseMote(l,x,y,z,style,size,opacity));
 }
 public static void emitLine(ClientLevel l,net.minecraft.world.phys.Vec3 a,net.minecraft.world.phys.Vec3 b,int style,double width,double opacity){
  if(threads!=null)net.minecraft.client.Minecraft.getInstance().particleEngine.add(new Ribbon(l,a,b,style,width,opacity));
 }
 private static final class Ribbon extends TextureSheetParticle {
  final net.minecraft.world.phys.Vec3 a,b;final double width;final float strength;
  Ribbon(ClientLevel l,net.minecraft.world.phys.Vec3 a,net.minecraft.world.phys.Vec3 b,int style,double width,double opacity){
   super(l,(a.x+b.x)/2,(a.y+b.y)/2,(a.z+b.z)/2,0,0,0);this.a=a;this.b=b;this.width=width;strength=(float)opacity;
   xd=yd=zd=0;hasPhysics=false;lifetime=3;pickSprite(threads);alpha=strength;
   if(style==0)setColor(.82f,.23f,1f);else if(style==1)setColor(1f,.74f,.27f);else setColor(.23f,.91f,1f);
  }
  @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime){remove();return;}alpha=strength*(1-age/(float)lifetime);}
  @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
  @Override protected int getLightColor(float partial){return 0xF000F0;}
  @Override public void render(com.mojang.blaze3d.vertex.VertexConsumer v,net.minecraft.client.Camera camera,float partial){
   var mid=a.add(b).scale(.5);var side=b.subtract(a).cross(mid.subtract(camera.getPosition())).normalize();if(side.lengthSqr()<.01)side=new net.minecraft.world.phys.Vec3(1,0,0);
   var aa=a.subtract(camera.getPosition());var bb=b.subtract(camera.getPosition());
   quad(v,aa,bb,side.scale(width*3),alpha*.17f);quad(v,aa,bb,side.scale(width),alpha);
  }
  private void quad(com.mojang.blaze3d.vertex.VertexConsumer v,net.minecraft.world.phys.Vec3 a,net.minecraft.world.phys.Vec3 b,net.minecraft.world.phys.Vec3 s,float opacity){
   vertex(v,a.add(s),getU0(),getV0(),opacity);vertex(v,a.subtract(s),getU0(),getV1(),opacity);vertex(v,b.subtract(s),getU1(),getV1(),opacity);vertex(v,b.add(s),getU1(),getV0(),opacity);
  }
  private void vertex(com.mojang.blaze3d.vertex.VertexConsumer v,net.minecraft.world.phys.Vec3 p,float u,float w,float opacity){v.addVertex((float)p.x,(float)p.y,(float)p.z).setUv(u,w).setColor(rCol,gCol,bCol,opacity).setLight(0xF000F0);}
 }
 @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime){remove();return;}alpha=strength*(1-age/(float)lifetime);}
 @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
 @Override protected int getLightColor(float partial){return 0xF000F0;}
 @EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
 public static class Registration {
  @SubscribeEvent public static void register(RegisterParticleProvidersEvent e){
   e.registerSpriteSet(MagicParticles.PHRASE_SPARK.get(),s->{sparks=s;return (type,l,x,y,z,dx,dy,dz)->null;});
   e.registerSpriteSet(MagicParticles.PHRASE_THREAD.get(),s->{threads=s;return (type,l,x,y,z,dx,dy,dz)->null;});
   e.registerSpriteSet(MagicParticles.PHRASE_SIGIL.get(),s->{sigils=s;return (type,l,x,y,z,dx,dy,dz)->null;});
  }
 }
}
