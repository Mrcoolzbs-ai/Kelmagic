package dev.kelmagic.server;

import dev.kelmagic.KelMagic;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.*;
import net.minecraft.network.chat.Component;

public final class MagicDamage {
    public static DamageSource source(ServerPlayer p,String kind) {
        var key=ResourceKey.<DamageType>create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,kind));
        int variation=1+p.getRandom().nextInt(4);
        return new DamageSource(p.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key)) {
            @Override public Component getLocalizedDeathMessage(LivingEntity entity) {
                return Component.translatable("death.attack.kelmagic."+kind+"."+variation,entity.getDisplayName());
            }
        };
    }
    private MagicDamage() {}
}
