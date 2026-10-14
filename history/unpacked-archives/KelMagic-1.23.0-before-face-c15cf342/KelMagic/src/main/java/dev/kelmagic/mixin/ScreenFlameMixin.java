package dev.kelmagic.mixin;
import dev.kelmagic.client.ArcaneFireVisuals;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenFlameMixin {
 @Redirect(method="renderScreenEffect",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z"))
 private static boolean kelmagic$burn(LocalPlayer p){return p.isOnFire() || ArcaneFireVisuals.active(p);}
 @Redirect(method="renderFire",at=@At(value="INVOKE",target="Lnet/minecraft/client/resources/model/Material;sprite()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
 private static TextureAtlasSprite kelmagic$texture(Material m,Minecraft mc,PoseStack pose){return ArcaneFireVisuals.active(mc.player)?ArcaneFireVisuals.sprite(1):m.sprite();}
}
