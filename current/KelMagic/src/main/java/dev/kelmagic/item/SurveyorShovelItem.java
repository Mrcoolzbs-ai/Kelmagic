package dev.kelmagic.item;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
public final class SurveyorShovelItem extends ShovelItem {
 private static final Map<UUID,BlockPos> MARKS=new HashMap<>();public SurveyorShovelItem(Tier t,Properties p){super(t,p);}
 @Override public InteractionResult useOn(UseOnContext c){if(!(c.getPlayer() instanceof ServerPlayer p))return super.useOn(c);var q=c.getClickedPos();if(p.isShiftKeyDown()){MARKS.put(p.getUUID(),q.immutable());p.displayClientMessage(Component.literal("Первая точка отмечена."),true);return InteractionResult.SUCCESS;}var a=MARKS.remove(p.getUUID());if(a==null||a.getY()!=q.getY())return super.useOn(c);int x0=Math.min(a.getX(),q.getX()),x1=Math.max(a.getX(),q.getX()),z0=Math.min(a.getZ(),q.getZ()),z1=Math.max(a.getZ(),q.getZ());if((x1-x0+1)*(z1-z0+1)>256){p.displayClientMessage(Component.literal("Область слишком велика: максимум 16×16."),true);return InteractionResult.FAIL;}int n=0;for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){var at=new BlockPos(x,q.getY(),z);var s=p.level().getBlockState(at);if(!s.isAir()&&s.getDestroySpeed(p.level(),at)>=0&&p.gameMode.destroyBlock(at))n++;}if(n==0)return InteractionResult.PASS;c.getItemInHand().hurtAndBreak(Math.max(1,n/4),p,EquipmentSlot.MAINHAND);return InteractionResult.SUCCESS;}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal("Shift+ПКМ — первая точка; ПКМ — снять один горизонтальный слой."));}
}
