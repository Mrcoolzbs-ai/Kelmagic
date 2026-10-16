package dev.kelmagic.research;
import dev.kelmagic.*;import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.item.*;import net.minecraft.world.level.Level;import net.minecraft.server.level.ServerPlayer;import net.minecraft.network.chat.Component;import java.util.List;
public final class ResearchPaperItem extends Item {
 private final boolean discovery;public ResearchPaperItem(boolean d){super(new Properties().stacksTo(1));discovery=d;}
 @Override public Component getName(ItemStack s){String id=s.getOrDefault(MagicComponents.RESEARCH_ID.get(),"");var n=ResearchCatalog.BY_ID.get(id);String name=n==null?"Неизвестная запись":Research.name(n);return Component.literal((discovery?"Открытие: ":"Гипотеза: ")+name);}
 @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){var s=p.getItemInHand(h);if(p instanceof ServerPlayer sp){if(discovery)return Research.apply(sp,s)?InteractionResultHolder.success(s):InteractionResultHolder.fail(s);Research.status(sp,s);}return InteractionResultHolder.sidedSuccess(s,l.isClientSide);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.literal(discovery?"ПКМ — сохранить понимание в памяти.":"ПКМ — прочесть вопрос; с образцом подойти к столу архивариуса."));}
}
