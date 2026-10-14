package dev.kelmagic.server;

import dev.kelmagic.Config;
import dev.kelmagic.core.SpellParser.Effect;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.*;

public final class SpellEffects {
    public record Aim(Vec3 point, HitResult.Type type, BlockHitResult block, LivingEntity entity) {}
    static Aim aim(ServerPlayer p) {
        Vec3 eye=p.getEyePosition(), end=eye.add(p.getLookAngle().scale(Config.RANGE.get()));
        BlockHitResult block=p.serverLevel().clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        Vec3 stop=block.getLocation(); LivingEntity target=null; double distance=eye.distanceToSqr(stop);
        AABB box=new AABB(eye,stop).inflate(1);
        for(LivingEntity e:p.serverLevel().getEntitiesOfClass(LivingEntity.class,box,e -> e!=p && e.isAlive() && !e.isSpectator())) {
            Optional<Vec3> hit=e.getBoundingBox().inflate(0.15).clip(eye,stop);
            if(hit.isPresent() && eye.distanceToSqr(hit.get())<distance) { distance=eye.distanceToSqr(hit.get()); target=e; }
        }
        return new Aim(target==null?stop:target.position(),target==null?block.getType():HitResult.Type.ENTITY,block,target);
    }
    public static boolean execute(ServerPlayer p,dev.kelmagic.core.SpellParser.Plan plan){
        if(plan.effect()==Effect.UNDEFINED || plan.effect()==Effect.SELECTION_ONLY)return false;
        if(plan.effect()==Effect.SYNERGY || plan.effect()==Effect.FLAME_LIFE || plan.effect()==Effect.COMPOSITE || plan.scale()!=1)return ExpandedSpellEffects.execute(p,plan);
        return execute(p,plan.effect());
    }
    public static boolean execute(ServerPlayer p,Effect effect) {
        Aim aim=aim(p);
        return switch(effect) {
            case FIRE_SELF -> { ignite(p); yield true; }
            case RESIST_SELF -> { p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,Config.RESIST_SECONDS.get()*20)); yield true; }
            case FIRE_PLAYER -> aim.entity instanceof Player other && canHarm(p,other) && ignite(other);
            case FIRE_MOB -> aim.entity instanceof Mob mob && ignite(mob);
            case FIRE_BLOCK -> aim.type==HitResult.Type.BLOCK && igniteBlockHit(p,aim.block);
            case FIRE_AREA -> areaFire(p,aim);
            case FIRE_PLAYERS -> areaTargets(p,aim,true);
            case FIRE_MOBS -> areaTargets(p,aim,false);
            default -> false;
        };
    }
    private static boolean canHarm(ServerPlayer caster,Player target) {
        return target==caster || caster.server.isPvpAllowed() && caster.canHarmPlayer(target);
    }
    private static boolean ignite(LivingEntity target) {
        return ArcaneFire.ignite(target);
    }
    private static boolean areaTargets(ServerPlayer p,Aim aim,boolean players) {
        double radius=Config.AREA_RADIUS.get();
        var entities=p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(aim.point,aim.point).inflate(radius),
            e -> e.isAlive() && !e.isSpectator() && e.distanceToSqr(aim.point)<=radius*radius && (players?e instanceof Player:e instanceof Mob));
        entities.sort(Comparator.<LivingEntity>comparingDouble(e -> e.distanceToSqr(aim.point)).thenComparing(e -> e.getUUID().toString()));
        int hits=0;
        for(LivingEntity e:entities) {
            if(hits==Config.MAX_TARGETS.get()) break;
            if(e instanceof Player player && !canHarm(p,player)) continue;
            // Neither the target ray nor a lu expansion should pass through solid walls.
            var obstruction=p.serverLevel().clip(new ClipContext(aim.point.add(0,0.5,0),e.getEyePosition(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
            if(obstruction.getType()!=HitResult.Type.MISS && obstruction.getLocation().distanceToSqr(e.getEyePosition())>1) continue;
            if(ignite(e)) hits++;
        }
        return hits>0;
    }
    public static boolean igniteBlockHit(ServerPlayer p,BlockHitResult hit){return igniteBlockHit(p,hit,1);}
    public static boolean igniteBlockHit(ServerPlayer p,BlockHitResult hit,double scale) {
        if(net.minecraft.world.level.block.CampfireBlock.canLight(p.serverLevel().getBlockState(hit.getBlockPos())))
            return placeFire(p,hit.getBlockPos(),scale);
        return placeFire(p,hit.getBlockPos().relative(hit.getDirection()),scale);
    }
    private static boolean placeFire(ServerPlayer p,BlockPos pos){return placeFire(p,pos,1);}
    private static boolean placeFire(ServerPlayer p,BlockPos pos,double scale) {
        var level=p.serverLevel();
        if(!level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos) || !level.isInWorldBounds(pos)
                || !level.mayInteract(p,pos) || !p.mayUseItemAt(pos,net.minecraft.core.Direction.UP,p.getMainHandItem()) ) return false;
        var old=level.getBlockState(pos);
        boolean campfire=net.minecraft.world.level.block.CampfireBlock.canLight(old);
        if(!campfire && !level.isEmptyBlock(pos))return false;
        var state=campfire?old.setValue(net.minecraft.world.level.block.CampfireBlock.LIT,true):dev.kelmagic.MagicContent.MAGIC_FIRE.get().stateAt(level,pos);
        if(!campfire)state=state.setValue(dev.kelmagic.block.MagicFireBlock.SCALE,dev.kelmagic.block.MagicFireBlock.scaleIndex(scale));
        if(!campfire && !state.canSurvive(level,pos) && !dev.kelmagic.block.MagicFireBlock.canOpenPortal(level,pos)) return false;
        var snapshot=net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(),level,pos);
        if(!level.setBlockAndUpdate(pos,state)) return false;
        if(net.neoforged.neoforge.event.EventHooks.onBlockPlace(p,snapshot,net.minecraft.core.Direction.UP)) {
            snapshot.restore(); return false;
        }
        if(campfire)level.playSound(null,pos,net.minecraft.sounds.SoundEvents.FLINTANDSTEEL_USE,net.minecraft.sounds.SoundSource.BLOCKS,1,1);
        return true;
    }
    private static boolean areaFire(ServerPlayer p,Aim aim){return areaFire(p,aim,1);}
    static boolean areaFire(ServerPlayer p,Aim aim,double scale) {
        if(aim.type!=HitResult.Type.BLOCK) return false;
        int r=Math.max(1,Math.min(12,(int)Math.round(Config.AREA_RADIUS.get()*scale))),hits=0; BlockPos origin=aim.block.getBlockPos().relative(aim.block.getDirection());
        List<BlockPos> positions=new ArrayList<>();
        for(int x=-r;x<=r;x++) for(int z=-r;z<=r;z++) if(x*x+z*z<=r*r) positions.add(origin.offset(x,0,z));
        positions.sort(Comparator.<BlockPos>comparingDouble(pos -> pos.distSqr(origin)).thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ));
        for(BlockPos pos:positions) {
            if(hits>=Math.max(1,Math.min(32,(int)Math.round(Config.MAX_FIRES.get()*scale)))) break;
            for(int y=2;y>=-2;y--) if(placeFire(p,pos.above(y),scale)) { hits++; break; }
        }
        return hits>0;
    }
    public static void fizzle(ServerPlayer p,String key) {
        p.displayClientMessage(Component.translatable(key),true);
        if(key.equals("kelmagic.undefined")) {
            // Real explosive firework attached to the caster: native damage, creative/PvP rules.
            var rocket=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FIREWORK_ROCKET);
            var star=new net.minecraft.world.item.component.FireworkExplosion(
                net.minecraft.world.item.component.FireworkExplosion.Shape.STAR,
                it.unimi.dsi.fastutil.ints.IntList.of(0x67E6ED,0xB982E6),
                it.unimi.dsi.fastutil.ints.IntList.of(0xEBC478),true,true);
            rocket.set(net.minecraft.core.component.DataComponents.FIREWORKS,
                new net.minecraft.world.item.component.Fireworks(0,java.util.List.of(star)));
            p.serverLevel().addFreshEntity(new net.minecraft.world.entity.projectile.FireworkRocketEntity(p.serverLevel(),rocket,p));
        } else p.serverLevel().sendParticles(ParticleTypes.FIREWORK,p.getX(),p.getY()+2.5,p.getZ(),24,0.4,0.3,0.4,0.06);
    }
    private SpellEffects() {}
}
