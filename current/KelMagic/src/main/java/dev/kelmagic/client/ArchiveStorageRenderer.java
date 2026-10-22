package dev.kelmagic.client;
import dev.kelmagic.content.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.item.*;
import net.minecraft.core.Direction;
public final class ArchiveStorageRenderer implements BlockEntityRenderer<ArchiveStorageEntity> {
 public ArchiveStorageRenderer(BlockEntityRendererProvider.Context c){}
 @Override public void render(ArchiveStorageEntity d,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){var b=(ArchiveStorageBlock)d.getBlockState().getBlock();boolean stand=b.stand;int shown=0;for(int n=0;n<d.inventory.getContainerSize()&&shown<(stand?1:ArchiveStorage.SLOTS);n++){var s=d.inventory.getItem(n);if(s.isEmpty())continue;int at=stand?0:n;shown++;p.pushPose();p.translate(.5,stand?1.14:.5,.5);if(stand){p.mulPose(Axis.YP.rotationDegrees((d.getLevel().getGameTime()+partial)*1.4f));p.scale(.48f,.48f,.48f);}else{Direction f=d.getBlockState().getValue(ArchiveStorageBlock.FACING);p.mulPose(Axis.YP.rotationDegrees(-f.toYRot()));p.translate((at%6-2.5)*.145,(1-at/6)*.24,.506);p.scale(.125f,.18f,.025f);}Minecraft.getInstance().getItemRenderer().renderStatic(s,ItemDisplayContext.FIXED,light,overlay,p,buffers,d.getLevel(),(int)d.getBlockPos().asLong()+at);p.popPose();}}
}
