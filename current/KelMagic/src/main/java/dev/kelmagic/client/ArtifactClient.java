package dev.kelmagic.client;
import dev.kelmagic.*;
import dev.kelmagic.artifact.*;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.extensions.common.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class ArtifactClient {
    private static final String CATEGORY="key.categories.kelmagic_artifacts";
    static final KeyMapping RESERVE=new KeyMapping("key.kelmagic.reserve",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_G,CATEGORY),DEW=new KeyMapping("key.kelmagic.dew",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_H,CATEGORY),SAMPLE=new KeyMapping("key.kelmagic.sample",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_Y,CATEGORY),ANCHOR=new KeyMapping("key.kelmagic.anchor",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_B,CATEGORY);
    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(RESERVE);e.register(DEW);e.register(SAMPLE);e.register(ANCHOR);}
    @SubscribeEvent public static void renderer(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(ArtifactSpellBolt.TYPE.get(),ctx->new ThrownItemRenderer<>(ctx,.45f,true));}
    @SubscribeEvent public static void armor(RegisterClientExtensionsEvent e){ArtifactContent.ARMOR.forEach((set,items)->{var models=new EnumMap<EquipmentSlot,ArtifactArmorModel>(EquipmentSlot.class);IClientItemExtensions extension=new IClientItemExtensions(){@Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> original){var model=models.computeIfAbsent(slot,k->new ArtifactArmorModel(set,k));((HumanoidModel<LivingEntity>)(HumanoidModel<?>)original).copyPropertiesTo(model);model.animate(entity);return model;}};items.forEach(item->e.registerItem(extension,item.get()));});}
    @EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
    public static final class Events {
        @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null)return;while(RESERVE.consumeClick())PacketDistributor.sendToServer(new ArtifactNetworking.Action(mc.player.isShiftKeyDown()?1:0));while(DEW.consumeClick())PacketDistributor.sendToServer(new ArtifactNetworking.Action(2));while(SAMPLE.consumeClick())PacketDistributor.sendToServer(new ArtifactNetworking.Action(3));while(ANCHOR.consumeClick())PacketDistributor.sendToServer(new ArtifactNetworking.Action(4));}
        @SubscribeEvent public static void silence(PlaySoundEvent e){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||e.getSound()==null||e.getSound().isRelative())return;var sound=e.getSound();var pos=new Vec3(sound.getX(),sound.getY(),sound.getZ());for(var p:mc.level.players())if(ArtifactRuntime.silent(p)&&pos.distanceToSqr(p.position())<1.5){e.setSound(null);return;}if(ArtifactRuntime.silent(mc.player)&&mc.hitResult instanceof BlockHitResult hit&&pos.distanceToSqr(Vec3.atCenterOf(hit.getBlockPos()))<2&&(mc.gameMode!=null&&(mc.gameMode.isDestroying()||mc.options.keyUse.isDown()||mc.options.keyAttack.isDown())))e.setSound(null);}
        @SubscribeEvent public static void marks(RenderLevelStageEvent e){if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.options.hideGui)return;ItemStack stack=mc.player.getMainHandItem().is(ArtifactContent.CHALK.get())?mc.player.getMainHandItem():mc.player.getOffhandItem();if(!stack.is(ArtifactContent.CHALK.get()))return;var tag=ArtifactData.get(stack);if(!tag.hasUUID("KelMarkOwner")||!tag.getUUID("KelMarkOwner").equals(mc.player.getUUID()))return;var pose=e.getPoseStack();var camera=e.getCamera().getPosition();pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);var buf=mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());for(var entry:tag.getList("KelMarks",net.minecraft.nbt.Tag.TAG_COMPOUND)){var n=(net.minecraft.nbt.CompoundTag)entry;if(!n.getString("Dim").equals(mc.level.dimension().location().toString()))continue;var at=BlockPos.of(n.getLong("Pos"));if(at.distToCenterSqr(mc.player.position())>4096||!mc.level.hasChunkAt(at))continue;LevelRenderer.renderLineBox(pose,buf,new AABB(at.getX()+.2,at.getY()+1.01,at.getZ()+.2,at.getX()+.8,at.getY()+1.04,at.getZ()+.8),.35f,.95f,.88f,.95f);}pose.popPose();mc.renderBuffers().bufferSource().endBatch(RenderType.lines());}
    }
}
