package dev.kelmagic.client;
import dev.kelmagic.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
public final class HeartMote extends TextureSheetParticle {
 private final Entity target;private final double phase,height;
 HeartMote(ClientLevel level,Entity target,int seed,SpriteSet sprites){
  super(level,target.getX(),target.getY()+1,target.getZ());this.target=target;phase=seed*Math.PI*2/32;height=.35+(seed%8)*.19;
  lifetime=44;quadSize=.08f;hasPhysics=false;pickSprite(sprites);
  if(seed%2==0)setColor(.68f,.16f,1f);else setColor(.9f,.025f,.21f);
 }
 @Override public void tick(){
  xo=x;yo=y;zo=z;if(age++>=lifetime || target.isRemoved()){remove();return;}
  // 6-tick burst, exactly 30 ticks (1.5 s) orbit, 8-tick return into the chest.
  double radius=age<6?age/6.0:age<=36?1:(44-age)/8.0;
  double angle=phase+(age-6)*.24;
  setPos(target.getX()+Math.cos(angle)*radius,target.getY()+1+(height-1)*radius+Math.sin(angle*2)*.08*radius,target.getZ()+Math.sin(angle)*radius);
  alpha=Math.min(1,(44-age)/6f);
 }
 @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
 @Override protected int getLightColor(float partial){return 0xF000F0;}
 @EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
 public static final class Registration {
  @SubscribeEvent public static void register(RegisterParticleProvidersEvent e){
   e.registerSpriteSet(MagicParticles.HEART.get(),sprites->(type,level,x,y,z,dx,dy,dz)->{
    var entity=level.getEntity((int)dx);return entity==null?null:new HeartMote(level,entity,(int)dy,sprites);
   });
  }
 }
}
