package dev.kelmagic;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.*;
public final class MagicParticles {
 public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> TYPES=DeferredRegister.create(Registries.PARTICLE_TYPE,KelMagic.MODID);
 public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>,SimpleParticleType> HEART=TYPES.register("heart_mote",()->new SimpleParticleType(true));
 private MagicParticles(){}
}
