package dev.kelmagic.content;
import dev.kelmagic.*;
import dev.kelmagic.entity.InkMark;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.registries.DeferredHolder;
/** Server owns selection, charge consumption, permissions, replacement and persistence. */
public final class InkDrawings {
 public static final DeferredHolder<EntityType<?>,EntityType<InkMark>> TYPE=MagicCreatures.TYPES.register("ink_mark",()->EntityType.Builder.<InkMark>of(InkMark::new,MobCategory.MISC).sized(.9f,.9f).clientTrackingRange(5).updateInterval(20).build("kelmagic:ink_mark"));
 public static java.util.function.Consumer<InteractionHand> OPEN=hand->{};
 public static String selected(ItemStack s){return InkPatterns.get(s.getOrDefault(MagicComponents.INK_PATTERN.get(),"u2192")).id();}
 public static void select(ServerPlayer p,boolean offhand,String id){var s=p.getItemInHand(offhand?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND);if(p.isAlive()&&s.is(ArchiveStorage.INK.get())&&InkPatterns.BY_ID.containsKey(id)){s.set(MagicComponents.INK_PATTERN.get(),id);p.getInventory().setChanged();p.containerMenu.broadcastChanges();}}
 public static InteractionResult draw(UseOnContext c){var player=c.getPlayer();if(player==null||!player.isShiftKeyDown())return InteractionResult.PASS;var l=c.getLevel();var pos=c.getClickedPos();var face=c.getClickedFace();if(!player.mayBuild()||!player.mayUseItemAt(pos,face,c.getItemInHand())||!l.getWorldBorder().isWithinBounds(pos))return InteractionResult.FAIL;
  if(!Block.isFaceFull(l.getBlockState(pos).getShape(l,pos),face)){if(!l.isClientSide)player.displayClientMessage(net.minecraft.network.chat.Component.literal("Для рисунка нужна полная плоская грань блока."),true);return InteractionResult.FAIL;}
  if(l.isClientSide)return InteractionResult.SUCCESS;
  var p=(ServerPlayer)player;if(!p.canInteractWithBlock(pos,0))return InteractionResult.FAIL;var marks=l.getEntitiesOfClass(InkMark.class,new AABB(pos).inflate(.05),m->m.anchor().equals(pos)&&m.face()==face);
  if(marks.stream().anyMatch(m->!m.owned(p)&&!p.isCreative())){p.displayClientMessage(net.minecraft.network.chat.Component.literal("Это чужая запись."),true);return InteractionResult.FAIL;}
  String id=selected(c.getItemInHand());if(id.equals("erase")){marks.forEach(Entity::discard);return InteractionResult.CONSUME;}
  int rotation=rotationFor(face,p.getDirection());
  int color=ArchivistInkItem.color(c.getItemInHand()).getTextureDiffuseColor()&0xFFFFFF;if(marks.size()==1&&marks.getFirst().pattern().equals(id)&&marks.getFirst().inkColor()==color&&marks.getFirst().rotation()==rotation)return InteractionResult.CONSUME;
  var mark=new InkMark(TYPE.get(),l);mark.configure(pos,face,id,color,p.getUUID(),rotation);if(!l.addFreshEntity(mark))return InteractionResult.FAIL;marks.forEach(Entity::discard);
  c.getItemInHand().hurtAndBreak(1,p,LivingEntity.getSlotForHand(c.getHand()));l.playSound(null,pos,net.minecraft.sounds.SoundEvents.GLOW_INK_SAC_USE,net.minecraft.sounds.SoundSource.BLOCKS,.6f,1.2f);return InteractionResult.CONSUME;
 }
 public static int rotationFor(Direction face,Direction looking){return face==Direction.UP?Math.floorMod(2-looking.get2DDataValue(),4):face==Direction.DOWN?(looking.get2DDataValue()+2)%4:0;}
 public static void init(){}
 private InkDrawings(){}
}
