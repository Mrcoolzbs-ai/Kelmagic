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
        String playback=Playback.source(p);if(!playback.isEmpty())kind=playback+"_"+kind;
        final String messageKind=kind;
        int variation=1+p.getRandom().nextInt(4);
        return new DamageSource(p.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key)) {
            @Override public Component getLocalizedDeathMessage(LivingEntity entity) {
                return Component.translatable("death.attack.kelmagic."+messageKind+"."+variation,entity.getDisplayName());
            }
        };
    }
    public static DamageSource spell(ServerPlayer p,LivingEntity target){
        var key=ResourceKey.<DamageType>create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"spell"));
        String src=Playback.source(p);String id=src.isEmpty()?"spell":src+"_spell";
        return new DamageSource(p.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key),p,p){
            @Override public Component getLocalizedDeathMessage(LivingEntity e){return Component.translatable("death.attack.kelmagic."+id,e.getDisplayName(),p.getDisplayName());}
        };
    }
    private MagicDamage() {}
}
