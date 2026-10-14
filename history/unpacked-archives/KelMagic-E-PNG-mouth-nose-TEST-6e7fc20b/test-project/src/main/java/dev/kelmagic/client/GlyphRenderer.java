package dev.kelmagic.client;

import dev.kelmagic.KelMagic;
import dev.kelmagic.core.GlyphWriting;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
public final class GlyphRenderer {
    private static final ResourceLocation FONT=ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"kel");
    static Component text(String s) { return Component.literal(s).withStyle(Style.EMPTY.withFont(FONT)); }
    @SubscribeEvent public static void world(RenderLevelStageEvent e) {
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        var mc=Minecraft.getInstance(); if(mc.level==null || mc.player==null || mc.options.hideGui || ClientSpellState.VISUALS.isEmpty()) return;
        Vec3 camera=e.getCamera().getPosition();
        var buffers=mc.renderBuffers().bufferSource();
        float partial=e.getPartialTick().getGameTimeDeltaPartialTick(false);
        boolean drew=false;
        for(var entry:ClientSpellState.VISUALS.entrySet()) {
            var player=mc.level.getPlayerByUUID(entry.getKey());if(player==null || player.distanceToSqr(camera)>64*64) continue;
            var visual=entry.getValue();visual.prepare(mc.font);var lines=visual.lines;
            float fade=visual.fading<0?1:Math.max(0,1-(ClientSpellState.ticks-visual.fading+partial)/24f);
            if(fade<=0) continue;
            double x=Mth.lerp(partial,player.xOld,player.getX()),y=Mth.lerp(partial,player.yOld,player.getY()),z=Mth.lerp(partial,player.zOld,player.getZ());
            Vec3 anchor=new Vec3(x,y+player.getBbHeight()+0.6,z);
            boolean firstPerson=player==mc.player && mc.options.getCameraType().isFirstPerson();
            if(firstPerson) anchor=camera.add(player.getLookAngle().scale(2.1)).add(0,0.35,0);
            var pose=e.getPoseStack();pose.pushPose();
            Vec3 offset=anchor.subtract(camera);pose.translate(offset.x,offset.y+Math.sin((ClientSpellState.ticks+partial)*0.06)*0.025+(1-fade)*0.6,offset.z);
            pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            pose.scale(0.012f, -0.012f,0.012f);
            int alpha=Math.max(4,(int)(255*fade));int color=alpha<<24 | 0xFFFFFF;
            drew|=!lines.isEmpty();
            for(int i=0;i<lines.size();i++) {
                Component line=lines.get(i);float width=visual.widths[i];
                mc.font.drawInBatch(line,-width/2f,-i*22,color,false,pose.last().pose(),buffers,Font.DisplayMode.NORMAL,0,0xF000F0);
            }
            pose.popPose();
        }
        if(drew)buffers.endBatch();
    }
    static List<String> lines(List<String> words) {
        List<String> lines=new ArrayList<>();StringBuilder row=new StringBuilder();int units=0;
        for(String word:words) {
            String glyphs=GlyphWriting.write(word);
            if(units+glyphs.length()>20 && !row.isEmpty()) {lines.add(row.toString());row.setLength(0);units=0;}
            if(!row.isEmpty()) row.append(' ');row.append(glyphs);units+=glyphs.length()+1;
        }
        if(!row.isEmpty()) lines.add(row.toString());return lines;
    }
    private GlyphRenderer() {}
}
