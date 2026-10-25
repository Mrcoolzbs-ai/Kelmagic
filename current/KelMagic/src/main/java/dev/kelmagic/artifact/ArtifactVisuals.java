package dev.kelmagic.artifact;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
public final class ArtifactVisuals {
    public static void ring(ServerLevel l,Vec3 at,double radius,int color,int count){var dust=new DustParticleOptions(new Vector3f(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f),.85f);for(int i=0;i<count;i++){double a=i*Math.PI*2/count;l.sendParticles(dust,at.x+Math.cos(a)*radius,at.y,at.z+Math.sin(a)*radius,1,0,.025,0,0);}}
    public static void line(ServerLevel l,Vec3 a,Vec3 b,int color){Vec3 d=b.subtract(a);int n=Math.min(40,Math.max(2,(int)(d.length()*3)));for(int i=0;i<=n;i++){Vec3 p=a.add(d.scale((double)i/n));l.sendParticles(new DustParticleOptions(new Vector3f(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f),.7f),p.x,p.y,p.z,1,0,0,0,0);}}
    public static void sound(ServerLevel l,Vec3 at,String sound,float pitch){l.playSound(null,at.x,at.y,at.z,ArtifactContent.SOUNDS.get(sound).get(),SoundSource.BLOCKS,.65f,pitch);}
    private ArtifactVisuals(){}
}
