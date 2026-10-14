package dev.kelmagic.gametest;

import com.mojang.authlib.GameProfile;
import dev.kelmagic.core.*;
import dev.kelmagic.network.SpellAction;
import dev.kelmagic.server.*;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

/** Runs only under NeoForge's enabled GameTest harness; no gameplay slash commands added. */
@GameTestHolder("kelmagic")
@PrefixGameTestTemplate(false)
public final class KelGameTests {
    private static final class TestPlayer extends FakePlayer {
        TestPlayer(ServerLevel level) { super(level,new GameProfile(UUID.randomUUID(),"KelTest")); }
        @Override public boolean isInvulnerableTo(DamageSource source) { return false; }
        @Override public boolean canHarmPlayer(Player p) { return true; }
    }
    private static TestPlayer player(GameTestHelper h) {
        // GameTestServer disables doFireTick by default; fire behavior tests need it enabled.
        h.getLevel().getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOFIRETICK).set(true,h.getLevel().getServer());
        for(int x=0;x<8;x++) for(int z=0;z<8;z++) h.setBlock(x,0,z,Blocks.STONE);
        TestPlayer p=new TestPlayer(h.getLevel());p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        Vec3 at=h.absoluteVec(new Vec3(3.5,1,1.5));p.setPos(at);p.setYRot(0);p.setXRot(0);p.setHealth(20);
        return p;
    }
    private static int ConfigFire(){return dev.kelmagic.Config.FIRE_SECONDS.get()*60;}
    @GameTest(template="empty",timeoutTicks=60)
    public static void idleManaRecovery(GameTestHelper h){
        var p=player(h);p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(0,false));
        for(int i=0;i<199;i++)MagicServer.tick(new PlayerTickEvent.Post(p));
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==0,"idle recovery not early");
        MagicServer.tick(new PlayerTickEvent.Post(p));h.assertTrue(p.getData(MagicAttachments.MANA).current()==1,"idle recovery at 10 seconds without a spell");
        for(int i=0;i<1200;i++)MagicServer.tick(new PlayerTickEvent.Post(p));h.assertTrue(p.getData(MagicAttachments.MANA).current()==5,"idle recovery capped");
        p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(0,true));for(int i=0;i<100;i++)MagicServer.tick(new PlayerTickEvent.Post(p));h.assertTrue(p.getData(MagicAttachments.MANA).current()==1,"Heart idle recovery at 5 seconds");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void magicFireBurnsItems(GameTestHelper h){
        player(h);var pos=h.absolutePos(new BlockPos(3,1,3));var state=dev.kelmagic.MagicContent.MAGIC_FIRE.get().defaultBlockState();
        var item=new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),pos.getX()+.5,pos.getY(),pos.getZ()+.5,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK));
        for(int i=0;i<4;i++)state.entityInside(h.getLevel(),pos,item);h.assertTrue(item.isRemoved(),"ordinary items burn");
        var safe=new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),pos.getX()+.5,pos.getY(),pos.getZ()+.5,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.NETHERITE_INGOT));
        for(int i=0;i<8;i++)state.entityInside(h.getLevel(),pos,safe);h.assertFalse(safe.isRemoved(),"fire-resistant items keep vanilla immunity");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void magicFireEternalAndCampfire(GameTestHelper h){
        player(h);var pos=new BlockPos(3,1,3);h.setBlock(pos.below(),Blocks.NETHERRACK);
        h.setBlock(pos.east(),Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.LIT,false));
        var fire=dev.kelmagic.MagicContent.MAGIC_FIRE.get();h.setBlock(pos,fire.defaultBlockState().setValue(dev.kelmagic.block.MagicFireBlock.AGE,15));
        h.runAfterDelay(15,()->h.assertTrue(h.getBlockState(pos.east()).getValue(net.minecraft.world.level.block.CampfireBlock.LIT),"magic fire relights campfire"));
        h.runAfterDelay(80,()->{h.assertTrue(h.getBlockState(pos).is(fire),"aged magic fire persists on netherrack");h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void magicFireHonorsFireTick(GameTestHelper h){
        player(h);var pos=new BlockPos(3,1,3);var fire=dev.kelmagic.MagicContent.MAGIC_FIRE.get();h.setBlock(pos,fire.defaultBlockState());h.setBlock(pos.east(),Blocks.OAK_PLANKS);
        var rule=h.getLevel().getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOFIRETICK);boolean previous=rule.get();
        try{rule.set(false,h.getLevel().getServer());for(int i=0;i<80;i++)h.getBlockState(pos).tick(h.getLevel(),h.absolutePos(pos),net.minecraft.util.RandomSource.create(i));h.assertTrue(h.getBlockState(pos.east()).is(Blocks.OAK_PLANKS),"doFireTick=false stops burning/spread");h.assertTrue(h.getBlockState(pos).getValue(dev.kelmagic.block.MagicFireBlock.AGE)==0,"rule freezes fire aging");}
        finally{rule.set(previous,h.getLevel().getServer());}h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void magicFireConsumesWood(GameTestHelper h){
        player(h);var pos=new BlockPos(3,1,3);var fire=dev.kelmagic.MagicContent.MAGIC_FIRE.get();h.setBlock(pos,fire.defaultBlockState());h.setBlock(pos.east(),Blocks.OAK_PLANKS);
        var random=net.minecraft.util.RandomSource.create(42);for(int i=0;i<80 && h.getBlockState(pos.east()).is(Blocks.OAK_PLANKS);i++)h.getBlockState(pos).tick(h.getLevel(),h.absolutePos(pos),random);
        h.assertFalse(h.getBlockState(pos.east()).is(Blocks.OAK_PLANKS),"flammable block consumed by purple fire");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void magicFireWallShape(GameTestHelper h){
        player(h);var pos=new BlockPos(3,2,3);h.setBlock(pos.north(),Blocks.OAK_PLANKS);var state=dev.kelmagic.MagicContent.MAGIC_FIRE.get().stateAt(h.getLevel(),h.absolutePos(pos));
        h.assertTrue(state.getValue(net.minecraft.world.level.block.FireBlock.NORTH),"fire attaches to flammable wall");h.assertTrue(state.canSurvive(h.getLevel(),h.absolutePos(pos)),"wall fire supported without floor");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void arcaneFireResistanceAndWater(GameTestHelper h){
        var mob=h.spawnWithNoFreeWill(EntityType.ZOMBIE,3.5f,1,5.5f);mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.FIRE_RESISTANCE,600));
        mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);mob.setHealth(20);h.assertTrue(ArcaneFire.ignite(mob),"magic fire starts");
        h.assertTrue(mob.getData(MagicAttachments.ARCANE_BURN)==dev.kelmagic.Config.FIRE_SECONDS.get()*20*3,"threefold burn time");
        ArcaneFire.advance(mob);h.assertTrue(mob.getHealth()==19,"fire resistance does not stop magic damage");
        h.assertTrue(!new net.minecraft.world.damagesource.DamageSource(h.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(ArcaneFire.DAMAGE)).is(net.minecraft.tags.DamageTypeTags.IS_FIRE),"not falsely tagged vanilla fire");
        var p=player(h);p.gameMode.changeGameModeForPlayer(GameType.CREATIVE);h.assertFalse(ArcaneFire.ignite(p),"creative immune to magic fire");
        var tag=mob.saveWithoutId(new net.minecraft.nbt.CompoundTag());var copy=EntityType.ZOMBIE.create(h.getLevel());copy.load(tag);h.assertTrue(copy.getData(MagicAttachments.ARCANE_BURN)==ConfigFire()-1,"magic burn persists in entity data");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=80)
    public static void magicFireBlockLifetime(GameTestHelper h){
        var p=player(h);var pos=new BlockPos(3,1,3);var block=dev.kelmagic.MagicContent.MAGIC_FIRE.get();h.setBlock(pos,block.defaultBlockState());
        h.assertTrue(h.getBlockState(pos).canSurvive(h.getLevel(),h.absolutePos(pos)),"supported magic fire");
        h.setBlock(pos,block.defaultBlockState().setValue(dev.kelmagic.block.MagicFireBlock.AGE,15));
        h.runAfterDelay(50,()->{h.assertTrue(h.getBlockState(pos).isAir(),"aged magic fire extinguishes naturally");h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=130)
    public static void magicFireExactlyFiveSeconds(GameTestHelper h){
        player(h);var pos=new BlockPos(3,1,3);h.setBlock(pos,dev.kelmagic.MagicContent.MAGIC_FIRE.get());
        h.runAfterDelay(99,()->h.assertTrue(h.getBlockState(pos).is(dev.kelmagic.MagicContent.MAGIC_FIRE.get()),"block flame lives until 100 ticks"));
        h.runAfterDelay(101,()->{h.assertTrue(h.getBlockState(pos).isAir(),"block flame expires at 5 seconds");h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void pirDirectlyLightsCampfires(GameTestHelper h){
        var p=player(h);var pos=new BlockPos(3,1,3);
        for(var block:java.util.List.of(Blocks.CAMPFIRE,Blocks.SOUL_CAMPFIRE))for(var face:net.minecraft.core.Direction.values()){
            h.setBlock(pos,block.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.LIT,false));
            var absolute=h.absolutePos(pos);var hit=new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(absolute),face,absolute,false);
            h.assertTrue(SpellEffects.igniteBlockHit(p,hit),"pir lights targeted campfire from every face");
            h.assertTrue(h.getBlockState(pos).getValue(net.minecraft.world.level.block.CampfireBlock.LIT),"target itself is lit");
        }
        h.setBlock(pos,Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.LIT,false).setValue(net.minecraft.world.level.block.CampfireBlock.WATERLOGGED,true));
        var absolute=h.absolutePos(pos);var hit=new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(absolute),net.minecraft.core.Direction.UP,absolute,false);
        SpellEffects.igniteBlockHit(p,hit);
        h.assertFalse(h.getBlockState(pos).getValue(net.minecraft.world.level.block.CampfireBlock.LIT),"waterlogged campfire stays unlit");h.succeed();
    }
    private static void portalFrame(GameTestHelper h,boolean alongX){
        for(int u=0;u<4;u++)for(int y=0;y<5;y++){
            var at=alongX?new BlockPos(2+u,1+y,3):new BlockPos(3,1+y,2+u);
            h.setBlock(at,(u==0||u==3||y==0||y==4)?Blocks.OBSIDIAN:Blocks.AIR);
        }
    }
    private static void assertPortal(GameTestHelper h,boolean alongX){
        player(h);portalFrame(h,alongX);var pos=new BlockPos(3,2,3);
        h.setBlock(pos,dev.kelmagic.MagicContent.MAGIC_FIRE.get());
        h.runAfterDelay(5,()->{
            for(int u=1;u<=2;u++)for(int y=1;y<=3;y++){
                var at=alongX?new BlockPos(2+u,1+y,3):new BlockPos(3,1+y,2+u);
                var st=h.getBlockState(at);h.assertTrue(st.is(Blocks.NETHER_PORTAL),"full portal created by magic fire");
                h.assertTrue(st.getValue(net.minecraft.world.level.block.NetherPortalBlock.AXIS)==(alongX?net.minecraft.core.Direction.Axis.X:net.minecraft.core.Direction.Axis.Z),"correct portal axis");
            }h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void magicFireOpensPortalX(GameTestHelper h){assertPortal(h,true);}
    @GameTest(template="empty",timeoutTicks=40)
    public static void magicFireOpensPortalZ(GameTestHelper h){assertPortal(h,false);}
    @GameTest(template="empty",timeoutTicks=40)
    public static void magicFireRejectsBrokenPortal(GameTestHelper h){
        player(h);portalFrame(h,true);h.setBlock(new BlockPos(2,3,3),Blocks.AIR);
        var pos=new BlockPos(3,2,3);h.setBlock(pos,dev.kelmagic.MagicContent.MAGIC_FIRE.get());
        h.runAfterDelay(5,()->{h.assertFalse(h.getBlockState(pos).is(Blocks.NETHER_PORTAL),"incomplete frame cannot open");h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void pirOpensPortalFromUnsupportedSide(GameTestHelper h){
        var p=player(h);portalFrame(h,true);var edge=h.absolutePos(new BlockPos(2,3,3));
        var hit=new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(edge),net.minecraft.core.Direction.EAST,edge,false);
        h.assertTrue(SpellEffects.igniteBlockHit(p,hit),"pir may light valid portal above its floor, like vanilla flint and steel");
        h.runAfterDelay(5,()->{h.assertTrue(h.getBlockState(new BlockPos(3,3,3)).is(Blocks.NETHER_PORTAL),"side-ignited portal opens");h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void portalIgnoresDisabledFireTick(GameTestHelper h){
        player(h);portalFrame(h,true);var pos=new BlockPos(3,2,3);var fire=dev.kelmagic.MagicContent.MAGIC_FIRE.get();h.setBlock(pos,fire);
        var rule=h.getLevel().getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOFIRETICK);boolean old=rule.get();
        try{rule.set(false,h.getLevel().getServer());h.getBlockState(pos).tick(h.getLevel(),h.absolutePos(pos),net.minecraft.util.RandomSource.create(1));
            h.assertTrue(h.getBlockState(pos).is(Blocks.NETHER_PORTAL),"doFireTick=false does not disable portals");}
        finally{rule.set(old,h.getLevel().getServer());}h.succeed();
    }
    private static void cookedLoot(GameTestHelper h,EntityType<? extends net.minecraft.world.entity.Mob> type,net.minecraft.world.item.Item cooked,net.minecraft.world.item.Item raw,boolean lastTick){
        player(h);h.getLevel().getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT).set(true,h.getLevel().getServer());
        var mob=h.spawnWithNoFreeWill(type,3.5f,1,3.5f);mob.setHealth(1);mob.tickCount=0;
        mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.FIRE_RESISTANCE,600));
        ArcaneFire.ignite(mob);if(lastTick)mob.setData(MagicAttachments.ARCANE_BURN,1);ArcaneFire.advance(mob);
        h.assertFalse(mob.isAlive(),"magic fire still kills through fire resistance");
        var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,mob.getBoundingBox().inflate(2));
        h.assertTrue(drops.stream().anyMatch(e->e.getItem().is(cooked)),"real death drops cooked meat");
        h.assertFalse(drops.stream().anyMatch(e->e.getItem().is(raw)),"death does not drop raw meat");
        h.assertTrue(mob.getRemainingFireTicks()<=0,"no vanilla burn added");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void magicFireCooksPork(GameTestHelper h){cookedLoot(h,EntityType.PIG,net.minecraft.world.item.Items.COOKED_PORKCHOP,net.minecraft.world.item.Items.PORKCHOP,false);}
    @GameTest(template="empty",timeoutTicks=40)
    public static void magicFireCooksBeef(GameTestHelper h){cookedLoot(h,EntityType.COW,net.minecraft.world.item.Items.COOKED_BEEF,net.minecraft.world.item.Items.BEEF,false);}
    @GameTest(template="empty",timeoutTicks=40)
    public static void magicFireCooksChicken(GameTestHelper h){cookedLoot(h,EntityType.CHICKEN,net.minecraft.world.item.Items.COOKED_CHICKEN,net.minecraft.world.item.Items.CHICKEN,false);}
    @GameTest(template="empty",timeoutTicks=40)
    public static void magicFireCooksMuttonOnFinalTick(GameTestHelper h){cookedLoot(h,EntityType.SHEEP,net.minecraft.world.item.Items.COOKED_MUTTON,net.minecraft.world.item.Items.MUTTON,true);}
    @GameTest(template="empty",timeoutTicks=60)
    public static void manuscriptReveal(GameTestHelper h){
        var p=player(h);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,dev.kelmagic.MagicArchive.ANCIENT.toStack());
        dev.kelmagic.MagicArchive.ANCIENT.get().use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(p.getMainHandItem().getItem() instanceof dev.kelmagic.item.WordManuscriptItem,"ancient manuscript becomes one real word page");
        var page=(dev.kelmagic.item.WordManuscriptItem)p.getMainHandItem().getItem();h.assertTrue(dev.kelmagic.core.KelLexicon.WORDS.containsKey(page.word),"only known words revealed");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void codexCraftAndKeepNotes(GameTestHelper h){
        var book=dev.kelmagic.MagicArchive.CODEX.toStack();book.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,Component.literal("My archive"));
        book.set(dev.kelmagic.MagicComponents.CODEX_NOTES.get(),java.util.List.of("self", "", "", "", "", ""));
        var input=net.minecraft.world.item.crafting.CraftingInput.of(2,1,java.util.List.of(book,dev.kelmagic.MagicArchive.PAGES.get("mina").toStack()));
        var recipe=h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,h.getLevel()).orElseThrow().value();
        var result=recipe.assemble(input,h.getLevel().registryAccess());h.assertTrue(result.get(dev.kelmagic.MagicComponents.CODEX_WORDS.get()).equals(java.util.List.of("mina")),"real table adds word");
        h.assertTrue(result.get(dev.kelmagic.MagicComponents.CODEX_NOTES.get()).get(0).equals("self") && result.getHoverName().getString().equals("My archive"),"craft preserves annotations and custom name");
        var duplicate=net.minecraft.world.item.crafting.CraftingInput.of(2,1,java.util.List.of(result,dev.kelmagic.MagicArchive.PAGES.get("mina").toStack()));h.assertFalse(recipe.matches(duplicate,h.getLevel()),"duplicate paper not wasted");
        var saved=result.save(h.getLevel().registryAccess());var loaded=net.minecraft.world.item.ItemStack.parse(h.getLevel().registryAccess(),saved).orElseThrow();h.assertTrue(loaded.get(dev.kelmagic.MagicComponents.CODEX_WORDS.get()).equals(java.util.List.of("mina")),"codex knowledge persists");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void codexBothInkRecipes(GameTestHelper h){
        for(var ink:java.util.List.of(net.minecraft.world.item.Items.INK_SAC,net.minecraft.world.item.Items.GLOW_INK_SAC)){
            var input=net.minecraft.world.item.crafting.CraftingInput.of(2,2,java.util.List.of(dev.kelmagic.MagicContent.MANA_CORE.toStack(),new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOOK),new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FEATHER),new net.minecraft.world.item.ItemStack(ink)));
            var recipe=h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,h.getLevel()).orElseThrow().value();h.assertTrue(recipe.assemble(input,h.getLevel().registryAccess()).is(dev.kelmagic.MagicArchive.CODEX.get()),"real codex crafting accepts ink variant");
        }h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void codexNotesRequireLearnedHeldBook(GameTestHelper h){
        var p=player(h);var book=dev.kelmagic.MagicArchive.CODEX.toStack();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,book);
        dev.kelmagic.network.CodexNote.save(p,new dev.kelmagic.network.CodexNote(false,"mina","invalid before learning"));h.assertFalse(book.has(dev.kelmagic.MagicComponents.CODEX_NOTES.get()),"unknown word note rejected");
        book.set(dev.kelmagic.MagicComponents.CODEX_WORDS.get(),java.util.List.of("mina"));dev.kelmagic.network.CodexNote.save(p,new dev.kelmagic.network.CodexNote(false,"mina","my interpretation"));h.assertTrue(book.get(dev.kelmagic.MagicComponents.CODEX_NOTES.get()).get(0).equals("my interpretation"),"learned note saved server-side");
        dev.kelmagic.network.CodexNote.save(p,new dev.kelmagic.network.CodexNote(false,"pir","forged"));h.assertTrue(book.get(dev.kelmagic.MagicComponents.CODEX_NOTES.get()).get(3).isEmpty(),"cannot annotate another unknown word");h.succeed();
    }
    private static void word(TestPlayer p,String w) { MagicServer.chat(new ServerChatEvent(p,w,Component.literal(w))); }
    @GameTest(template="empty",timeoutTicks=40)
    public static void pir(GameTestHelper h) {
        var p=player(h);p.setXRot(45);
        h.assertTrue(SpellEffects.execute(p,SpellParser.parse(java.util.List.of("pir"))),"pir must place safe fire");
        boolean found=false;for(int x=0;x<8;x++) for(int z=0;z<8;z++) if(h.getBlockState(new BlockPos(x,1,z)).is(dev.kelmagic.MagicContent.MAGIC_FIRE.get())) found=true;
        h.assertTrue(found,"real fire block placed");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void silaPir(GameTestHelper h) {
        var p=player(h);var mob=h.spawnWithNoFreeWill(EntityType.ZOMBIE,3.5f,1,5.5f);
        h.assertTrue(SpellEffects.execute(p,SpellParser.parse(java.util.List.of("sila","pir"))),"aimed mob selected");
        h.assertTrue(mob.getData(MagicAttachments.ARCANE_BURN)==ConfigFire(),"mob receives fire status");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void tunaPir(GameTestHelper h) {
        var p=player(h);var target=new TestPlayer(h.getLevel());target.setPos(h.absoluteVec(new Vec3(3.5,1,5.5)));
        h.getLevel().addNewPlayer(target);
        boolean pvp=h.getLevel().getServer().isPvpAllowed();h.getLevel().getServer().setPvpAllowed(true);
        try {
            h.assertTrue(SpellEffects.execute(p,SpellParser.parse(java.util.List.of("tuna","pir"))),"aimed player selected");
            h.assertTrue(target.getData(MagicAttachments.ARCANE_BURN)==ConfigFire(),"player receives fire status");
        } finally { target.discard();h.getLevel().getServer().setPvpAllowed(pvp); }
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void minaNoPir(GameTestHelper h) {
        var p=player(h);
        h.assertTrue(SpellEffects.execute(p,SpellParser.parse(java.util.List.of("pir","mina","no"))),"permuted self protection");
        h.assertTrue(p.hasEffect(MobEffects.FIRE_RESISTANCE),"real fire resistance status");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void luTuna(GameTestHelper h) {
        h.assertTrue(SpellParser.parse(java.util.List.of("tuna","lu"))==SpellParser.Effect.SELECTION_ONLY,"no effect invented for selector alone");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void luPir(GameTestHelper h) {
        var p=player(h);p.setXRot(45);
        h.assertTrue(SpellEffects.execute(p,SpellParser.parse(java.util.List.of("pir","lu"))),"area fire");
        int fires=0;for(int x=0;x<8;x++) for(int z=0;z<8;z++) if(h.getBlockState(new BlockPos(x,1,z)).is(dev.kelmagic.MagicContent.MAGIC_FIRE.get())) fires++;
        h.assertTrue(fires>1 && fires<=16,"multiple bounded fire blocks");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void manaDebt(GameTestHelper h) {
        var p=player(h);word(p,"mina");word(p,"no");word(p,"pir");
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==0,"mana drained after each word");
        h.assertTrue(Math.abs(p.getHealth()-12)<0.001,"four hearts lost, cooldown bypassed; actual health="+p.getHealth());h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void waitingBetweenWords(GameTestHelper h) {
        var p=player(h);word(p,"mina");
        for(int i=0;i<200;i++) MagicServer.tick(new PlayerTickEvent.Post(p));
        word(p,"no");for(int i=0;i<600;i++) MagicServer.tick(new PlayerTickEvent.Post(p));word(p,"pir");
        h.assertTrue(p.getHealth()==20,"waiting avoids health debt");
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==0,"all recovered mana spent");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void cancelAndReplay(GameTestHelper h) {
        var p=player(h);word(p,"mina");var s=p.getData(MagicAttachments.SESSION);long token=s.generation();
        MagicServer.action(p,new SpellAction(token,1,true));
        h.assertTrue(!s.active(),"cancel clears sequence");
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==1,"cancel does not refund mana");
        MagicServer.action(p,new SpellAction(token,1,false));
        h.assertTrue(!p.hasEffect(MobEffects.FIRE_RESISTANCE),"stale action cannot cast");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void persistentManaCodec(GameTestHelper h) {
        var value=new MagicAttachments.Mana(11,true);
        var encoded=MagicAttachments.Mana.CODEC.encodeStart(NbtOps.INSTANCE,value).getOrThrow();
        var read=MagicAttachments.Mana.CODEC.parse(NbtOps.INSTANCE,encoded).getOrThrow();
        h.assertTrue(read.equals(value),"serializable mana and permanent upgrade survive codec roundtrip");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void ordinaryChat(GameTestHelper h) {
        var p=player(h);word(p,"hello mina pir");
        h.assertTrue(!p.getData(MagicAttachments.SESSION).active() && p.getData(MagicAttachments.MANA).current()==5,"ordinary sentences do not mutate magic");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void packetRoundtrip(GameTestHelper h) {
        var p=player(h);word(p,"mina");
        var value=MagicServer.snapshot(p,dev.kelmagic.network.SpellSnapshot.SYNC);
        var buf=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());
        try {
            dev.kelmagic.network.SpellSnapshot.CODEC.encode(buf,value);
            var decoded=dev.kelmagic.network.SpellSnapshot.CODEC.decode(buf);
            h.assertTrue(decoded.equals(value),"snapshot wire codec roundtrip");
            var action=new SpellAction(value.generation(),value.revision(),false);
            SpellAction.CODEC.encode(buf,action);
            h.assertTrue(SpellAction.CODEC.decode(buf).equals(action),"action wire codec roundtrip");
        } finally { buf.release(); }
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void word101(GameTestHelper h) {
        var p=player(h);
        for(int i=0;i<100;i++) { p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(5,false));word(p,"mina"); }
        h.assertTrue(p.isAlive() && p.getData(MagicAttachments.SESSION).revision()==100,"100 words allowed without automatic casting");
        boolean terrain=dev.kelmagic.Config.OVERLOAD_TERRAIN.get();
        try {
            dev.kelmagic.Config.OVERLOAD_TERRAIN.set(false);word(p,"mina");
            h.assertTrue(!p.isAlive() && !p.getData(MagicAttachments.SESSION).active(),"101st word kills caster and clears state");
        } finally { dev.kelmagic.Config.OVERLOAD_TERRAIN.set(terrain); }
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=40)
    public static void attachmentCopyOnDeath(GameTestHelper h) {
        var p=player(h);word(p,"mina");p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(11,true));
        var reborn=new TestPlayer(h.getLevel());reborn.restoreFrom(p,false);
        h.assertTrue(reborn.getData(MagicAttachments.MANA).equals(new MagicAttachments.Mana(11,true)),"persisted attachment copies on death");
        h.assertTrue(!reborn.getData(MagicAttachments.SESSION).active(),"transient sequence does not copy on death");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void luSilaPir(GameTestHelper h) {
        var p=player(h);var first=h.spawnWithNoFreeWill(EntityType.ZOMBIE,3.5f,1,5.5f);
        var second=h.spawnWithNoFreeWill(EntityType.ZOMBIE,5.5f,1,5.5f);
        h.assertTrue(SpellEffects.execute(p,SpellParser.parse(java.util.List.of("sila","lu","pir"))),"lu mob expansion");
        h.assertTrue(first.getData(MagicAttachments.ARCANE_BURN)==ConfigFire() && second.getData(MagicAttachments.ARCANE_BURN)==ConfigFire(),"both mobs ignited");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void pvpDisabled(GameTestHelper h) {
        var p=player(h);var target=new TestPlayer(h.getLevel());target.setPos(h.absoluteVec(new Vec3(3.5,1,5.5)));h.getLevel().addNewPlayer(target);
        boolean pvp=h.getLevel().getServer().isPvpAllowed();h.getLevel().getServer().setPvpAllowed(false);
        try {
            h.assertFalse(SpellEffects.execute(p,SpellParser.parse(java.util.List.of("tuna","pir"))),"server PvP policy respected");
            h.assertTrue(target.getData(MagicAttachments.ARCANE_BURN)==0,"target not burning");
        } finally { target.discard();h.getLevel().getServer().setPvpAllowed(pvp); }
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=40)
    public static void creativeManaDebt(GameTestHelper h) {
        var p=player(h);p.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        word(p,"mina");word(p,"no");word(p,"pir");
        h.assertTrue(p.getHealth()==20,"creative must not take mana damage, even from a fake player overriding invulnerability");
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==0,"creative still accounts mana without health debt");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void manaNoKnockback(GameTestHelper h) {
        var p=player(h);p.setDeltaMovement(new Vec3(.125,.05,-.1));
        var before=p.getDeltaMovement();p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(0,false));word(p,"mina");
        h.assertTrue(p.getHealth()==12,"mana damage still applies");
        h.assertTrue(p.getDeltaMovement().equals(before),"no mana impulse: existing movement unchanged");
        h.assertTrue(MagicDamage.source(p,"mana_debt").is(net.minecraft.tags.DamageTypeTags.NO_IMPACT),"damage type no_impact tag");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void respawnRestoresMana(GameTestHelper h) {
        var p=player(h);p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(0,true));word(p,"no");
        MagicServer.respawn(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent(p,false));
        h.assertTrue(p.getData(MagicAttachments.MANA).equals(new MagicAttachments.Mana(15,true)),"death restores full upgraded mana without losing upgrade");
        h.assertTrue(!p.getData(MagicAttachments.SESSION).active(),"respawn clears preparation");
        p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(7,true));
        MagicServer.respawn(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent(p,true));
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==7,"End return is not a mana refill exploit");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void creativeOverload(GameTestHelper h) {
        var p=player(h);p.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        boolean terrain=dev.kelmagic.Config.OVERLOAD_TERRAIN.get();
        try {
            dev.kelmagic.Config.OVERLOAD_TERRAIN.set(false);
            for(int i=0;i<101;i++) word(p,"mina");
            h.assertTrue(p.isAlive() && p.getHealth()==20,"creative overload must not force-kill");
            h.assertTrue(!p.getData(MagicAttachments.SESSION).active(),"overload still clears creative preparation");
        } finally {dev.kelmagic.Config.OVERLOAD_TERRAIN.set(terrain);}
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=60)
    public static void manaEquipment(GameTestHelper h) {
        var p=player(h);
        for(var it:dev.kelmagic.MagicContent.PLATE_ITEMS) p.setItemSlot(it.get().getEquipmentSlot(),it.toStack());
        h.assertTrue(MagicServer.maximum(p)==9,"four plates give +4 mana");
        for(var it:dev.kelmagic.MagicContent.ROBE_ITEMS) p.setItemSlot(it.get().getEquipmentSlot(),it.toStack());
        h.assertTrue(MagicServer.maximum(p)==13,"four robes give +8 mana");
        p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(23,true));
        h.assertTrue(MagicServer.maximum(p)==23,"dragon heart stacks with four robes");
        for(var it:dev.kelmagic.MagicContent.ROBE_ITEMS) p.setItemSlot(it.get().getEquipmentSlot(),net.minecraft.world.item.ItemStack.EMPTY);
        MagicServer.tick(new PlayerTickEvent.Post(p));
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==15,"unequipping gear clamps to new maximum");
        h.assertTrue(dev.kelmagic.MagicContent.PLATE.value().defense().equals(net.minecraft.world.item.ArmorMaterials.IRON.value().defense())==false,"only requested player slots configured");
        for(var type:java.util.List.of(net.minecraft.world.item.ArmorItem.Type.HELMET,net.minecraft.world.item.ArmorItem.Type.CHESTPLATE,net.minecraft.world.item.ArmorItem.Type.LEGGINGS,net.minecraft.world.item.ArmorItem.Type.BOOTS)) {
            h.assertTrue(dev.kelmagic.MagicContent.PLATE.value().getDefense(type)==net.minecraft.world.item.ArmorMaterials.IRON.value().getDefense(type),"iron-equivalent plate defense");
            h.assertTrue(dev.kelmagic.MagicContent.ROBE.value().getDefense(type)==net.minecraft.world.item.ArmorMaterials.LEATHER.value().getDefense(type),"leather-equivalent robe defense");
        }
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void dragonHeartItem(GameTestHelper h) {
        var p=player(h);var heart=dev.kelmagic.MagicContent.DRAGON_HEART.toStack();
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,heart);
        dev.kelmagic.MagicContent.DRAGON_HEART.get().use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(heart.isEmpty() && p.getData(MagicAttachments.MANA).dragonUpgrade(),"real heart item consumed and upgrade persisted");
        h.assertTrue(MagicServer.maximum(p)==15,"permanent base becomes 15");
        var duplicate=dev.kelmagic.MagicContent.DRAGON_HEART.toStack();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,duplicate);
        dev.kelmagic.MagicContent.DRAGON_HEART.get().use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(duplicate.getCount()==1 && MagicServer.maximum(p)==15,"repeat heart cannot stack upgrade or consume item");
        word(p,"mina");for(int i=0;i<100;i++) MagicServer.tick(new PlayerTickEvent.Post(p));
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==2,"upgraded regeneration in 100 ticks");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void bottledMana(GameTestHelper h) {
        var p=player(h);p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(1,true));
        var potion=net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION,dev.kelmagic.MagicContent.MANA);
        var result=net.minecraft.world.item.Items.POTION.finishUsingItem(potion,h.getLevel(),p);
        h.assertTrue(result.is(net.minecraft.world.item.Items.GLASS_BOTTLE),"drinking consumes potion and returns glass bottle");
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==11,"drink restores exactly 10 mana");
        h.assertTrue(p.getEffect(dev.kelmagic.MagicContent.FLOW).getDuration()==1800,"flow lasts 1:30");
        word(p,"mina");for(int i=0;i<60;i++) MagicServer.tick(new PlayerTickEvent.Post(p));
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==8,"flow regeneration every 60 ticks");
        var longPotion=net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION,dev.kelmagic.MagicContent.LONG_MANA);
        net.minecraft.world.item.Items.POTION.finishUsingItem(longPotion,h.getLevel(),p);
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==15,"restoration clamps at current max");
        h.assertTrue(p.getEffect(dev.kelmagic.MagicContent.FLOW).getDuration()==4800,"redstone extends flow to 4:00");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void realBrewingStand(GameTestHelper h) {
        var pos=new BlockPos(3,1,3);h.setBlock(pos,Blocks.BREWING_STAND);
        var stand=(net.minecraft.world.level.block.entity.BrewingStandBlockEntity)h.getBlockEntity(pos);
        for(int i=0;i<3;i++) stand.setItem(i,net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION,net.minecraft.world.item.alchemy.Potions.AWKWARD));
        stand.setItem(3,dev.kelmagic.MagicContent.MANA_CORE.toStack());stand.setItem(4,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BLAZE_POWDER));
        for(int i=0;i<401;i++) net.minecraft.world.level.block.entity.BrewingStandBlockEntity.serverTick(h.getLevel(),h.absolutePos(pos),h.getBlockState(pos),stand);
        for(int i=0;i<3;i++) h.assertTrue(stand.getItem(i).get(net.minecraft.core.component.DataComponents.POTION_CONTENTS).is(dev.kelmagic.MagicContent.MANA),"real stand brews mana in all three slots");
        h.assertTrue(stand.getItem(3).isEmpty() && stand.getItem(4).isEmpty(),"core and blaze fuel consumed");
        stand.setItem(3,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.REDSTONE));
        for(int i=0;i<401;i++) net.minecraft.world.level.block.entity.BrewingStandBlockEntity.serverTick(h.getLevel(),h.absolutePos(pos),h.getBlockState(pos),stand);
        for(int i=0;i<3;i++) h.assertTrue(stand.getItem(i).get(net.minecraft.core.component.DataComponents.POTION_CONTENTS).is(dev.kelmagic.MagicContent.LONG_MANA),"real redstone brewing upgrade");
        h.assertTrue(stand.getItem(3).isEmpty(),"redstone consumed");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void dyeableRobe(GameTestHelper h) {
        var stack=dev.kelmagic.MagicContent.ROBE_ITEMS.get(1).toStack();
        var input=net.minecraft.world.item.crafting.CraftingInput.of(2,1,java.util.List.of(stack,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.RED_DYE)));
        var recipe=h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,h.getLevel()).orElseThrow();
        var colored=recipe.value().assemble(input,h.getLevel().registryAccess());
        h.assertTrue(colored.is(stack.getItem()) && colored.has(net.minecraft.core.component.DataComponents.DYED_COLOR),"vanilla dye recipe works for robe");
        var saved=colored.save(h.getLevel().registryAccess());var loaded=net.minecraft.world.item.ItemStack.parse(h.getLevel().registryAccess(),saved).orElseThrow();
        h.assertTrue(loaded.get(net.minecraft.core.component.DataComponents.DYED_COLOR).equals(colored.get(net.minecraft.core.component.DataComponents.DYED_COLOR)),"dye color persists in item data");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void scripterRecordAndCast(GameTestHelper h) {
        var p=player(h);p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(15,true));
        var item=dev.kelmagic.MagicContent.SCRIPTER.get();var stack=dev.kelmagic.MagicContent.SCRIPTER.toStack();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,stack);
        item.use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(stack.getOrDefault(dev.kelmagic.MagicComponents.RECORDING.get(),false),"first click arms recorder");
        word(p,"mina");word(p,"no");word(p,"pir");
        h.assertTrue(!stack.has(dev.kelmagic.MagicComponents.CANDIDATE.get()),"not captured until completed");
        var s=p.getData(MagicAttachments.SESSION);MagicServer.action(p,new SpellAction(s.generation(),s.revision(),false));
        h.assertTrue(stack.get(dev.kelmagic.MagicComponents.CANDIDATE.get()).equals(java.util.List.of("mina","no","pir")),"own completed chat spell captured");
        h.assertTrue(stack.getOrDefault(dev.kelmagic.MagicComponents.SPELL.get(),java.util.List.<String>of()).isEmpty(),"capture is not saved until second click");
        dev.kelmagic.item.ScripterItem.capture(p,java.util.List.of("pir"));
        h.assertTrue(stack.get(dev.kelmagic.MagicComponents.CANDIDATE.get()).size()==3,"first completed spell locked");
        item.use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(stack.get(dev.kelmagic.MagicComponents.SPELL.get()).equals(java.util.List.of("mina","no","pir")),"second click saves");
        h.assertTrue(!stack.has(dev.kelmagic.MagicComponents.RECORDING.get()),"save ends recording");
        p.removeEffect(MobEffects.FIRE_RESISTANCE);
        p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(15,true));p.setShiftKeyDown(true);
        item.use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(p.hasEffect(MobEffects.FIRE_RESISTANCE),"scripter casts real effect");
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==10,"half of 9 rounds up to 5 mana");
        h.assertTrue(!s.active(),"scripter sequence cleared");
        item.use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==10,"server cooldown prevents repeated immediate casts");
        var saved=stack.save(h.getLevel().registryAccess());var loaded=net.minecraft.world.item.ItemStack.parse(h.getLevel().registryAccess(),saved).orElseThrow();
        h.assertTrue(loaded.get(dev.kelmagic.MagicComponents.SPELL.get()).equals(stack.get(dev.kelmagic.MagicComponents.SPELL.get())),"recorded spell persists on item for trade/rejoin");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void scripterCancelIsolation(GameTestHelper h) {
        var p=player(h);var other=player(h);var item=dev.kelmagic.MagicContent.SCRIPTER.get();
        var stack=dev.kelmagic.MagicContent.SCRIPTER.toStack();stack.set(dev.kelmagic.MagicComponents.SPELL.get(),java.util.List.of("pir"));
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,stack);
        item.use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        dev.kelmagic.item.ScripterItem.capture(other,java.util.List.of("mina","no","pir"));
        h.assertTrue(!stack.has(dev.kelmagic.MagicComponents.CANDIDATE.get()),"another player's spell is not captured");
        word(p,"pir");var ss=p.getData(MagicAttachments.SESSION);MagicServer.action(p,new SpellAction(ss.generation(),ss.revision(),true));
        h.assertTrue(!stack.has(dev.kelmagic.MagicComponents.CANDIDATE.get()),"cancel does not record");
        dev.kelmagic.item.ScripterItem.capture(p,java.util.List.of("no"));
        h.assertTrue(!stack.has(dev.kelmagic.MagicComponents.CANDIDATE.get()),"incomplete selector does not record");
        item.use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(stack.get(dev.kelmagic.MagicComponents.SPELL.get()).equals(java.util.List.of("pir")),"empty recording cancellation preserves saved spell");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void undefinedFireworkDamage(GameTestHelper h) {
        var p=player(h);var zombie=EntityType.ZOMBIE.create(h.getLevel());zombie.setPos(p.position().add(.5,0,0));zombie.setNoAi(true);h.getLevel().addFreshEntity(zombie);
        SpellEffects.fizzle(p,"kelmagic.undefined");
        var rockets=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.FireworkRocketEntity.class,p.getBoundingBox().inflate(2));
        h.assertTrue(rockets.size()==1,"undefined spell creates a real rocket");
        var rocket=rockets.get(0);for(int i=0;i<26 && !rocket.isRemoved();i++) rocket.tick();
        h.assertTrue(zombie.getHealth()<20,"explosive star damages nearby living entity");
        SpellEffects.fizzle(p,"kelmagic.no_target");
        h.assertTrue(h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.FireworkRocketEntity.class,p.getBoundingBox().inflate(2)).stream().noneMatch(e->!e.isRemoved()),"missing target feedback remains harmless");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void rejectForgedScripter(GameTestHelper h) {
        var p=player(h);var stack=dev.kelmagic.MagicContent.SCRIPTER.toStack();stack.set(dev.kelmagic.MagicComponents.SPELL.get(),java.util.List.of("fake"));
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,stack);p.setShiftKeyDown(true);
        dev.kelmagic.MagicContent.SCRIPTER.get().use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==5 && !p.getData(MagicAttachments.SESSION).active(),"forged unknown spell rejected before mana spend");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void stage2CraftingRecipes(GameTestHelper h) {
        var registry=net.minecraft.core.registries.BuiltInRegistries.ITEM;
        for(String name:java.util.List.of("mana_core","magic_ingot","magic_cloth","dragon_heart","scripter","arcane_helmet","arcane_chestplate","arcane_leggings","arcane_boots","mage_helmet","mage_chestplate","mage_leggings","mage_boots")) {
            var id=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic",name);
            var holder=h.getLevel().getRecipeManager().byKey(id).orElseThrow();
            var recipe=(net.minecraft.world.item.crafting.ShapedRecipe)holder.value();
            var grid=recipe.getIngredients().stream().map(ingredient->ingredient.isEmpty()?net.minecraft.world.item.ItemStack.EMPTY:ingredient.getItems()[0].copy()).toList();
            var input=net.minecraft.world.item.crafting.CraftingInput.of(recipe.getWidth(),recipe.getHeight(),grid);
            h.assertTrue(recipe.matches(input,h.getLevel()),"recipe input matches "+name);
            var result=recipe.assemble(input,h.getLevel().registryAccess());
            h.assertTrue(result.is(registry.get(id)),"real recipe output "+name);
            h.assertTrue(result.getCount()==(name.equals("magic_ingot") || name.equals("magic_cloth")?8:1),"correct recipe yield "+name);
        }
        h.succeed();
    }

    @GameTest(template="empty",batch="destructive_overload",timeoutTicks=100)
    public static void overloadTenRealBlasts(GameTestHelper h) {
        var p=player(h);p.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        int cx=p.blockPosition().getX()>>4,cz=p.blockPosition().getZ()>>4;
        // Fixture preloads the test area; the production overload code never requests chunk loads.
        for(int x=cx-4;x<=cx+4;x++) for(int z=cz-4;z<=cz+4;z++) h.getLevel().getChunk(x,z);
        OverloadEffects.clear();long before=OverloadEffects.totalBlasts();
        boolean terrain=dev.kelmagic.Config.OVERLOAD_TERRAIN.get();int bursts=dev.kelmagic.Config.OVERLOAD_BURSTS.get();
        try {
            dev.kelmagic.Config.OVERLOAD_TERRAIN.set(true);dev.kelmagic.Config.OVERLOAD_BURSTS.set(10);
            OverloadEffects.trigger(p);
            for(int i=0;i<101;i++) OverloadEffects.tick();
            h.assertTrue(OverloadEffects.totalBlasts()-before==10,"ten actual terrain explosions, not just visual particles");
            h.assertTrue(OverloadEffects.pendingJobs()==0,"burst queue cleaned after finishing");
            dev.kelmagic.KelMagic.LOGGER.info("OVERLOAD PROFILE: 10 real blasts, max single blast {} ms",OverloadEffects.maxBlastNanos()/1_000_000.0);
        } finally {
            dev.kelmagic.Config.OVERLOAD_TERRAIN.set(terrain);dev.kelmagic.Config.OVERLOAD_BURSTS.set(bursts);OverloadEffects.clear();
        }
        h.succeed();
    }

}
