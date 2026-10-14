package dev.kelmagic.mixin;
import dev.kelmagic.MagicStudy;import net.minecraft.world.entity.LivingEntity;import net.minecraft.world.entity.npc.VillagerDataHolder;import net.minecraft.core.registries.BuiltInRegistries;import net.minecraft.resources.ResourceLocation;import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(VillagerProfessionLayer.class)
public abstract class ArchivistClothesMixin {
 @Unique private String kelmagic$biome="plains";
 @Unique private static final ResourceLocation KELMAGIC_JOB=ResourceLocation.fromNamespaceAndPath("kelmagic","archivist");
 @Unique private static final java.util.Map<String,ResourceLocation> KELMAGIC_OUTFITS=java.util.stream.Stream.of("plains","desert","taiga","snow","snowy","savanna","jungle","swamp").collect(java.util.stream.Collectors.toUnmodifiableMap(b->b,b->ResourceLocation.fromNamespaceAndPath("kelmagic","textures/entity/villager/profession/archivist_"+b+".png")));
 @Inject(method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",at=@At("HEAD"))private void kelmagic$biome(com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,int light,LivingEntity e,float a,float b,float c,float d,float f,float g,CallbackInfo ci){if(e instanceof VillagerDataHolder v)kelmagic$biome=BuiltInRegistries.VILLAGER_TYPE.getKey(v.getVillagerData().getType()).getPath();}
 @Inject(method="getResourceLocation",at=@At("RETURN"),cancellable=true)private void kelmagic$outfit(String kind,ResourceLocation id,CallbackInfoReturnable<ResourceLocation> ci){if(kind.equals("profession")&&id.equals(KELMAGIC_JOB))ci.setReturnValue(KELMAGIC_OUTFITS.getOrDefault(kelmagic$biome,KELMAGIC_OUTFITS.get("plains")));}

}
