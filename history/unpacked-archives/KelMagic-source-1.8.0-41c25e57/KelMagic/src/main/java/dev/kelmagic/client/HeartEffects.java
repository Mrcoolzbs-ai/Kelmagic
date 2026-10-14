package dev.kelmagic.client;
import dev.kelmagic.MagicContent;
import dev.kelmagic.network.HeartActivation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
/** Uses vanilla totem animation with our own item, not a substituted vanilla totem. */
public final class HeartEffects {
    public static void activate(HeartActivation packet) {
        var mc=Minecraft.getInstance();if(mc.level==null) return;
        var entity=mc.level.getEntity(packet.entityId());if(entity==null) return;
        for(int i=0;i<32;i++)mc.level.addParticle(dev.kelmagic.MagicParticles.HEART.get(),entity.getX(),entity.getY()+1,entity.getZ(),entity.getId(),i,0);
        mc.level.playLocalSound(entity.getX(),entity.getY(),entity.getZ(),SoundEvents.TOTEM_USE,entity.getSoundSource(),1,1,false);
        if(entity==mc.player) mc.gameRenderer.displayItemActivation(MagicContent.DRAGON_HEART.toStack());
    }
    private HeartEffects(){}
}
