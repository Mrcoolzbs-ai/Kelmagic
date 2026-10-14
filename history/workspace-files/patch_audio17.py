from pathlib import Path
r=Path('/data/KelMagic');j=r/'src/main/java/dev/kelmagic'
def put(p,s):q=j/p;q.parent.mkdir(parents=True,exist_ok=True);q.write_text(s)
p=r/'gradle.properties';p.write_text(p.read_text().replace('mod_version=1.6.0','mod_version=1.7.0'))
p=j/'KelMagic.java';p.write_text(p.read_text().replace('MagicContent.register(modEventBus);','MagicContent.register(modEventBus);\n        MagicAudio.register(modEventBus);'))
p=j/'MagicContent.java';p.write_text(p.read_text().replace('out.accept(SCRIPTER);','out.accept(SCRIPTER);out.accept(MagicAudio.CASSETTE);out.accept(MagicAudio.DECK_ITEM);out.accept(MagicAudio.PLAYER);'))
p=j/'MagicComponents.java';p.write_text(p.read_text().replace('    private MagicComponents()', '''    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> ACTIVE_TAPE=TYPES.register("active_tape",()->DataComponentType.<Integer>builder().persistent(Codec.intRange(0,7)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    private MagicComponents()'''))
put(Path('MagicAudio.java'),'''package dev.kelmagic;
import dev.kelmagic.audio.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
public final class MagicAudio {
 public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,KelMagic.MODID);
 public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,KelMagic.MODID);
 public static final DeferredItem<CassetteItem> CASSETTE=KelMagic.ITEMS.register("cassette",()->new CassetteItem(new Item.Properties().stacksTo(1)));
 public static final DeferredBlock<DeckBlock> DECK=KelMagic.BLOCKS.register("cassette_deck",()->new DeckBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(2.5f).sound(net.minecraft.world.level.block.SoundType.METAL)));
 public static final DeferredItem<BlockItem> DECK_ITEM=KelMagic.ITEMS.registerSimpleBlockItem(DECK);
 public static final DeferredItem<TapePlayerItem> PLAYER=KelMagic.ITEMS.register("cassette_player",()->new TapePlayerItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).component(MagicComponents.ACTIVE_TAPE.get(),0).component(net.minecraft.core.component.DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.EMPTY)));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<DeckEntity>> DECK_ENTITY=ENTITIES.register("cassette_deck",()->BlockEntityType.Builder.of(DeckEntity::new,DECK.get()).build(null));
 public static final DeferredHolder<MenuType<?>,MenuType<DeckMenu>> DECK_MENU=MENUS.register("cassette_deck",()->IMenuTypeExtension.create((id,inv,b)->new DeckMenu(id,inv,b.readBlockPos())));
 public static final DeferredHolder<MenuType<?>,MenuType<TapeMenu>> PLAYER_MENU=MENUS.register("cassette_player",()->IMenuTypeExtension.create((id,inv,b)->new TapeMenu(id,inv,b.readBoolean()?net.minecraft.world.InteractionHand.OFF_HAND:net.minecraft.world.InteractionHand.MAIN_HAND)));
 public static void register(IEventBus b){ENTITIES.register(b);MENUS.register(b);}
 private MagicAudio(){}
}
''')
put(Path('audio/CassetteItem.java'),'''package dev.kelmagic.audio;
import dev.kelmagic.MagicComponents;
import dev.kelmagic.item.ScripterItem;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
public final class CassetteItem extends Item {
 public CassetteItem(Properties p){super(p);}
 public static List<String> words(ItemStack s){return s.getOrDefault(MagicComponents.SPELL.get(),List.of());}
 public static boolean recorded(ItemStack s){return s.getItem() instanceof CassetteItem && ScripterItem.valid(words(s));}
 public static boolean blank(ItemStack s){return s.getItem() instanceof CassetteItem && words(s).isEmpty();}
 @Override public Component getName(ItemStack s){return Component.translatable(recorded(s)?"item.kelmagic.recorded_cassette":"item.kelmagic.cassette");}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){var w=words(s);t.add(Component.translatable(w.isEmpty()?"kelmagic.tape.blank":"kelmagic.tape.recorded"));if(!w.isEmpty()){t.add(Component.literal(String.join(" ",w)));t.add(Component.translatable("kelmagic.tape.cost",TapePlayerItem.cost(w)));}}
}
''')
put(Path('audio/TapePlayerItem.java'),'''package dev.kelmagic.audio;
import dev.kelmagic.*;
import dev.kelmagic.item.ScripterItem;
import dev.kelmagic.server.*;
import dev.kelmagic.core.*;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
public final class TapePlayerItem extends Item {
 public TapePlayerItem(Properties p){super(p);}
 public static int cost(List<String> w){return Math.max(1,(w.stream().mapToInt(String::length).sum()*3+9)/10);}
 public static List<ItemStack> tapes(ItemStack s){var out=new ArrayList<ItemStack>();var c=s.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY);for(int i=0;i<8;i++)out.add(i<c.getSlots()?c.getStackInSlot(i).copy():ItemStack.EMPTY);return out;}
 public static int active(ItemStack s){return Math.floorMod(s.getOrDefault(MagicComponents.ACTIVE_TAPE.get(),0),8);}
 public static void select(ServerPlayer p,int step){if(!p.isAlive()||p.isSpectator()||Math.abs(step)!=1)return;for(var h:InteractionHand.values()){var s=p.getItemInHand(h);if(s.getItem() instanceof TapePlayerItem){int i=Math.floorMod(active(s)+step,8);s.set(MagicComponents.ACTIVE_TAPE.get(),i);p.displayClientMessage(Component.translatable("kelmagic.tape.selected",i+1),true);p.inventoryMenu.broadcastChanges();return;}}}
 @Override public InteractionResultHolder<ItemStack> use(Level l,Player player,InteractionHand hand){var s=player.getItemInHand(hand);if(player instanceof ServerPlayer p){if(p.isSpectator())return InteractionResultHolder.fail(s);if(p.isShiftKeyDown()){p.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inv,owner)->new TapeMenu(id,inv,hand),Component.translatable("menu.kelmagic.cassette_player")),b->b.writeBoolean(hand==InteractionHand.OFF_HAND));}else{var tape=tapes(s).get(active(s));if(!CassetteItem.recorded(tape)||p.getData(MagicAttachments.SESSION).active()){p.displayClientMessage(Component.translatable("kelmagic.tape.unavailable"),true);return InteractionResultHolder.fail(s);}var words=CassetteItem.words(tape);var mana=p.getData(MagicAttachments.MANA);var spend=ManaMath.spend(Math.min(MagicServer.maximum(p),mana.current()),cost(words));p.setData(MagicAttachments.MANA,mana.withCurrent(spend.remaining()));Playback.run(p,"tape",()->{MagicServer.hurtMana(p,spend.healthDamage());if(p.isAlive()){var session=p.getData(MagicAttachments.SESSION);words.forEach(session::append);MagicServer.actionFromScripter(p,new dev.kelmagic.network.SpellAction(session.generation(),session.revision(),false));}});MagicServer.sync(p);}}return InteractionResultHolder.sidedSuccess(s,l.isClientSide);}
 @Override public void appendHoverText(ItemStack s,TooltipContext ctx,List<Component> tips,TooltipFlag flag){tips.add(Component.translatable("kelmagic.tape.controls"));int i=active(s);var t=tapes(s).get(i);tips.add(Component.translatable("kelmagic.tape.selected",i+1));if(CassetteItem.recorded(t)){tips.add(Component.literal(String.join(" ",CassetteItem.words(t))));tips.add(Component.translatable("kelmagic.tape.cost",cost(CassetteItem.words(t))));}}
}
''')
put(Path('audio/TapeMenu.java'),'''package dev.kelmagic.audio;
import dev.kelmagic.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
public final class TapeMenu extends AbstractContainerMenu {
 public final SimpleContainer tapes;
 public final ItemStack holder;
 private final Player owner;private final InteractionHand hand;private final int locked;
 public TapeMenu(int id,Inventory inv,InteractionHand hand){super(MagicAudio.PLAYER_MENU.get(),id);this.owner=inv.player;this.hand=hand;this.holder=owner.getItemInHand(hand);locked=hand==InteractionHand.MAIN_HAND?inv.selected:-1;
 tapes=new SimpleContainer(8){@Override public void setChanged(){super.setChanged();if(!owner.level().isClientSide && holder.getItem() instanceof TapePlayerItem)holder.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(getItems()));}};
 var contents=TapePlayerItem.tapes(holder);for(int i=0;i<8;i++)tapes.setItem(i,contents.get(i));
 for(int i=0;i<8;i++){final int index=i;addSlot(new Slot(tapes,i,24+(i%4)*38,54+(i/4)*29){@Override public boolean mayPlace(ItemStack s){return CassetteItem.recorded(s);}@Override public int getMaxStackSize(){return 1;}});}
 for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(inventorySlot(inv,x+y*9+9,8+x*18,126+y*18));
 for(int x=0;x<9;x++)addSlot(inventorySlot(inv,x,8+x*18,184));
 }
 private Slot inventorySlot(Inventory inv,int index,int x,int y){return new Slot(inv,index,x,y){@Override public boolean mayPickup(Player p){return index!=locked;}@Override public boolean mayPlace(ItemStack s){return index!=locked;}};}
 @Override public void clicked(int slot,int button,ClickType type,Player p){if(type==ClickType.SWAP&&(button==locked||button==40&&hand==InteractionHand.OFF_HAND))return;super.clicked(slot,button,type,p);}
 @Override public boolean stillValid(Player p){return p==owner && p.isAlive() && p.getItemInHand(hand)==holder && holder.getItem() instanceof TapePlayerItem;}
 @Override public ItemStack quickMoveStack(Player p,int idx){if(idx<0||idx>=slots.size())return ItemStack.EMPTY;var slot=slots.get(idx);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(idx<8){if(!moveItemStackTo(s,8,44,true))return ItemStack.EMPTY;}else if(!CassetteItem.recorded(s)||!moveItemStackTo(s,0,8,false))return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return copy;}
}
''')
put(Path('audio/DeckBlock.java'),'''package dev.kelmagic.audio;
import com.mojang.serialization.MapCodec;
import dev.kelmagic.MagicAudio;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class DeckBlock extends BaseEntityBlock {
 public static final MapCodec<DeckBlock> CODEC=simpleCodec(DeckBlock::new);
 public DeckBlock(Properties p){super(p);}
 @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
 @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new DeckEntity(p,s);}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult h){if(l.getBlockEntity(pos) instanceof DeckEntity d){if(p instanceof net.minecraft.server.level.ServerPlayer sp)sp.openMenu(d,b->b.writeBlockPos(pos));return InteractionResult.sidedSuccess(l.isClientSide);}return InteractionResult.PASS;}
 @Override protected void onRemove(BlockState old,Level l,BlockPos pos,BlockState next,boolean moving){if(!old.is(next.getBlock()) && l.getBlockEntity(pos) instanceof DeckEntity d)Containers.dropContents(l,pos,d.inventory);super.onRemove(old,l,pos,next,moving);}
 @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide?null:createTickerHelper(t,MagicAudio.DECK_ENTITY.get(),DeckEntity::tick);}
}
''')
put(Path('audio/DeckEntity.java'),'''package dev.kelmagic.audio;
import dev.kelmagic.*;
import dev.kelmagic.item.ScripterItem;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
public final class DeckEntity extends BlockEntity implements MenuProvider {
 public final SimpleContainer inventory=new SimpleContainer(3){@Override public void setChanged(){super.setChanged();DeckEntity.this.setChanged();}};
 public int progress,total;private List<String> recording=List.of();
 public final ContainerData data=new ContainerData(){public int get(int i){return i==0?progress:total;}public void set(int i,int v){if(i==0)progress=v;else total=v;}public int getCount(){return 2;}};
 public DeckEntity(BlockPos p,BlockState s){super(MagicAudio.DECK_ENTITY.get(),p,s);}
 public static int ticks(List<String> w){return w.size()*100;}
 public static void tick(Level l,BlockPos pos,BlockState s,DeckEntity d){d.advance();}
 public void advance(){
 var tape=inventory.getItem(0);var writer=inventory.getItem(1);var words=writer.getOrDefault(MagicComponents.SPELL.get(),List.<String>of());
 boolean valid=CassetteItem.blank(tape) && writer.getItem() instanceof ScripterItem && !writer.getOrDefault(MagicComponents.RECORDING.get(),false) && ScripterItem.valid(words) && inventory.getItem(2).isEmpty();
 if(!valid){if(progress!=0||total!=0){progress=total=0;recording=List.of();setChanged();}return;}
 if(!words.equals(recording)){recording=List.copyOf(words);progress=0;total=ticks(words);}
 if(++progress>=total){var result=tape.copyWithCount(1);result.set(MagicComponents.SPELL.get(),recording);inventory.setItem(2,result);inventory.removeItem(0,1);progress=total=0;recording=List.of();}setChanged();
 }
 @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider regs){super.saveAdditional(tag,regs);var list=new ListTag();for(int i=0;i<3;i++){var s=inventory.getItem(i);if(!s.isEmpty()){var t=new CompoundTag();t.putInt("Slot",i);t.put("Stack",s.save(regs));list.add(t);}}tag.put("Items",list);tag.putInt("Progress",progress);tag.putInt("Total",total);var words=new ListTag();for(String w:recording)words.add(StringTag.valueOf(w));tag.put("Recording",words);}
 @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider regs){super.loadAdditional(tag,regs);inventory.clearContent();var list=tag.getList("Items",Tag.TAG_COMPOUND);for(int i=0;i<list.size();i++){var t=list.getCompound(i);int slot=t.getInt("Slot");if(slot>=0&&slot<3)inventory.setItem(slot,ItemStack.parseOptional(regs,t.getCompound("Stack")));}progress=Math.max(0,Math.min(10000,tag.getInt("Progress")));total=Math.max(0,Math.min(10000,tag.getInt("Total")));var words=tag.getList("Recording",Tag.TAG_STRING);var out=new ArrayList<String>();for(int i=0;i<Math.min(100,words.size());i++)out.add(words.getString(i));recording=List.copyOf(out);}
 @Override public Component getDisplayName(){return Component.translatable("block.kelmagic.cassette_deck");}
 @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new DeckMenu(id,inv,this);}
}
''')
put(Path('audio/DeckMenu.java'),'''package dev.kelmagic.audio;
import dev.kelmagic.*;
import dev.kelmagic.item.ScripterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
public final class DeckMenu extends AbstractContainerMenu {
 public final Container machine;public final ContainerData data;private final ContainerLevelAccess access;
 public DeckMenu(int id,Inventory inv,BlockPos pos){this(id,inv,inv.player.level().getBlockEntity(pos) instanceof DeckEntity d?d:null,pos);}
 public DeckMenu(int id,Inventory inv,DeckEntity d){this(id,inv,d,d.getBlockPos());}
 private DeckMenu(int id,Inventory inv,DeckEntity d,BlockPos pos){super(MagicAudio.DECK_MENU.get(),id);machine=d==null?new SimpleContainer(3):d.inventory;data=d==null?new SimpleContainerData(2):d.data;access=ContainerLevelAccess.create(inv.player.level(),pos);
 addSlot(new Slot(machine,0,29,58){@Override public boolean mayPlace(ItemStack s){return CassetteItem.blank(s);}@Override public int getMaxStackSize(){return 1;}});
 addSlot(new Slot(machine,1,65,58){@Override public boolean mayPlace(ItemStack s){return s.getItem() instanceof ScripterItem && ScripterItem.valid(CassetteItem.words(s)) && !s.getOrDefault(MagicComponents.RECORDING.get(),false);}@Override public int getMaxStackSize(){return 1;}});
 addSlot(new Slot(machine,2,137,58){@Override public boolean mayPlace(ItemStack s){return false;}});
 for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inv,x+y*9+9,8+x*18,115+y*18));for(int x=0;x<9;x++)addSlot(new Slot(inv,x,8+x*18,173));addDataSlots(data);
 }
 @Override public boolean stillValid(Player p){return stillValid(access,p,MagicAudio.DECK.get());}
 @Override public ItemStack quickMoveStack(Player p,int idx){if(idx<0||idx>=slots.size())return ItemStack.EMPTY;var slot=slots.get(idx);if(!slot.hasItem())return ItemStack.EMPTY;var s=slot.getItem();var copy=s.copy();if(idx<3){if(!moveItemStackTo(s,3,39,true))return ItemStack.EMPTY;}else if(CassetteItem.blank(s)){if(!moveItemStackTo(s,0,1,false))return ItemStack.EMPTY;}else if(s.getItem() instanceof ScripterItem && dev.kelmagic.item.ScripterItem.valid(CassetteItem.words(s)) && !s.getOrDefault(MagicComponents.RECORDING.get(),false)){if(!moveItemStackTo(s,1,2,false))return ItemStack.EMPTY;}else return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,s);return copy;}
}
''')
put(Path('server/Playback.java'),'''package dev.kelmagic.server;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
public final class Playback {
 private static final Map<UUID,String> SOURCE=new HashMap<>();
 public static String source(ServerPlayer p){return SOURCE.getOrDefault(p.getUUID(),"");}
 public static void run(ServerPlayer p,String source,Runnable f){var old=SOURCE.put(p.getUUID(),source);try{f.run();}finally{if(old==null)SOURCE.remove(p.getUUID());else SOURCE.put(p.getUUID(),old);}}
 private Playback(){}
}
''')
# Playback context covers both mana debt and immediate self-harm.
p=j/'item/ScripterItem.java';s=p.read_text();s=s.replace('p.setData(MagicAttachments.MANA,mana.withCurrent(spend.remaining()));MagicServer.hurtMana(p,spend.healthDamage());','p.setData(MagicAttachments.MANA,mana.withCurrent(spend.remaining()));Playback.run(p,"scripter",()->{MagicServer.hurtMana(p,spend.healthDamage());').replace('p.getCooldowns().addCooldown(this,20);\n                }','p.getCooldowns().addCooldown(this,20);\n                }});');p.write_text(s)
p=j/'server/MagicDamage.java';s=p.read_text();s=s.replace('int variation=','String playback=Playback.source(p);if(!playback.isEmpty())kind=playback+"_"+kind;\n        final String messageKind=kind;\n        int variation=');s=s.replace('"+kind+".', '"+messageKind+".') # holder still original key computed before alias
s=s.replace('    private MagicDamage()', '''    public static DamageSource spell(ServerPlayer p,LivingEntity target){
        var key=ResourceKey.<DamageType>create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"spell"));
        String src=Playback.source(p);String id=src.isEmpty()?"spell":src+"_spell";
        return new DamageSource(p.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key),p,p){
            @Override public Component getLocalizedDeathMessage(LivingEntity e){return Component.translatable("death.attack.kelmagic."+id,e.getDisplayName(),p.getDisplayName());}
        };
    }
    private MagicDamage()''');p.write_text(s)
# Self damage cannot use vanilla indirectMagic(caster,caster): vanilla player self-PvP protection rejects it.
p=j/'server/ExpandedSpellEffects.java';s=p.read_text().replace('target.damageSources().indirectMagic(caster,caster)','target==caster?MagicDamage.source(caster,"spell"):MagicDamage.spell(caster,target)');s=s.replace('v=v.scale(Math.min(1.5,.25*power));','v=v.scale(op.action().equals("PULL")?Math.min(3.2,1.15*power):Math.min(1.5,.25*power));');p.write_text(s)
# Combat contact contagion, not ambient fire damage; respect actual accepted damage, PvP and water.
p=j/'server/ArcaneFire.java';s=p.read_text().replace(' private ArcaneFire(){}',''' @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
 public static void contact(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event){
  var victim=event.getEntity();var source=event.getSource();if(victim.level().isClientSide||source.is(DAMAGE)||!(source.getEntity() instanceof LivingEntity attacker)||attacker==victim||event.getNewDamage()<=0)return;
  if(attacker instanceof Player a && victim instanceof Player b && (!a.canHarmPlayer(b)||attacker.level().getServer()!=null&&!attacker.level().getServer().isPvpAllowed()))return;
  int a=attacker.getData(MagicAttachments.ARCANE_BURN),b=victim.getData(MagicAttachments.ARCANE_BURN);float ap=attacker.getData(MagicAttachments.ARCANE_POWER),bp=victim.getData(MagicAttachments.ARCANE_POWER);
  if(a>0&&!victim.isInWaterRainOrBubble())ignite(victim,a,ap);
  if(b>0&&!attacker.isInWaterRainOrBubble())ignite(attacker,b,bp);
 }
 private ArcaneFire(){}''');p.write_text(s)
put(Path('network/TapeSelect.java'),'''package dev.kelmagic.network;
import dev.kelmagic.KelMagic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record TapeSelect(int step) implements CustomPacketPayload {
 public static final Type<TapeSelect> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"tape_select"));
 public static final StreamCodec<RegistryFriendlyByteBuf,TapeSelect> CODEC=StreamCodec.of((b,p)->b.writeByte(p.step),b->new TapeSelect(b.readByte()));
 @Override public Type<TapeSelect> type(){return TYPE;}
}
''')
p=j/'network/SpellNetwork.java';s=p.read_text().replace('event.registrar("3")','event.registrar("4")');s=s.replace('registrar.playToClient(BurnSnapshot','registrar.playToServer(TapeSelect.TYPE,TapeSelect.CODEC,(packet,context)->context.enqueueWork(()->{if(context.player() instanceof ServerPlayer p && p.isShiftKeyDown())dev.kelmagic.audio.TapePlayerItem.select(p,packet.step());}));\n        registrar.playToClient(BurnSnapshot');p.write_text(s)
put(Path('client/AudioScreens.java'),'''package dev.kelmagic.client;
import dev.kelmagic.*;
import dev.kelmagic.audio.*;
import dev.kelmagic.network.TapeSelect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
public final class AudioScreens {
 @EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
 public static class Registration {@SubscribeEvent public static void screens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent e){e.register(MagicAudio.PLAYER_MENU.get(),PlayerScreen::new);e.register(MagicAudio.DECK_MENU.get(),DeckScreen::new);}}
 @EventBusSubscriber(modid=KelMagic.MODID,value=Dist.CLIENT)
 public static class Input {@SubscribeEvent public static void scroll(net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent e){var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null||!mc.player.isShiftKeyDown()||e.getScrollDeltaY()==0)return;if(mc.player.getMainHandItem().getItem() instanceof TapePlayerItem||mc.player.getOffhandItem().getItem() instanceof TapePlayerItem){e.setCanceled(true);net.neoforged.neoforge.network.PacketDistributor.sendToServer(new TapeSelect(e.getScrollDeltaY()>0?-1:1));}}}
 static ResourceLocation tex(String id){return ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"textures/gui/"+id+".png");}
 public static class PlayerScreen extends AbstractContainerScreen<TapeMenu>{
  public PlayerScreen(TapeMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=208;inventoryLabelY=114;titleLabelX=12;titleLabelY=9;}
  @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){g.blit(tex("tape_player"),leftPos,topPos,0,0,imageWidth,imageHeight,256,256);int i=TapePlayerItem.active(menu.holder),x=leftPos+22+(i%4)*38,y=topPos+52+(i/4)*29;g.renderOutline(x,y,20,20,0xFFF2CA76);g.drawString(font,Component.translatable("kelmagic.tape.selected",i+1),leftPos+12,topPos+30,0xEDCDA0,false);}
  @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,titleLabelX,titleLabelY,0xFFE8BF,false);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,0xBFA88A,false);}
  @Override public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);}
 }
 public static class DeckScreen extends AbstractContainerScreen<DeckMenu>{
  public DeckScreen(DeckMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=197;titleLabelX=12;titleLabelY=10;inventoryLabelY=103;}
  @Override protected void renderBg(GuiGraphics g,float p,int x,int y){g.blit(tex("tape_deck"),leftPos,topPos,0,0,imageWidth,imageHeight,256,256);int total=menu.data.get(1),progress=menu.data.get(0);g.fill(leftPos+23,topPos+84,leftPos+153,topPos+88,0xFF29262E);if(total>0)g.fill(leftPos+23,topPos+84,leftPos+23+(int)(130L*progress/total),topPos+88,0xFFD8AC62);g.drawString(font,Component.translatable(total>0?"kelmagic.deck.remaining":"kelmagic.deck.idle",Math.max(0,(total-progress+19)/20)),leftPos+12,topPos+29,0xE7CDA6,false);}
  @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,titleLabelX,titleLabelY,0xFFE8BF,false);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,0xBFA88A,false);}
  @Override public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);}
 }
}
''')
print('audio source files written')
