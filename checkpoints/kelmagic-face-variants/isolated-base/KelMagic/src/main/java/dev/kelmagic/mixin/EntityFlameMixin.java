package dev.kelmagic.mixin;
import dev.kelmagic.client.ArcaneFireVisuals;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityFlameMixin {
 @Redirect(method="render",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;displayFireAnimation()Z"))
 private boolean kelmagic$flames(Entity e){return e.displayFireAnimation() || ArcaneFireVisuals.active(e);}
 @Redirect(method="renderFlame",at=@At(value="INVOKE",target="Lnet/minecraft/client/resources/model/Material;sprite()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
 private TextureAtlasSprite kelmagic$sprite(Material m,PoseStack pose,MultiBufferSource buffer,Entity e,Quaternionf rotation){return ArcaneFireVisuals.active(e)?ArcaneFireVisuals.sprite(m.texture().getPath().endsWith("0")?0:1):m.sprite();}
}
