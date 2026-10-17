package dev.kelmagic.item;
import dev.kelmagic.server.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.server.level.ServerPlayer;
public final class RestoreManaEffect extends InstantenousMobEffect {
    public RestoreManaEffect(){super(MobEffectCategory.BENEFICIAL,0x56DCCC);}
    @Override public void applyInstantenousEffect(Entity source,Entity indirect,LivingEntity target,int amplifier,double strength) {
        restore(target,(int)Math.round(10*Math.max(0,Math.min(1,strength))));
    }
    @Override public boolean applyEffectTick(LivingEntity entity,int amplifier) {restore(entity,10);return true;}
    private static void restore(LivingEntity entity,int amount) {
        if(entity instanceof ServerPlayer p) {
            var mana=p.getData(MagicAttachments.MANA);
            p.setData(MagicAttachments.MANA,mana.withCurrent(Math.min(MagicServer.maximum(p),mana.current()+amount)));
            MagicServer.sync(p);
        }
    }
}
