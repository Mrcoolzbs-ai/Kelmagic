package dev.kelmagic.entity;
import dev.kelmagic.server.ThresholdLinks;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
/** A spell surface, not a nether portal block. Living for splash/lingering potion targeting. */
public final class ThresholdPortal extends LivingEntity {
 private static final EntityDataAccessor<Integer> COLOR=SynchedEntityData.defineId(ThresholdPortal.class,EntityDataSerializers.INT);
 public ThresholdPortal(EntityType<? extends ThresholdPortal> type,Level l){super(type,l);setNoGravity(true);noPhysics=true;}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(COLOR,0x62b8de);}
 public int color(){return entityData.get(COLOR);}public void color(int value){entityData.set(COLOR,value&0xffffff);}
 @Override public boolean isPickable(){return true;}@Override public boolean isPushable(){return false;}
 @Override public boolean canBeCollidedWith(){return false;}
 @Override public boolean skipAttackInteraction(Entity who){if(!level().isClientSide)ThresholdLinks.close(this,who);return true;}
 @Override public boolean hurt(DamageSource source,float amount){if(!level().isClientSide)ThresholdLinks.close(this,source.getEntity());return true;}
 @Override public boolean addEffect(MobEffectInstance effect,Entity source){if(!effect.getEffect().value().isBeneficial()){if(!level().isClientSide)ThresholdLinks.close(this,source);return true;}return false;}
 @Override public void tick(){super.tick();setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);if(!level().isClientSide)ThresholdLinks.tick(this);}
 @Override public Iterable<ItemStack> getArmorSlots(){return java.util.List.of();}
 @Override public ItemStack getItemBySlot(EquipmentSlot s){return ItemStack.EMPTY;}
 @Override public void setItemSlot(EquipmentSlot s,ItemStack item){}
 @Override public HumanoidArm getMainArm(){return HumanoidArm.RIGHT;}
 @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putInt("ThresholdColor",color());}
 @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);color(tag.getInt("ThresholdColor"));setNoGravity(true);noPhysics=true;}
}
