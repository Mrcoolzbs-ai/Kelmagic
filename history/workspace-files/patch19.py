from pathlib import Path
r=Path('/data/KelMagic');j=r/'src/main/java/dev/kelmagic'
def put(n,s):p=j/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s)
def sub(n,a,b):p=j/n;s=p.read_text();assert a in s,(n,a);p.write_text(s.replace(a,b))
p=r/'gradle.properties';p.write_text(p.read_text().replace('mod_version=1.8.0','mod_version=1.9.0'))
put('core/WordRarity.java','''package dev.kelmagic.core;
import java.util.*;
import net.minecraft.world.item.Rarity;
public final class WordRarity {
 public static final Map<String,Rarity> ALL=load();
 private static Map<String,Rarity> load(){var out=new LinkedHashMap<String,Rarity>();for(var e:KelLexicon.resource("rarities").getAsJsonObject("words").entrySet())out.put(e.getKey(),Rarity.valueOf(e.getValue().getAsString().toUpperCase(Locale.ROOT)));if(out.size()!=180)throw new IllegalStateException("Every word needs rarity");return Collections.unmodifiableMap(out);}
 public static Rarity of(String word){return ALL.getOrDefault(word,Rarity.COMMON);}
 public static String label(String w){return switch(of(w)){case COMMON->"Обычное";case UNCOMMON->"Необычное";case RARE->"Редкое";case EPIC->"Эпическое";};}
 private WordRarity(){}
}''')
sub('MagicArchive.java','new Item.Properties().stacksTo(1),w','new Item.Properties().stacksTo(1).rarity(dev.kelmagic.core.WordRarity.of(w)),w')
sub('MagicArchive.java','new AncientManuscriptItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))','new AncientManuscriptItem(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON))')
sub('item/AncientManuscriptItem.java','var keys=new ArrayList<>(MagicArchive.PAGES.keySet());','var keys=new ArrayList<>(MagicArchive.PAGES.keySet().stream().filter(w->dev.kelmagic.core.WordRarity.of(w)==s.getOrDefault(net.minecraft.core.component.DataComponents.RARITY,Rarity.COMMON)).toList());if(keys.isEmpty())return InteractionResultHolder.fail(s);')
sub('item/WordManuscriptItem.java','t.add(Component.translatable("kelmagic.page_hint"));','t.add(Component.literal("Редкость: "+dev.kelmagic.core.WordRarity.label(word)));t.add(Component.translatable("kelmagic.page_hint"));')
sub('server/ExpandedSpellEffects.java','(scan && p.selector().isEmpty() || p.selector().equals("mina"))','(scan || p.selector().equals("mina"))')
sub('server/ExpandedSpellEffects.java','list.removeIf(e->{var hit=','if(!scan)list.removeIf(e->{var hit=')
sub('MagicComponents.java','    private MagicComponents() {}','''    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> CODEX_PAGE=TYPES.register("codex_page",()->DataComponentType.<Integer>builder().persistent(Codec.intRange(0,179)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> CODEX_SECTION=TYPES.register("codex_section",()->DataComponentType.<Integer>builder().persistent(Codec.intRange(0,1)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<String>> CODEX_QUERY=TYPES.register("codex_query",()->DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<String>> CODEX_TOKEN=TYPES.register("codex_token",()->DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    private MagicComponents() {}''')
put('network/CodexPosition.java','''package dev.kelmagic.network;
import dev.kelmagic.*;
import net.minecraft.network.*;import net.minecraft.network.codec.*;import net.minecraft.network.protocol.common.custom.CustomPacketPayload;import net.minecraft.resources.ResourceLocation;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.InteractionHand;
public record CodexPosition(boolean offhand,String token,int section,int page,String query) implements CustomPacketPayload {
 public static final Type<CodexPosition> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("kelmagic","codex_position"));
 public static final StreamCodec<RegistryFriendlyByteBuf,CodexPosition> CODEC=StreamCodec.of((b,p)->{b.writeBoolean(p.offhand);b.writeUtf(p.token,36);b.writeVarInt(p.section);b.writeVarInt(p.page);b.writeUtf(p.query,40);},b->new CodexPosition(b.readBoolean(),b.readUtf(36),b.readVarInt(),b.readVarInt(),b.readUtf(40)));
 public Type<CodexPosition> type(){return TYPE;}
 public static void save(ServerPlayer p,CodexPosition v){var s=p.getItemInHand(v.offhand?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND);if(!p.isAlive()||!(s.getItem() instanceof dev.kelmagic.item.CodexItem)||!v.token.equals(s.get(MagicComponents.CODEX_TOKEN.get()))||v.section<0||v.section>1||v.page<0||v.page>179||v.query.length()>40)return;s.set(MagicComponents.CODEX_SECTION.get(),v.section);s.set(MagicComponents.CODEX_PAGE.get(),v.page);s.set(MagicComponents.CODEX_QUERY.get(),v.query);}
}''')
sub('network/CodexOpen.java','boolean offhand) implements','boolean offhand,int section,int page,String query,String token) implements')
sub('network/CodexOpen.java',' public static final Type<CodexOpen>',' public CodexOpen(List<String> w,List<String> n,boolean h){this(w,n,h,0,0,"","");}\n public static final Type<CodexOpen>')
sub('network/CodexOpen.java','b.writeBoolean(p.offhand);','b.writeBoolean(p.offhand);b.writeVarInt(p.section);b.writeVarInt(p.page);b.writeUtf(p.query,40);b.writeUtf(p.token,36);')
sub('network/CodexOpen.java','read(b,240),b.readBoolean()))','read(b,240),b.readBoolean(),b.readVarInt(),b.readVarInt(),b.readUtf(40),b.readUtf(36)))')
sub('item/CodexItem.java','  if(p instanceof ServerPlayer sp) net.', '  if(p instanceof ServerPlayer&&!s.has(MagicComponents.CODEX_TOKEN.get()))s.set(MagicComponents.CODEX_TOKEN.get(),UUID.randomUUID().toString());\n  if(p instanceof ServerPlayer sp) net.')
sub('item/CodexItem.java','h==InteractionHand.OFF_HAND));','h==InteractionHand.OFF_HAND,s.getOrDefault(MagicComponents.CODEX_SECTION.get(),0),s.getOrDefault(MagicComponents.CODEX_PAGE.get(),0),s.getOrDefault(MagicComponents.CODEX_QUERY.get(),""),s.getOrDefault(MagicComponents.CODEX_TOKEN.get(),"")));')
sub('network/SpellNetwork.java','event.registrar("5")','event.registrar("6")')
sub('network/SpellNetwork.java','        registrar.playToServer(TapeSelect.TYPE','        registrar.playToServer(CodexPosition.TYPE,CodexPosition.CODEC,(packet,context)->context.enqueueWork(()->{if(context.player() instanceof ServerPlayer p)CodexPosition.save(p,packet);}));\n        registrar.playToServer(TapeSelect.TYPE')
sub('client/CodexScreen.java','private final boolean offhand;','private final boolean offhand;private final String token;')
sub('client/CodexScreen.java','offhand=p.offhand();}','offhand=p.offhand();token=p.token();section=Math.clamp(p.section(),0,1);page=Math.clamp(p.page(),0,total()-1);query=p.query();}')
sub('client/CodexScreen.java',' @Override public boolean isPauseScreen()', ' @Override public void removed(){if(note!=null&&section==0&&known.contains(WORDS.get(page))){notes.set(page,note.getValue());PacketDistributor.sendToServer(new CodexNote(offhand,WORDS.get(page),note.getValue()));}PacketDistributor.sendToServer(new CodexPosition(offhand,token,section,page,query));super.removed();}\n @Override public boolean isPauseScreen()')
sub('client/CodexScreen.java','text(g,"Значение: "+KelLexicon.WORDS.get(w).translation(),25,91,132);','text(g,"Значение: "+KelLexicon.WORDS.get(w).translation(),25,91,132);text(g,WordRarity.label(w),25,118,132);')
sub('client/CodexScreen.java','и кодекс на верстаке','и кодекс на столе архивариуса')
sub('client/CodexScreen.java','Кодекс + лист слова на верстаке','Кодекс + лист слова на столе архивариуса')
for n in ['add_manuscript.json','copy_codex.json']:
 p=r/'src/main/resources/data/kelmagic/recipe'/n
 if p.exists():p.unlink()
