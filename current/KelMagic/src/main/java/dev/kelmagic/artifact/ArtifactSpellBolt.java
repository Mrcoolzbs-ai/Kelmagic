package dev.kelmagic.artifact;
import dev.kelmagic.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.registries.DeferredHolder;
/** A real, attributable projectile. Its refundable cost is divided across a cast's bolts. */
public final class ArtifactSpellBolt extends ThrowableItemProjectile {
    public static final DeferredHolder<EntityType<?>,EntityType<ArtifactSpellBolt>> TYPE=MagicCreatures.TYPES.register("word_bolt",()->EntityType.Builder.<ArtifactSpellBolt>of(ArtifactSpellBolt::new,MobCategory.MISC).sized(.25f,.25f).clientTrackingRange(8).updateInterval(1).build("kelmagic:word_bolt"));
    private float power=3;private int duration=60;
    public ArtifactSpellBolt(EntityType<? extends ArtifactSpellBolt> type,Level level){super(type,level);setNoGravity(true);}
    @Override protected Item getDefaultItem(){return MagicContent.MANA_CORE.get();}
    public void cost(double value){getPersistentData().putDouble("KelSpellCost",ArtifactRules.finite(value,1024));}
    public static boolean fire(ServerPlayer p,LivingEntity target,float power,int duration){return fireFrom(p,target,power,duration);}
    public static boolean fireFrom(LivingEntity p,LivingEntity target,float power,int duration){if(!(p.level() instanceof ServerLevel level)||ArtifactRuntime.immune(target))return false;if(target==p){var key=ResourceKey.<DamageType>create(Registries.DAMAGE_TYPE,ArtifactContent.id("spell"));boolean hit=target.hurt(new DamageSource(p.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key),p,p),power);if(hit&&duration>0)target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING,duration,0),p);return hit;}if(p instanceof ServerPlayer player&&!ArtifactRuntime.canHarm(player,target))return false;var bolt=TYPE.get().create(level);if(bolt==null)return false;bolt.setOwner(p);bolt.power=power;bolt.duration=duration;bolt.setPos(p.getEyePosition().subtract(0,.15,0));Vec3 direction=target.getBoundingBox().getCenter().subtract(bolt.position()).normalize();bolt.setDeltaMovement(direction.scale(1.05));if(!level.addFreshEntity(bolt))return false;ArtifactMagic.bolt(bolt);return true;}
    @Override public void tick(){super.tick();if(level().isClientSide){level().addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT,getX(),getY(),getZ(),0,0,0);}else{if(tickCount>80)discard();if(tickCount%3==0)ArtifactVisuals.ring((ServerLevel)level(),position(),.1,0x63dfe4,4);}}
    @Override protected void onHitEntity(EntityHitResult hit){super.onHitEntity(hit);if(level().isClientSide)return;if(hit.getEntity() instanceof LivingEntity target&&!ArtifactRuntime.immune(target)&&(!(getOwner() instanceof ServerPlayer p)||ArtifactRuntime.canHarm(p,target))){var key=ResourceKey.<DamageType>create(Registries.DAMAGE_TYPE,ArtifactContent.id("spell"));var source=new DamageSource(registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key),this,getOwner());if(target.hurt(source,power)&&duration>0)target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING,duration,0),getOwner());}burst();discard();}
    @Override protected void onHitBlock(BlockHitResult hit){super.onHitBlock(hit);if(!level().isClientSide){burst();discard();}}
    private void burst(){ArtifactVisuals.ring((ServerLevel)level(),position(),.3,0xe6c66e,10);}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putFloat("Power",power);t.putInt("Duration",duration);}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);power=(float)ArtifactRules.finite(t.getFloat("Power"),32);duration=Math.clamp(t.getInt("Duration"),0,1200);setNoGravity(true);}
}
