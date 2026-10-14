package dev.kelmagic.server;

import dev.kelmagic.Config;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Ten bounded, staggered blasts. No forced chunk loads; vanilla explosion hooks retained. */
public final class OverloadEffects {
    private static final List<Job> JOBS=new ArrayList<>();
    private static long totalBlasts,maxBlastNanos;
    public static long totalBlasts(){return totalBlasts;}
    public static long maxBlastNanos(){return maxBlastNanos;}
    public static int pendingJobs(){return JOBS.size();}
    private static final class Job {
        final ServerLevel level; final Vec3 center; final List<UUID> viewers;
        final int bursts,power; final boolean terrain; final UUID caster; int age,done;
        Job(ServerPlayer p) {
            level=p.serverLevel();center=p.position();caster=p.getUUID();bursts=Config.OVERLOAD_BURSTS.get();
            power=Config.OVERLOAD_POWER.get();terrain=Config.OVERLOAD_TERRAIN.get();
            viewers=level.players().stream().filter(v->v.distanceToSqr(center)<256*256)
                .sorted(Comparator.comparingDouble(v->v.distanceToSqr(center))).limit(16).map(ServerPlayer::getUUID).toList();
        }
        void blast() {
            int index=done++;
            // Central blast + two offset rings; overlap produces a connected crater, not ten identical replays.
            double a=2*Math.PI*(index-1)/9;
            double radius=index==0?0:power*(index%2==0?1.65:1.1);
            Vec3 pos=center.add(Math.cos(a)*radius,0.5,Math.sin(a)*radius);
            // Do not let vanilla explosion traversal load an unloaded neighboring chunk.
            int margin=power*2+2;
            for(int x=BlockPos.containing(pos).getX()-margin;x<=pos.x+margin;x+=8)
                for(int z=BlockPos.containing(pos).getZ()-margin;z<=pos.z+margin;z+=8)
                    if(!level.hasChunkAt(new BlockPos(x,(int)pos.y,z))) return;
            ServerPlayer owner=level.getServer().getPlayerList().getPlayer(caster);
            long started=System.nanoTime();
            level.explode(owner,pos.x,pos.y,pos.z,power,terrain?Level.ExplosionInteraction.TNT:Level.ExplosionInteraction.NONE);
            long elapsed=System.nanoTime()-started;totalBlasts++;maxBlastNanos=Math.max(maxBlastNanos,elapsed);
            dev.kelmagic.KelMagic.LOGGER.debug("Overload burst {}/{}: power {}, terrain {}, {} ms",index+1,bursts,power,terrain,elapsed/1_000_000.0);
        }
        void particle(net.minecraft.core.particles.SimpleParticleType type,double x,double y,double z,int count) {
            if(!level.hasChunkAt(BlockPos.containing(x,y,z))) return;
            for(UUID id:viewers) {
                ServerPlayer p=level.getServer().getPlayerList().getPlayer(id);
                if(p!=null && p.serverLevel()==level && p.distanceToSqr(center)<256*256)
                    level.sendParticles(p,type,true,x,y,z,count,0.8,0.8,0.8,0.035);
            }
        }
    }
    public static void trigger(ServerPlayer p) {
        Job job=new Job(p);
        if(!p.isCreative() && !p.isSpectator() && !p.getAbilities().invulnerable)
            p.hurt(MagicDamage.source(p,"overload"),Float.MAX_VALUE);
        if(JOBS.size()>=4) return; // Burst storms cannot accumulate unboundedly from spam/creative casters.
        job.blast();JOBS.add(job);
    }
    public static void tick() {
        boolean blasted=false; // Global bound: at most one queued terrain blast per server tick.
        for(Iterator<Job> it=JOBS.iterator();it.hasNext();) {
            Job j=it.next();j.age++;
            if(!blasted && j.age%6==0 && j.done<j.bursts) {j.blast();blasted=true;}
            if(j.age%2==0) {
                double ring=Math.min(100,j.age*2.4);
                for(int i=0;i<24;i++) {
                    double a=2*Math.PI*i/24;
                    j.particle(ParticleTypes.CLOUD,j.center.x+Math.cos(a)*ring,j.center.y+1.5,j.center.z+Math.sin(a)*ring,2);
                }
                double height=Math.min(65,j.age*1.2),cap=Math.min(32,j.age*0.7);
                for(int i=0;i<12;i++) {
                    double a=2*Math.PI*i/12;
                    j.particle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,j.center.x+Math.cos(a)*cap,j.center.y+height,j.center.z+Math.sin(a)*cap,3);
                }
                if(j.age<20) j.particle(ParticleTypes.EXPLOSION_EMITTER,j.center.x,j.center.y+j.age*0.6,j.center.z,1);
            }
            if(j.age>=100 && j.done>=j.bursts) it.remove();
        }
    }
    public static void clear() {JOBS.clear();}
    private OverloadEffects() {}
}
