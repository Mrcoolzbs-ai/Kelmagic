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
import net.minecraft.server.level.ServerPlayer;
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
        // FakePlayer suppresses native menu opening; keep real server menus in the fixture.
        @Override public java.util.OptionalInt openMenu(net.minecraft.world.MenuProvider provider) {containerMenu=provider.createMenu(33,getInventory(),this);return containerMenu==null?java.util.OptionalInt.empty():java.util.OptionalInt.of(33);}
        private net.minecraft.server.PlayerAdvancements realAdvancements;
        @Override public net.minecraft.server.PlayerAdvancements getAdvancements() {
            if(realAdvancements==null)realAdvancements=new net.minecraft.server.PlayerAdvancements(net.minecraft.util.datafix.DataFixers.getDataFixer(),getServer().getPlayerList(),getServer().getAdvancements(),java.nio.file.Path.of("build/test-advancements",getUUID()+".json"),this);
            return realAdvancements;
        }
        TestPlayer(ServerLevel level) {
            super(level,new GameProfile(UUID.randomUUID(),"KelTest"));
            // Test players have no sixty-tick real login period; test the actual damage path.
            try{var field=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");field.setAccessible(true);field.setInt(this,0);}
            catch(ReflectiveOperationException ex){throw new IllegalStateException("Cannot prepare damage fixture",ex);}
        }
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
        var recipe=new dev.kelmagic.item.ArchiveRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
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
        h.assertTrue(SpellParser.parse(java.util.List.of("tuna","lu"))==SpellParser.Effect.PAIR,"complete actor/area pair grammar");h.succeed();
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
            var secret=p.getServer().getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic","overload_101"));h.assertTrue(secret.value().display().orElseThrow().isHidden()&&p.getAdvancements().getOrStartProgress(secret).isDone(),"real overload awards hidden challenge before death");
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
        h.assertTrue(MagicServer.maximum(p)==15,"four robes give +10 mana");
        p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(23,true));
        h.assertTrue(MagicServer.maximum(p)==25,"dragon heart stacks with four robes");
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


    @GameTest(template="empty",timeoutTicks=80)
    public static void all180ManuscriptsAndPersistence(GameTestHelper h){
        var book=dev.kelmagic.MagicArchive.CODEX.toStack();
        h.assertTrue(dev.kelmagic.MagicArchive.PAGES.size()==180,"one registered manuscript per word");
        var recipe=new dev.kelmagic.item.ArchiveRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
        for(var w:KelLexicon.WORDS.keySet()){
            h.assertTrue(dev.kelmagic.MagicArchive.PAGES.get(w).get().word.equals(w),"correct manuscript word");
            var input=net.minecraft.world.item.crafting.CraftingInput.of(2,1,java.util.List.of(book,dev.kelmagic.MagicArchive.PAGES.get(w).toStack()));
            h.assertTrue(recipe.matches(input,h.getLevel()),"every manuscript can be added");book=recipe.assemble(input,h.getLevel().registryAccess());
        }
        h.assertTrue(dev.kelmagic.item.CodexItem.knownWords(book).size()==180,"all 180 learned");
        var saved=book.save(h.getLevel().registryAccess());var restored=net.minecraft.world.item.ItemStack.parse(h.getLevel().registryAccess(),saved).orElseThrow();
        h.assertTrue(dev.kelmagic.item.CodexItem.knownWords(restored).equals(java.util.List.copyOf(KelLexicon.WORDS.keySet())),"180-word components persist");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void codex180NetworkAndLateNotes(GameTestHelper h){
        var p=player(h);var book=dev.kelmagic.MagicArchive.CREATIVE_CODEX.toStack();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,book);
        var keys=new java.util.ArrayList<>(KelLexicon.WORDS.keySet());String last=keys.get(179);
        h.assertTrue(dev.kelmagic.item.CodexItem.knownWords(book).size()==180,"creative codex filled by default");
        dev.kelmagic.network.CodexNote.save(p,new dev.kelmagic.network.CodexNote(false,last,"last word interpretation"));
        h.assertTrue(book.get(dev.kelmagic.MagicComponents.CODEX_NOTES.get()).get(179).equals("last word interpretation"),"late note saved without six-word ceiling");
        var packet=new dev.kelmagic.network.CodexOpen(keys,book.get(dev.kelmagic.MagicComponents.CODEX_NOTES.get()),false);
        var buf=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());
        try{dev.kelmagic.network.CodexOpen.CODEC.encode(buf,packet);var decoded=dev.kelmagic.network.CodexOpen.CODEC.decode(buf);h.assertTrue(decoded.equals(packet),"180 words and notes survive network codec");}finally{buf.release();}h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void copyCodexKeepsOriginalBothInks(GameTestHelper h){
        var original=dev.kelmagic.MagicArchive.CODEX.toStack();original.set(dev.kelmagic.MagicComponents.CODEX_WORDS.get(),java.util.List.of("mina","nis","liv"));
        original.set(dev.kelmagic.MagicComponents.CODEX_NOTES.get(),java.util.List.of("old first note"));original.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,Component.literal("Personal archive"));
        for(var ink:java.util.List.of(net.minecraft.world.item.Items.INK_SAC,net.minecraft.world.item.Items.GLOW_INK_SAC)){
            var input=net.minecraft.world.item.crafting.CraftingInput.of(2,2,java.util.List.of(new net.minecraft.world.item.ItemStack(ink),original,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FEATHER),new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOOK)));
            var recipe=new dev.kelmagic.item.CodexCopyRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
            h.assertTrue(recipe instanceof dev.kelmagic.item.CodexCopyRecipe,"actual shapeless copy recipe used");
            var copy=recipe.assemble(input,h.getLevel().registryAccess());h.assertTrue(net.minecraft.world.item.ItemStack.isSameItemSameComponents(copy,original)&&copy.getCount()==1,"copy preserves knowledge, name and notes");
            var rest=recipe.getRemainingItems(input);h.assertTrue(net.minecraft.world.item.ItemStack.isSameItemSameComponents(rest.get(1),original),"original returned to its slot");
            h.assertTrue(rest.get(0).isEmpty()&&rest.get(2).isEmpty()&&rest.get(3).isEmpty(),"only original remains; materials consumed");
            copy.set(dev.kelmagic.MagicComponents.CODEX_WORDS.get(),java.util.List.of("pir"));h.assertTrue(dev.kelmagic.item.CodexItem.knownWords(original).contains("nis"),"copy edits do not change original");
        }h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void copyCodexRejectsEmptyAndExtras(GameTestHelper h){
        var recipe=new dev.kelmagic.item.CodexCopyRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);var book=dev.kelmagic.MagicArchive.CODEX.toStack();
        var ingredients=new java.util.ArrayList<>(java.util.List.of(book,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOOK),new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FEATHER),new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.INK_SAC)));
        h.assertFalse(recipe.matches(net.minecraft.world.item.crafting.CraftingInput.of(2,2,ingredients),h.getLevel()),"empty codex not copied");
        book.set(dev.kelmagic.MagicComponents.CODEX_WORDS.get(),java.util.List.of("nis"));h.assertTrue(recipe.matches(net.minecraft.world.item.crafting.CraftingInput.of(2,2,ingredients),h.getLevel()),"even one learned word permits copy");
        ingredients.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND));ingredients.add(net.minecraft.world.item.ItemStack.EMPTY);
        h.assertFalse(recipe.matches(net.minecraft.world.item.crafting.CraftingInput.of(3,2,ingredients),h.getLevel()),"extra ingredient rejected");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void flameLifeHealsBothFiresNotUnlit(GameTestHelper h){
        var p=player(h);p.setHealth(10);h.assertTrue(SpellEffects.execute(p,SpellParser.plan(java.util.List.of("pir","liv","mina"))),"special permutation executes");
        var effect=dev.kelmagic.MagicContent.FLAME_LIFE.get();effect.applyEffectTick(p,0);h.assertTrue(p.getHealth()==10,"does not heal when unlit");
        p.igniteForTicks(100);effect.applyEffectTick(p,0);h.assertTrue(p.getHealth()==12,"ordinary flame heals");p.clearFire();
        ArcaneFire.ignite(p);effect.applyEffectTick(p,0);h.assertTrue(p.getHealth()==14,"magic flame heals");
        h.assertTrue(p.getData(MagicAttachments.ARCANE_BURN)>0,"counter does not extinguish magic flame");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void flameLifeCancelsFireNotLavaOrMana(GameTestHelper h){
        var p=player(h);FlameLife.grant(p,1);p.setHealth(20);
        p.hurt(p.damageSources().inFire(),4);h.assertTrue(p.getHealth()==20,"in-fire damage canceled");p.invulnerableTime=0;
        p.hurt(p.damageSources().onFire(),4);h.assertTrue(p.getHealth()==20,"on-fire damage canceled");p.invulnerableTime=0;
        ArcaneFire.ignite(p);p.tickCount=0;ArcaneFire.advance(p);h.assertTrue(p.getHealth()==20,"arcane damage canceled");p.invulnerableTime=0;
        p.hurt(p.damageSources().lava(),4);h.assertTrue(p.getHealth()==16,"lava retains its damage");p.invulnerableTime=0;
        MagicServer.hurtMana(p,2);h.assertTrue(p.getHealth()==14,"mana debt still hurts");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void flameLifeExpiresNormally(GameTestHelper h){
        var cow=h.spawnWithNoFreeWill(EntityType.COW,3.5f,1,3.5f);cow.addEffect(new net.minecraft.world.effect.MobEffectInstance(dev.kelmagic.MagicContent.FLAME_LIFE,1,0));
        h.runAfterDelay(3,()->{h.assertFalse(cow.hasEffect(dev.kelmagic.MagicContent.FLAME_LIFE),"effect expires rather than granting permanent immunity");h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void mukNisScaleMagicEntityFire(GameTestHelper h){
        var p=player(h);p.setHealth(20);SpellEffects.execute(p,SpellParser.plan(java.util.List.of("mina","pir","nis")));
        h.assertTrue(p.getData(MagicAttachments.ARCANE_BURN)==SpellProfiles.duration(ConfigFire(),2.0/3),"nis shortens configured entity burn");
        h.assertTrue(Math.abs(p.getData(MagicAttachments.ARCANE_POWER)-2f/3)<.0001,"nis weakens arcane damage");
        var mob=h.spawnWithNoFreeWill(EntityType.COW,3.5f,1,3.5f);ArcaneFire.ignite(mob,SpellProfiles.duration(1800,2.25),2.25f);
        h.assertTrue(mob.getData(MagicAttachments.ARCANE_BURN)==3600,"repeated muk respects duration cap");
        h.assertTrue(mob.getData(MagicAttachments.ARCANE_POWER)==2.25f,"repeated muk increases power");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=190)
    public static void scaledBlockFireLifetimes(GameTestHelper h){
        player(h);var fire=dev.kelmagic.MagicContent.MAGIC_FIRE.get();var smaller=new BlockPos(2,1,3);var larger=new BlockPos(5,1,3);
        h.setBlock(smaller,fire.defaultBlockState().setValue(dev.kelmagic.block.MagicFireBlock.SCALE,5));h.setBlock(larger,fire.defaultBlockState().setValue(dev.kelmagic.block.MagicFireBlock.SCALE,7));
        h.runAfterDelay(66,()->h.assertTrue(h.getBlockState(smaller).is(fire),"nis fire lives until tick 67"));
        h.runAfterDelay(68,()->h.assertTrue(h.getBlockState(smaller).isAir(),"nis block expires at scaled time"));
        h.runAfterDelay(149,()->h.assertTrue(h.getBlockState(larger).is(fire),"muk block lives until tick 150"));
        h.runAfterDelay(151,()->{h.assertTrue(h.getBlockState(larger).isAir(),"muk block expires at scaled time");h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void everyWordProfileHasUniqueAction(GameTestHelper h){
        var signatures=new java.util.HashSet<String>();for(var e:SpellProfiles.ALL.entrySet()){
            h.assertTrue(signatures.add(e.getValue().action()+":"+e.getValue().status()),"distinct nonnumeric action signature "+e.getKey());
            h.assertTrue(SpellParser.plan(java.util.List.of(e.getKey())).effect()!=SpellParser.Effect.UNDEFINED,"registered profile "+e.getKey());
        }h.assertTrue(signatures.size()==172,"172 genuinely differentiated profiles plus eight selectors/operators");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void wordDoorsAndProtection(GameTestHelper h){
        var p=player(h);var pos=h.absolutePos(new BlockPos(3,1,5));h.setBlock(new BlockPos(3,1,5),Blocks.OAK_TRAPDOOR.defaultBlockState());
        h.assertTrue(ExpandedSpellEffects.door(p,pos,true),"opening word handler changes trapdoor");
        h.assertTrue(h.getLevel().getBlockState(pos).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN),"door opened");
        h.assertTrue(ExpandedSpellEffects.door(p,pos,false),"closing word handler changes trapdoor");
        java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent> deny=e->{if(e.getEntity()==p)e.setCanceled(true);};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent.class,deny);
        try{h.assertFalse(ExpandedSpellEffects.door(p,pos,true),"protection can deny opening");h.assertFalse(h.getLevel().getBlockState(pos).getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN),"denied door unchanged");}finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(deny);}h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void compositeBuffAndHealAreReal(GameTestHelper h){
        var p=player(h);p.setHealth(10);h.assertTrue(SpellEffects.execute(p,SpellParser.plan(java.util.List.of("rap","mina","kor"))),"multi-profile spell executes");
        h.assertTrue(p.getHealth()==14&&p.hasEffect(MobEffects.MOVEMENT_SPEED),"healing and speed both applied");
        p.removeAllEffects();SpellEffects.execute(p,SpellParser.plan(java.util.List.of("mina","rap","no")));h.assertTrue(p.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"no reverses speed");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void otherSpellsCannotClearMagicBurn(GameTestHelper h){
        var p=player(h);ArcaneFire.ignite(p);p.igniteForTicks(100);SpellEffects.execute(p,SpellParser.plan(java.util.List.of("mina","pin")));
        h.assertTrue(p.getData(MagicAttachments.ARCANE_BURN)==ConfigFire(),"extinguishing word never clears arcane burn");
        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,100,4));p.tickCount=0;float hp=p.getHealth();ArcaneFire.advance(p);
        h.assertTrue(p.getHealth()<hp,"even resistance V does not counter arcane fire");h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=60)
    public static void newWordsChatChargesOperators(GameTestHelper h){
        var p=player(h);p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(15,true));
        for(var w:java.util.List.of("mina","rap","muk","muk","nis"))word(p,w);
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==0 && p.getHealth()==18,"all five words cost their letters, including repeats");
        var session=p.getData(MagicAttachments.SESSION);MagicServer.action(p,new SpellAction(session.generation(),session.revision(),false));
        h.assertTrue(!session.active() && p.hasEffect(MobEffects.MOVEMENT_SPEED),"chat finish uses expanded plan");
        h.assertTrue(p.getEffect(MobEffects.MOVEMENT_SPEED).getDuration()==360,"net muk scales duration through chat");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void scripterPlaysFlameLifeWithNis(GameTestHelper h){
        var p=player(h);p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(15,true));var stack=dev.kelmagic.MagicContent.SCRIPTER.toStack();
        var words=java.util.List.of("nis","pir","liv","mina");stack.set(dev.kelmagic.MagicComponents.SPELL.get(),words);
        h.assertTrue(dev.kelmagic.item.ScripterItem.valid(words),"new scaled special spell record valid");
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,stack);p.setShiftKeyDown(true);dev.kelmagic.MagicContent.SCRIPTER.get().use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(p.hasEffect(dev.kelmagic.MagicContent.FLAME_LIFE),"scripter executes new healer");
        h.assertTrue(p.getEffect(dev.kelmagic.MagicContent.FLAME_LIFE).getDuration()==800,"nis scales healer through scripter");
        h.assertTrue(p.getData(MagicAttachments.MANA).current()==8,"half of 13 letters rounded up costs 7");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void wordDoorProfilesUseActualAim(GameTestHelper h){
        var p=player(h);var pos=new BlockPos(3,1,4);h.setBlock(pos,Blocks.OAK_TRAPDOOR.defaultBlockState());
        for(String opener:java.util.List.of("rin")){
            p.setXRot(29);h.assertTrue(SpellEffects.execute(p,SpellParser.plan(java.util.List.of(opener))),"opening profile follows real block ray "+opener);
            p.setXRot(20);h.assertTrue(SpellEffects.execute(p,SpellParser.plan(java.util.List.of(opener.equals("rin")?"tin":"tul"))),"closing profile follows real block ray");
        }h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void needleHurtsCasterAndScripterMessage(GameTestHelper h){
        var p=player(h);p.setHealth(20);h.assertTrue(SpellEffects.execute(p,SpellParser.plan(java.util.List.of("mina","but"))),"needle may harm its own caster");h.assertTrue(p.getHealth()==18,"self hurt actually subtracts HP");
        String normal=MagicDamage.source(p,"mana_debt").getLocalizedDeathMessage(p).getString();
        Playback.run(p,"scripter",()->{String item=MagicDamage.source(p,"mana_debt").getLocalizedDeathMessage(p).getString();h.assertTrue(!item.equals(normal)&&!item.startsWith("death.attack."),"scripter has localized independent debt death text");});
        h.assertTrue(Playback.source(p).isEmpty(),"context never leaks to later chat casts");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void magicFireSpreadsBothCombatDirections(GameTestHelper h){
        var p=player(h);var cow=h.spawnWithNoFreeWill(EntityType.COW,3.5f,1,3.5f);ArcaneFire.ignite(cow,400,2f);
        cow.hurt(cow.damageSources().playerAttack(p),1);h.assertTrue(p.getData(MagicAttachments.ARCANE_BURN)==400,"attacking a burning victim ignites attacker");
        p.setData(MagicAttachments.ARCANE_BURN,500);var second=h.spawnWithNoFreeWill(EntityType.COW,4.5f,1,3.5f);second.hurt(second.damageSources().playerAttack(p),1);h.assertTrue(second.getData(MagicAttachments.ARCANE_BURN)==500,"burning attacker ignites victim");
        var dry=h.spawnWithNoFreeWill(EntityType.COW,5.5f,1,3.5f);dry.hurt(dry.damageSources().inFire(),1);h.assertTrue(dry.getData(MagicAttachments.ARCANE_BURN)==0,"ambient fire damage never starts combat contagion");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void pullIsStrongerAndSelfNeedleFromScripter(GameTestHelper h){
        var p=player(h);p.setXRot(20);var cow=h.spawnWithNoFreeWill(EntityType.COW,3.5f,1,3.5f);h.assertTrue(SpellEffects.execute(p,SpellParser.plan(java.util.List.of("sila","vol"))),"pull finds aimed mob");h.assertTrue(cow.getDeltaMovement().length()>2,"pull impulse exceeds old weak impulse");
        var s=dev.kelmagic.MagicContent.SCRIPTER.toStack();s.set(dev.kelmagic.MagicComponents.SPELL.get(),java.util.List.of("mina","but"));p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,s);p.setShiftKeyDown(true);p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(15,true));p.invulnerableTime=0;
        dev.kelmagic.MagicContent.SCRIPTER.get().use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);h.assertTrue(p.getHealth()==18,"scripter invokes real self-harm");h.assertTrue(p.getData(MagicAttachments.MANA).current()==11,"half-cost rounded up");h.succeed();
    }
    private static dev.kelmagic.server.SpellEffects.Aim blockAim(GameTestHelper h,BlockPos pos){var at=h.absolutePos(pos);return new SpellEffects.Aim(Vec3.atCenterOf(at),net.minecraft.world.phys.HitResult.Type.BLOCK,new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(at),net.minecraft.core.Direction.UP,at,false),null);}
    @GameTest(template="empty",timeoutTicks=60)
    public static void worldPlacementAndProtection(GameTestHelper h){
        var p=player(h);var hit=new BlockPos(5,0,5);var aim=blockAim(h,hit);var plan=SpellParser.plan(java.util.List.of("nok"));
        h.assertTrue(WorldSpells.execute(p,aim,plan,SpellProfiles.ALL.get("nok")),"stone placed");h.assertTrue(h.getBlockState(hit.above()).is(Blocks.STONE),"actual block material");
        h.assertFalse(WorldSpells.execute(p,aim,plan,SpellProfiles.ALL.get("nok")),"does not overwrite solid block");
        var other=blockAim(h,new BlockPos(5,0,4));java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent> deny=e->{if(e.getEntity()==p)e.setCanceled(true);};net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent.class,deny);
        try{h.assertFalse(WorldSpells.execute(p,other,plan,SpellProfiles.ALL.get("nok")),"claims can deny conjuring");h.assertTrue(h.getBlockState(new BlockPos(5,1,4)).isAir(),"denied place rolled back");}finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(deny);}h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void worldMiningDropsAndUnbreakable(GameTestHelper h){
        var p=player(h);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE));var pos=new BlockPos(5,1,5);h.setBlock(pos,Blocks.STONE);
        h.assertTrue(WorldSpells.execute(p,blockAim(h,pos),SpellParser.plan(java.util.List.of("lab")),SpellProfiles.ALL.get("lab")),"mine with actual tool");h.assertTrue(h.getBlockState(pos).isAir(),"mined block removed");h.assertTrue(!h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(h.absolutePos(pos)).inflate(2)).isEmpty(),"ordinary drop exists");
        h.setBlock(pos,Blocks.BEDROCK);h.assertFalse(WorldSpells.execute(p,blockAim(h,pos),SpellParser.plan(java.util.List.of("lab")),SpellProfiles.ALL.get("lab")),"bedrock remains");h.setBlock(pos,Blocks.CHEST);h.assertFalse(WorldSpells.execute(p,blockAim(h,pos),SpellParser.plan(java.util.List.of("lab")),SpellProfiles.ALL.get("lab")),"block-entity inventory protected");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void growFreezeAndConvertAreDifferent(GameTestHelper h){
        var p=player(h);var pos=new BlockPos(5,1,5);h.setBlock(pos.below(),Blocks.FARMLAND);h.setBlock(pos,Blocks.WHEAT);var aim=blockAim(h,pos);
        h.assertTrue(WorldSpells.execute(p,aim,SpellParser.plan(java.util.List.of("mel")),SpellProfiles.ALL.get("mel")),"bone meal applied");h.assertTrue(h.getBlockState(pos).getValue(net.minecraft.world.level.block.CropBlock.AGE)>0,"wheat grew");
        h.setBlock(pos,Blocks.WATER);h.assertTrue(WorldSpells.execute(p,aim,SpellParser.plan(java.util.List.of("kol")),SpellProfiles.ALL.get("kol")),"freeze water");h.assertTrue(h.getBlockState(pos).is(Blocks.ICE),"actual ice");
        h.setBlock(pos,Blocks.COBBLESTONE);h.assertTrue(WorldSpells.execute(p,aim,SpellParser.plan(java.util.List.of("sel")),SpellProfiles.ALL.get("sel")),"moss transformation");h.assertTrue(h.getBlockState(pos).is(Blocks.MOSSY_COBBLESTONE),"moss material");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void synergyOrderAndActualBridge(GameTestHelper h){
        var p=player(h);var a=SpellParser.plan(java.util.List.of("rak","tok","muk"));var b=SpellParser.plan(java.util.List.of("muk","tok","rak"));h.assertTrue(a.equals(b)&&a.effect()==SpellParser.Effect.SYNERGY,"synergy is order independent and scales");
        h.assertTrue(WorldSpells.synergy(p,a,blockAim(h,new BlockPos(5,0,1))),"special bridge builds");h.assertTrue(h.getBlockState(new BlockPos(3,1,2)).is(Blocks.GLASS)&&h.getBlockState(new BlockPos(3,1,3)).is(Blocks.GLASS),"multiple bridge blocks");
        var guard=SpellParser.plan(java.util.List.of("kor","ten"));p.setHealth(10);h.assertTrue(SpellEffects.execute(p,guard)&&p.getHealth()==16&&p.hasEffect(MobEffects.DAMAGE_RESISTANCE),"guardian combination has dedicated behavior");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void cassetteRecipeIsExact(GameTestHelper h){
        var I=net.minecraft.world.item.Items.IRON_NUGGET;var P=net.minecraft.world.item.Items.PAPER;var S=net.minecraft.world.item.Items.SLIME_BALL;var R=net.minecraft.world.item.Items.REDSTONE;var T=net.minecraft.world.item.Items.STRING;
        var in=net.minecraft.world.item.crafting.CraftingInput.of(3,3,java.util.List.of(new net.minecraft.world.item.ItemStack(I),new net.minecraft.world.item.ItemStack(P),new net.minecraft.world.item.ItemStack(I),new net.minecraft.world.item.ItemStack(S),new net.minecraft.world.item.ItemStack(R),new net.minecraft.world.item.ItemStack(S),new net.minecraft.world.item.ItemStack(S),new net.minecraft.world.item.ItemStack(T),new net.minecraft.world.item.ItemStack(S)));
        var recipe=h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,in,h.getLevel()).orElseThrow().value();h.assertTrue(recipe.assemble(in,h.getLevel().registryAccess()).is(dev.kelmagic.MagicAudio.CASSETTE),"requested shaped recipe yields cassette");h.succeed();
    }
    private static dev.kelmagic.audio.DeckEntity deck(GameTestHelper h){var pos=new BlockPos(5,1,5);h.setBlock(pos,dev.kelmagic.MagicAudio.DECK.get());return (dev.kelmagic.audio.DeckEntity)h.getLevel().getBlockEntity(h.absolutePos(pos));}
    @GameTest(template="empty",timeoutTicks=60)
    public static void deckWritesAtFiveSecondsPerWord(GameTestHelper h){
        player(h);var d=deck(h);var w=java.util.List.of("mina","rap","muk","muk");var writer=dev.kelmagic.MagicContent.SCRIPTER.toStack();writer.set(dev.kelmagic.MagicComponents.SPELL.get(),w);d.inventory.setItem(0,dev.kelmagic.MagicAudio.CASSETTE.toStack());d.inventory.setItem(1,writer);
        for(int i=0;i<399;i++)d.advance();h.assertTrue(d.inventory.getItem(2).isEmpty()&&d.progress==399,"no early output; duplicates count toward 20 seconds");d.advance();h.assertTrue(dev.kelmagic.audio.CassetteItem.words(d.inventory.getItem(2)).equals(w),"all words copied to tape");h.assertTrue(d.inventory.getItem(0).isEmpty()&&d.inventory.getItem(1).isEmpty(),"cassette and writer consumed on successful completion");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void deckResetAndBlockedOutput(GameTestHelper h){
        player(h);var d=deck(h);var writer=dev.kelmagic.MagicContent.SCRIPTER.toStack();writer.set(dev.kelmagic.MagicComponents.SPELL.get(),java.util.List.of("rap"));d.inventory.setItem(0,dev.kelmagic.MagicAudio.CASSETTE.toStack());d.inventory.setItem(1,writer);for(int i=0;i<50;i++)d.advance();d.inventory.setItem(1,net.minecraft.world.item.ItemStack.EMPTY);d.advance();h.assertTrue(d.progress==0,"removing input cancels progress");d.inventory.setItem(1,writer);d.inventory.setItem(2,dev.kelmagic.MagicAudio.CASSETTE.toStack());for(int i=0;i<200;i++)d.advance();h.assertTrue(d.progress==0&&!d.inventory.getItem(0).isEmpty(),"occupied output never eats input");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void deckPersistsProgressAndOriginal(GameTestHelper h){
        player(h);var d=deck(h);var writer=dev.kelmagic.MagicContent.SCRIPTER.toStack();writer.set(dev.kelmagic.MagicComponents.SPELL.get(),java.util.List.of("mina","rap"));d.inventory.setItem(0,dev.kelmagic.MagicAudio.CASSETTE.toStack());d.inventory.setItem(1,writer);for(int i=0;i<77;i++)d.advance();var tag=d.saveWithoutMetadata(h.getLevel().registryAccess());var reload=new dev.kelmagic.audio.DeckEntity(d.getBlockPos(),d.getBlockState());reload.loadWithComponents(tag,h.getLevel().registryAccess());h.assertTrue(reload.progress==77&&reload.total==200,"remaining recording persists");for(int i=0;i<123;i++)reload.advance();h.assertTrue(dev.kelmagic.audio.CassetteItem.recorded(reload.inventory.getItem(2)),"resumes rather than restarting");h.assertTrue(reload.inventory.getItem(1).isEmpty(),"writer consumed after resumed success");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void playerRejectsBlankAndLocksHolder(GameTestHelper h){
        var p=player(h);var device=dev.kelmagic.MagicAudio.PLAYER.toStack();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,device);var menu=new dev.kelmagic.audio.TapeMenu(1,p.getInventory(),net.minecraft.world.InteractionHand.MAIN_HAND);var blank=dev.kelmagic.MagicAudio.CASSETTE.toStack();h.assertFalse(menu.getSlot(0).mayPlace(blank),"blank cannot enter player");var tape=blank.copy();tape.set(dev.kelmagic.MagicComponents.SPELL.get(),java.util.List.of("rap"));h.assertTrue(menu.getSlot(0).mayPlace(tape),"valid tape can enter");menu.getSlot(0).set(tape);h.assertTrue(dev.kelmagic.audio.CassetteItem.words(dev.kelmagic.audio.TapePlayerItem.tapes(device).get(0)).equals(java.util.List.of("rap")),"menu immediately persists tape");h.assertFalse(menu.getSlot(35+p.getInventory().selected).mayPickup(p),"held player locked against nesting/moving");h.assertTrue(menu.stillValid(p),"holder tracked");p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);h.assertFalse(menu.stillValid(p),"changing holder closes menu");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void playerPlaybackThirtyPercentNoCooldown(GameTestHelper h){
        var p=player(h);var device=dev.kelmagic.MagicAudio.PLAYER.toStack();var tape=dev.kelmagic.MagicAudio.CASSETTE.toStack();var w=java.util.List.of("mina","rap");tape.set(dev.kelmagic.MagicComponents.SPELL.get(),w);device.set(net.minecraft.core.component.DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.fromItems(java.util.List.of(tape)));p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,device);p.setData(MagicAttachments.MANA,new MagicAttachments.Mana(15,true));
        dev.kelmagic.MagicAudio.PLAYER.get().use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);h.assertTrue(p.hasEffect(MobEffects.MOVEMENT_SPEED)&&p.getData(MagicAttachments.MANA).current()==12,"ceil 30 percent of seven costs three");h.assertFalse(p.getCooldowns().isOnCooldown(dev.kelmagic.MagicAudio.PLAYER.get()),"no player cooldown");dev.kelmagic.MagicAudio.PLAYER.get().use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);h.assertTrue(p.getData(MagicAttachments.MANA).current()==9,"immediate second playback succeeds");h.assertTrue(!device.isDamageableItem()&&dev.kelmagic.audio.CassetteItem.recorded(tape),"no durability or consumption");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void tapeSelectionAndComponentsPersist(GameTestHelper h){
        var p=player(h);var device=dev.kelmagic.MagicAudio.PLAYER.toStack();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,device);dev.kelmagic.audio.TapePlayerItem.select(p,-1);h.assertTrue(dev.kelmagic.audio.TapePlayerItem.active(device)==7,"selection wraps");dev.kelmagic.audio.TapePlayerItem.select(p,99);h.assertTrue(dev.kelmagic.audio.TapePlayerItem.active(device)==7,"malformed delta ignored");dev.kelmagic.audio.TapePlayerItem.select(p,1);h.assertTrue(dev.kelmagic.audio.TapePlayerItem.active(device)==0,"back to first");var tape=dev.kelmagic.MagicAudio.CASSETTE.toStack();tape.set(dev.kelmagic.MagicComponents.SPELL.get(),java.util.List.of("mina","but"));device.set(net.minecraft.core.component.DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.fromItems(java.util.List.of(tape)));var reload=net.minecraft.world.item.ItemStack.parseOptional(h.getLevel().registryAccess(),(net.minecraft.nbt.CompoundTag)device.save(h.getLevel().registryAccess()));h.assertTrue(dev.kelmagic.audio.CassetteItem.words(dev.kelmagic.audio.TapePlayerItem.tapes(reload).get(0)).equals(java.util.List.of("mina","but")),"persistent nested tape spell");h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=60)
    public static void worldProfilesDispatchWithRealFixtures(GameTestHelper h){
        var p=player(h);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE));int checked=0;
        for(var entry:SpellProfiles.ALL.entrySet()){
            var profile=entry.getValue();if(!profile.action().startsWith("W_")&&!profile.action().equals("TURN"))continue;
            for(int x=3;x<=7;x++)for(int z=3;z<=7;z++){h.setBlock(x,0,z,Blocks.STONE);for(int y=1;y<=5;y++)h.setBlock(x,y,z,Blocks.AIR);}
            var pos=new BlockPos(5,0,5);var action=profile.action();
            if(action.equals("W_PLANT")){h.setBlock(pos,profile.status().equals("wheat")?Blocks.FARMLAND:profile.status().equals("brown_mushroom")?Blocks.MYCELIUM:Blocks.GRASS_BLOCK);}
            if(java.util.Set.of("W_GROW","W_GROW_AREA","W_TICK","W_HARVEST").contains(action)){h.setBlock(pos,Blocks.FARMLAND);pos=pos.above();h.setBlock(pos,Blocks.WHEAT.defaultBlockState().setValue(net.minecraft.world.level.block.CropBlock.AGE,action.equals("W_HARVEST")?7:0));}
            if(java.util.Set.of("W_FREEZE","W_STEAM","W_WAVE").contains(action)){pos=pos.above();h.setBlock(pos,Blocks.WATER);}
            if(action.equals("W_DRY")){pos=pos.above();h.setBlock(pos,Blocks.WET_SPONGE);}
            if(action.equals("W_PUDDLE")){pos=pos.above();h.setBlock(pos,Blocks.OAK_SLAB);}
            if(action.equals("W_WARM")){pos=pos.above();h.setBlock(pos,Blocks.ICE);}
            if(action.equals("W_CONVERT")){pos=pos.above();h.setBlock(pos,net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.withDefaultNamespace(profile.status().split(">")[0])));}
            if(action.equals("W_MOISTEN"))h.setBlock(pos,Blocks.FARMLAND);
            if(action.equals("W_PATH")||action.equals("W_TILL"))h.setBlock(pos,Blocks.DIRT);
            if(action.equals("W_STRIP")){pos=pos.above();h.setBlock(pos,Blocks.OAK_LOG);}
            if(action.equals("W_WASH")){pos=pos.above();h.setBlock(pos,Blocks.RED_WOOL);}
            if(action.equals("W_VALVE")){pos=pos.above();h.setBlock(pos,Blocks.LEVER.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACH_FACE,net.minecraft.world.level.block.state.properties.AttachFace.FLOOR));}
            if(action.equals("TURN")){pos=pos.above();h.setBlock(pos,Blocks.OAK_STAIRS);}
            if(action.equals("W_CLEAR")){h.setBlock(pos,Blocks.GRASS_BLOCK);pos=pos.above();h.setBlock(pos,Blocks.SHORT_GRASS);}
            if(action.equals("W_MINE")){pos=pos.above();h.setBlock(pos,profile.status().equals("wood")?Blocks.OAK_LOG:profile.status().equals("soil")?Blocks.DIRT:profile.status().equals("soft")?Blocks.SAND:Blocks.STONE);}
            if(action.equals("W_VACUUM")){var at=h.absoluteVec(Vec3.atCenterOf(pos.above()));var item=new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),at.x,at.y,at.z,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE));item.setNoPickUpDelay();h.getLevel().addFreshEntity(item);}
            h.assertTrue(WorldSpells.execute(p,blockAim(h,pos),SpellParser.plan(java.util.List.of(entry.getKey())),profile),"real world effect "+entry.getKey()+" / "+action);checked++;
        }h.assertTrue(checked>=60,"broad distinct world coverage");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void newEquipmentRecipesRegistered(GameTestHelper h){
        for(String name:java.util.List.of("cassette_deck","cassette_player")){
            String[] pat=name.equals("cassette_deck")?new String[]{"IPI","RCR","IQI"}:new String[]{"IQI","BCB","MRS"};
            var map=new java.util.HashMap<Character,net.minecraft.world.item.Item>();map.put('I',net.minecraft.world.item.Items.IRON_INGOT);map.put('P',net.minecraft.world.item.Items.PAPER);map.put('R',net.minecraft.world.item.Items.REDSTONE_BLOCK);map.put('Q',net.minecraft.world.item.Items.QUARTZ);map.put('B',net.minecraft.world.item.Items.STONE_BUTTON);map.put('M',net.minecraft.world.item.Items.COPPER_INGOT);map.put('S',net.minecraft.world.item.Items.SLIME_BALL);map.put('C',name.equals("cassette_deck")?dev.kelmagic.MagicAudio.CASSETTE.get():dev.kelmagic.MagicContent.MANA_CORE.get());
            var stacks=new java.util.ArrayList<net.minecraft.world.item.ItemStack>();for(var row:pat)for(char c:row.toCharArray())stacks.add(new net.minecraft.world.item.ItemStack(map.get(c)));var input=net.minecraft.world.item.crafting.CraftingInput.of(3,3,stacks);var recipe=h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,h.getLevel()).orElseThrow().value();h.assertTrue(recipe.assemble(input,h.getLevel().registryAccess()).is(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic",name))),"registered recipe "+name);
        }h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void miningHonorsProtectionAndAdventure(GameTestHelper h){
        var p=player(h);var pos=new BlockPos(5,1,5);h.setBlock(pos,Blocks.STONE);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE));
        java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.BreakEvent> deny=e->{if(e.getPlayer()==p)e.setCanceled(true);};net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent.class,deny);
        try{h.assertFalse(WorldSpells.execute(p,blockAim(h,pos),SpellParser.plan(java.util.List.of("lab")),SpellProfiles.ALL.get("lab")),"claim denies breaking");h.assertTrue(h.getBlockState(pos).is(Blocks.STONE),"denied mine retains block");}finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(deny);}
        p.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);h.assertFalse(WorldSpells.execute(p,blockAim(h,pos),SpellParser.plan(java.util.List.of("lab")),SpellProfiles.ALL.get("lab")),"adventure cannot bypass building permissions");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void noUnsupportedActionIsSilentlyApplied(GameTestHelper h){
        var p=player(h);p.setHealth(20);h.assertFalse(SpellEffects.execute(p,SpellParser.plan(java.util.List.of("mina","don","no"))),"unsupported throw inverse does not run original damage");h.assertTrue(p.getHealth()==20,"no silently ignored negative operator");h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=60)
    public static void allEntityProfilesExecuteWithSuitableTargets(GameTestHelper h){
        var p=player(h);var mob=h.spawnWithNoFreeWill(EntityType.ZOMBIE,3.5f,1,6.5f);int checked=0;
        for(var entry:SpellProfiles.ALL.entrySet()){
            var a=entry.getValue().action();if(a.startsWith("W_")||java.util.Set.of("OPEN","CLOSE","TURN").contains(a))continue;
            p.setPos(h.absoluteVec(new Vec3(3.5,1,1.5)));p.setXRot(60);p.removeAllEffects();p.setHealth(10);p.invulnerableTime=0;p.setDeltaMovement(Vec3.ZERO);p.getFoodData().setFoodLevel(5);p.getFoodData().setSaturation(0);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.APPLE));p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE));
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON,100));p.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.WEAKNESS,100));p.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,100));p.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.DARKNESS,100));
            mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,net.minecraft.world.item.ItemStack.EMPTY);
            if(a.equals("MEND")){var armor=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_HELMET);armor.setDamageValue(12);p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,armor);}
            if(a.equals("BAKE"))p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WHEAT,3));
            if(a.equals("COOK"))p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BEEF));
            if(a.equals("READ")){var writer=dev.kelmagic.MagicContent.SCRIPTER.toStack();writer.set(dev.kelmagic.MagicComponents.SPELL.get(),java.util.List.of("rap"));p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,writer);}
            if(a.equals("SPELL_INFO")){var codex=dev.kelmagic.MagicArchive.CODEX.toStack();codex.set(dev.kelmagic.MagicComponents.CODEX_WORDS.get(),java.util.List.of("rap"));p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,codex);}
            if(a.equals("COLLECT")||a.equals("SCAN_ITEMS")){var at=p.position().add(.5,0,.5);var item=new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),at.x,at.y,at.z,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE));item.setNoPickUpDelay();h.getLevel().addFreshEntity(item);}
            if(a.equals("EXIT"))p.setPos(h.absoluteVec(new Vec3(3.5,1,5.5)));
            boolean result;
            if(java.util.Set.of("PACIFY","HOSTILE","COMMAND","FEAR","LEAD","GIVE").contains(a))result=EntitySpells.apply(p,a.equals("HOSTILE")?h.spawnWithNoFreeWill(EntityType.COW,4.5f,1,6.5f):mob,a,240,(float)entry.getValue().power(),1);
            else if(a.equals("SCAN_PLAYERS"))result=EntitySpells.apply(p,p,a,240,1,1);
            else result=SpellEffects.execute(p,SpellParser.plan(java.util.List.of("mina",entry.getKey())));
            h.assertTrue(result,"entity or utility effect "+entry.getKey()+" / "+a);checked++;
        }h.assertTrue(checked>=90,"broad differentiated entity and utility coverage");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void shatterHasNoLootAndCannotBreakBedrock(GameTestHelper h){
        var p=player(h);var pos=new BlockPos(5,1,5);h.setBlock(pos,Blocks.STONE);h.assertTrue(WorldSpells.execute(p,blockAim(h,pos),SpellParser.plan(java.util.List.of("lom")),SpellProfiles.ALL.get("lom")),"shatter works on soft stone");h.assertTrue(h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(h.absolutePos(pos)).inflate(1)).isEmpty(),"shattering differs from mining: no loot");h.setBlock(pos,Blocks.BEDROCK);h.assertFalse(WorldSpells.execute(p,blockAim(h,pos),SpellParser.plan(java.util.List.of("lom")),SpellProfiles.ALL.get("lom")),"negative hardness never treated as soft");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void luConstructionAndGardenHaveRealShapes(GameTestHelper h){
        var p=player(h);var aim=blockAim(h,new BlockPos(5,0,5));h.assertTrue(WorldSpells.execute(p,aim,SpellParser.plan(java.util.List.of("lu","nok")),SpellProfiles.ALL.get("nok")),"lu expands creation");h.assertTrue(h.getBlockState(new BlockPos(4,1,4)).is(Blocks.STONE)&&h.getBlockState(new BlockPos(6,1,6)).is(Blocks.STONE),"3 by 3 shape");
        for(int x=4;x<=6;x++)for(int z=4;z<=6;z++){h.setBlock(x,1,z,Blocks.AIR);h.setBlock(x,0,z,Blocks.DIRT);}var plan=SpellParser.plan(java.util.List.of("ter","pel","vak"));h.assertTrue(WorldSpells.synergy(p,plan,aim),"garden creates irrigation");h.assertTrue(h.getBlockState(new BlockPos(5,0,5)).is(Blocks.WATER)&&h.getBlockState(new BlockPos(4,1,4)).is(Blocks.WHEAT),"center water and planted farmland");h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=60)
    public static void goldenThreadClothAndWoolRecipes(GameTestHelper h){
        var S=net.minecraft.world.item.Items.STRING;var G=net.minecraft.world.item.Items.GOLD_INGOT;var ingredients=new java.util.ArrayList<net.minecraft.world.item.ItemStack>();for(int i=0;i<9;i++)ingredients.add(new net.minecraft.world.item.ItemStack(i==4?G:S));var input=net.minecraft.world.item.crafting.CraftingInput.of(3,3,ingredients);var recipe=h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,h.getLevel()).orElseThrow().value();var result=recipe.assemble(input,h.getLevel().registryAccess());h.assertTrue(result.is(dev.kelmagic.MagicMaterials.GOLDEN_THREAD)&&result.getCount()==8,"one gold and eight strings yield eight golden threads");
        var four=net.minecraft.world.item.crafting.CraftingInput.of(2,2,java.util.Collections.nCopies(4,dev.kelmagic.MagicMaterials.GOLDEN_THREAD.toStack()));var wool=h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,four,h.getLevel()).orElseThrow().value().assemble(four,h.getLevel().registryAccess());h.assertTrue(wool.is(dev.kelmagic.MagicMaterials.WOOL_ITEM),"four threads yield wool");
        var cloth=new java.util.ArrayList<net.minecraft.world.item.ItemStack>();for(int i=0;i<9;i++)cloth.add(i==4?dev.kelmagic.MagicContent.MANA_CORE.toStack():i%2==0?new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LEATHER):dev.kelmagic.MagicMaterials.GOLDEN_THREAD.toStack());var fabric=net.minecraft.world.item.crafting.CraftingInput.of(3,3,cloth);h.assertTrue(h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,fabric,h.getLevel()).orElseThrow().value().assemble(fabric,h.getLevel().registryAccess()).is(dev.kelmagic.MagicContent.MAGIC_CLOTH),"cloth uses golden threads");for(int i:java.util.List.of(1,3,5,7))cloth.set(i,new net.minecraft.world.item.ItemStack(S));h.assertTrue(h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,net.minecraft.world.item.crafting.CraftingInput.of(3,3,cloth),h.getLevel()).isEmpty(),"old string recipe no longer works");
        var state=dev.kelmagic.MagicMaterials.GOLDEN_WOOL.get().defaultBlockState();var pos=h.absolutePos(new BlockPos(5,1,5));h.assertTrue(state.getFlammability(h.getLevel(),pos,net.minecraft.core.Direction.UP)==0,"golden wool does not burn");h.assertTrue(state.getDestroySpeed(h.getLevel(),pos)>=4,"strong decorative wool");h.assertTrue(dev.kelmagic.MagicMaterials.WOOL_ITEM.toStack().has(net.minecraft.core.component.DataComponents.FIRE_RESISTANT),"dropped block also fire resistant");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void decorationPreservesSpellAndTemplate(GameTestHelper h){
        var tape=dev.kelmagic.MagicAudio.CASSETTE.toStack();var words=java.util.List.of("mina","rap","muk");tape.set(dev.kelmagic.MagicComponents.SPELL.get(),words);tape.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,Component.literal("Личная лента"));var pattern=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CREEPER_BANNER_PATTERN);var dye=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BLUE_DYE);var slime=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SLIME_BALL);
        var in=net.minecraft.world.item.crafting.CraftingInput.of(2,2,java.util.List.of(tape,slime,pattern,dye));var recipe=h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,in,h.getLevel()).orElseThrow().value();h.assertTrue(recipe instanceof dev.kelmagic.audio.DecorateTapeRecipe,"real customization recipe");var decorated=recipe.assemble(in,h.getLevel().registryAccess());h.assertTrue(dev.kelmagic.audio.CassetteItem.words(decorated).equals(words)&&decorated.getHoverName().getString().equals("Личная лента"),"spell/name preserved");h.assertTrue(decorated.get(net.minecraft.core.component.DataComponents.DYED_COLOR).rgb()==net.minecraft.world.item.DyeColor.BLUE.getTextureDiffuseColor(),"blue dye changes casing");h.assertTrue(dev.kelmagic.audio.CassetteItem.patternIndex(decorated)==2,"creeper sticker");h.assertTrue(recipe.getRemainingItems(in).get(2).is(net.minecraft.world.item.Items.CREEPER_BANNER_PATTERN),"pattern template returned");
        var recolor=net.minecraft.world.item.crafting.CraftingInput.of(3,1,java.util.List.of(decorated,slime,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.RED_DYE)));var changed=recipe.assemble(recolor,h.getLevel().registryAccess());h.assertTrue(dev.kelmagic.audio.CassetteItem.patternIndex(changed)==2&&changed.get(net.minecraft.core.component.DataComponents.DYED_COLOR).rgb()==net.minecraft.world.item.DyeColor.RED.getTextureDiffuseColor(),"dye-only preserves sticker");h.assertFalse(recipe.matches(net.minecraft.world.item.crafting.CraftingInput.of(3,1,java.util.List.of(dev.kelmagic.MagicAudio.CASSETTE.toStack(),slime,dye)),h.getLevel()),"blank tape rejected");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void fractionalManaPersistsAndPaysExactDebt(GameTestHelper h){
        var p=player(h);var robe=dev.kelmagic.MagicContent.ROBE_ITEMS.getFirst();p.setItemSlot(robe.get().getEquipmentSlot(),robe.toStack());h.assertTrue(MagicServer.maximum(p)==7.5,"one robe grants exactly 2.5");var mana=new MagicAttachments.Mana(7.5,false);var tag=MagicAttachments.Mana.CODEC.encodeStart(NbtOps.INSTANCE,mana).getOrThrow();h.assertTrue(MagicAttachments.Mana.CODEC.parse(NbtOps.INSTANCE,tag).getOrThrow().current()==7.5,"half-unit saved");var old=new net.minecraft.nbt.CompoundTag();old.putInt("current",5);old.putBoolean("dragon_upgrade",false);h.assertTrue(MagicAttachments.Mana.CODEC.parse(NbtOps.INSTANCE,old).getOrThrow().current()==5,"legacy integer mana decodes unchanged");var spend=ManaMath.spend(7.5,8);h.assertTrue(spend.remaining()==0&&spend.healthDamage()==1,"half missing mana costs one HP");h.assertTrue(ManaMath.regenerate(7,7.5)==7.5,"regen reaches fractional cap");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void fractionalSnapshotCodec(GameTestHelper h){
        var buf=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());var p=player(h);var packet=new dev.kelmagic.network.SpellSnapshot(p.getUUID(),1L,1,6.5,7.5,java.util.List.of("mina"),0);dev.kelmagic.network.SpellSnapshot.CODEC.encode(buf,packet);var decoded=dev.kelmagic.network.SpellSnapshot.CODEC.decode(buf);h.assertTrue(decoded.mana()==6.5&&decoded.maximum()==7.5,"network preserves halves");buf.release();h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void hostileStaysNeutralUntilHit(GameTestHelper h){
        var p=player(h);var zombie=h.spawnWithNoFreeWill(EntityType.ZOMBIE,3.5f,1,3.5f);h.assertTrue(NeutralMagic.neutralize(zombie,true),"hostile changed to neutral");zombie.setTarget(p);h.assertTrue(zombie.getTarget()==null,"hostile cannot reacquire a player spontaneously");zombie.hurt(zombie.damageSources().playerAttack(p),1);h.assertTrue(zombie.getTarget()==p,"neutral hostile retaliates");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void peacefulBecomesRetaliatingNeutral(GameTestHelper h){
        var p=player(h);var cow=h.spawnWithNoFreeWill(EntityType.COW,3.5f,1,3.5f);h.assertTrue(NeutralMagic.neutralize(cow,false),"passive becomes neutral");cow.setTarget(p);h.assertTrue(cow.getTarget()==null,"not made permanently hostile");cow.hurt(cow.damageSources().playerAttack(p),1);h.assertTrue(cow.getTarget()==p&&cow.getData(MagicAttachments.NEUTRAL_KIND)==2,"cow now retaliates on attacker");h.assertFalse(NeutralMagic.neutralize(cow,true),"mil targets hostile rather than passive mobs");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void sunWorksWithoutAimAndBridgeStartsHere(GameTestHelper h){var p=player(h);p.setXRot(-70);h.assertFalse(SpellEffects.execute(p,SpellParser.plan(java.util.List.of("sol"))),"sol needs aimed surface not placement beneath caster");p.setXRot(0);h.assertTrue(SpellEffects.execute(p,SpellParser.plan(java.util.List.of("tok","rak"))),"bridge no distant block required");h.assertTrue(h.getBlockState(new BlockPos(3,1,2)).is(Blocks.GLASS),"first block right ahead");h.succeed();}
    @GameTest(template="empty",timeoutTicks=60)
    public static void ruinScalesUpAndDown(GameTestHelper h){
        var p=player(h);var aim=blockAim(h,new BlockPos(3,0,5));int normal=WorldSpells.scaled(3,1,15),strong=WorldSpells.scaled(3,2.25,15),weak=WorldSpells.scaled(3,2.0/3,15);h.assertTrue(strong>normal&&weak<normal,"wall width scales both directions");h.assertTrue(WorldSpells.synergy(p,SpellParser.plan(java.util.List.of("sel","nok","muk","muk")),aim),"expanded mossy wall");h.assertTrue(h.getBlockState(new BlockPos(0,1,5)).is(Blocks.MOSSY_STONE_BRICKS)&&h.getBlockState(new BlockPos(6,1,5)).is(Blocks.MOSSY_STONE_BRICKS),"two muk make seven-wide wall");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void dashFollowsLookAndEveryPairHasPlan(GameTestHelper h){
        var p=player(h);p.setYRot(40);p.setXRot(-25);var look=p.getLookAngle().normalize();h.assertTrue(SpellEffects.execute(p,SpellParser.plan(java.util.List.of("rak","rap"))),"new directional dash");h.assertTrue(p.getDeltaMovement().normalize().distanceTo(look)<.0001,"three-dimensional gaze direction");var words=new java.util.ArrayList<>(KelLexicon.WORDS.keySet());int pairs=0;for(int i=0;i<words.size();i++)for(int k=i;k<words.size();k++){var plan=SpellParser.plan(java.util.List.of(words.get(i),words.get(k)));h.assertTrue(plan.effect()!=SpellParser.Effect.UNDEFINED&&plan.effect()!=SpellParser.Effect.SELECTION_ONLY,"pair "+words.get(i)+" "+words.get(k));pairs++;}h.assertTrue(pairs==16290,"all unordered pairs including resonance");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void goldenWoolBarterCostsOneAndHasCooldown(GameTestHelper h){
        var p=player(h);var pig=h.spawnWithNoFreeWill(EntityType.PIGLIN,3.5f,1,3.5f);var stack=new net.minecraft.world.item.ItemStack(dev.kelmagic.MagicMaterials.WOOL_ITEM.get(),2);h.assertTrue(WoolBarter.trade(p,pig,stack),"custom piglin exchange");h.assertTrue(stack.getCount()==1,"one wool paid");h.assertTrue(pig.getOffhandItem().is(dev.kelmagic.MagicMaterials.WOOL_ITEM)&&pig.getBrain().hasMemoryValue(net.minecraft.world.entity.ai.memory.MemoryModuleType.ADMIRING_ITEM),"holds wool and thinks before reward");h.assertTrue(h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,pig.getBoundingBox().inflate(2)).isEmpty(),"no premature reward");h.assertFalse(WoolBarter.trade(p,pig,stack),"second immediate exchange blocked");pig.setBaby(true);pig.setData(MagicAttachments.BARTER_READY,0L);h.assertFalse(WoolBarter.trade(p,pig,stack),"baby cannot trade");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void secretAchievementsAwardedByRealConditions(GameTestHelper h){
        var p=player(h);var id=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic","complete_codex");var adv=p.getServer().getAdvancements().get(id);h.assertTrue(adv!=null&&adv.value().display().orElseThrow().isHidden(),"secret hidden by advancement UI");h.assertFalse(p.getAdvancements().getOrStartProgress(adv).isDone(),"secret not pre-awarded");p.getInventory().setItem(0,dev.kelmagic.MagicArchive.CREATIVE_CODEX.toStack());MagicProgress.inventory(p);h.assertFalse(p.getAdvancements().getOrStartProgress(adv).isDone(),"creative full codex not accepted");var book=dev.kelmagic.MagicArchive.CODEX.toStack();book.set(dev.kelmagic.MagicComponents.CODEX_WORDS.get(),java.util.List.copyOf(KelLexicon.WORDS.keySet()));p.getInventory().setItem(0,book);MagicProgress.inventory(p);h.assertTrue(p.getAdvancements().getOrStartProgress(adv).isDone(),"180 learned words awards secret; inv="+p.getInventory().getItem(0).getItem()+" words="+p.getInventory().getItem(0).getOrDefault(dev.kelmagic.MagicComponents.CODEX_WORDS.get(),java.util.List.<String>of()).size()+" remaining="+p.getAdvancements().getOrStartProgress(adv).getRemainingCriteria());h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=50)
    public static void neutralCowActuallyRetaliatesAndPersists(GameTestHelper h){
        var p=player(h);var cow=h.spawnWithNoFreeWill(EntityType.COW,3.5f,1,3.5f);cow.setNoAi(false);NeutralMagic.neutralize(cow,false);var saved=new net.minecraft.nbt.CompoundTag();cow.saveWithoutId(saved);var fresh=EntityType.COW.create(h.getLevel());fresh.load(saved);h.assertTrue(fresh.getData(MagicAttachments.NEUTRAL_KIND)==2,"neutral marker persisted");
        cow.hurt(cow.damageSources().playerAttack(p),1);h.runAfterDelay(20,()->{h.assertTrue(p.getHealth()<20,"ordinary cow now delivers retaliatory melee damage");h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void individualWordRarities(GameTestHelper h){player(h);h.assertTrue(dev.kelmagic.core.WordRarity.ALL.size()==180,"every word tiered");h.assertTrue(dev.kelmagic.core.WordRarity.of("vak")==net.minecraft.world.item.Rarity.COMMON&&dev.kelmagic.core.WordRarity.of("riv")==net.minecraft.world.item.Rarity.RARE&&dev.kelmagic.core.WordRarity.of("mar")==net.minecraft.world.item.Rarity.EPIC,"requested water tiers");for(var e:dev.kelmagic.MagicArchive.PAGES.entrySet())h.assertTrue(e.getValue().toStack().getRarity()==dev.kelmagic.core.WordRarity.of(e.getKey()),"page matches word tier");h.succeed();}
    @GameTest(template="empty",timeoutTicks=60)
    public static void vidScansBehindCasterAndWalls(GameTestHelper h){var p=player(h);var mob=h.spawnWithNoFreeWill(EntityType.COW,3.5f,1,0.5f);h.setBlock(3,1,1,Blocks.STONE);h.assertTrue(SpellEffects.execute(p,SpellParser.plan(java.util.List.of("vid"))),"area scan without aim");h.assertTrue(mob.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING),"off-axis and occluded mob glows");h.succeed();}
    @GameTest(template="empty",timeoutTicks=60)
    public static void codexPositionStoredAndBoundToBook(GameTestHelper h){var p=player(h);var b=dev.kelmagic.MagicArchive.CODEX.toStack();b.set(dev.kelmagic.MagicComponents.CODEX_TOKEN.get(),"test-book");p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,b);dev.kelmagic.network.CodexPosition.save(p,new dev.kelmagic.network.CodexPosition(false,"test-book",1,19,"mar"));h.assertTrue(b.get(dev.kelmagic.MagicComponents.CODEX_PAGE.get())==19&&b.get(dev.kelmagic.MagicComponents.CODEX_SECTION.get())==1,"stored page and section");dev.kelmagic.network.CodexPosition.save(p,new dev.kelmagic.network.CodexPosition(false,"forged",0,5,""));h.assertTrue(b.get(dev.kelmagic.MagicComponents.CODEX_PAGE.get())==19,"other book token rejected");var tag=b.save(h.getLevel().registryAccess());var copy=net.minecraft.world.item.ItemStack.parse(h.getLevel().registryAccess(),tag).orElseThrow();h.assertTrue(copy.get(dev.kelmagic.MagicComponents.CODEX_QUERY.get()).equals("mar"),"position survives save");h.succeed();}
    @GameTest(template="empty",timeoutTicks=60)
    public static void archiveTableOnlyMergeAndCopy(GameTestHelper h){var p=player(h);h.setBlock(2,1,2,dev.kelmagic.MagicStudy.TABLE.get());var t=(dev.kelmagic.archive.ArchiveTableEntity)h.getBlockEntity(new BlockPos(2,1,2));t.inventory.setItem(0,dev.kelmagic.MagicArchive.CODEX.toStack());t.inventory.setItem(1,dev.kelmagic.MagicArchive.PAGES.get("vak").toStack());h.assertTrue(!t.inventory.getItem(4).isEmpty(),"table previews knowledge");var result=t.inventory.getItem(4).copy();t.finish(p);h.assertTrue(t.inventory.getItem(0).isEmpty()&&t.inventory.getItem(1).isEmpty(),"merge consumes exactly inputs");h.assertTrue(dev.kelmagic.item.CodexItem.knownWords(result).equals(java.util.List.of("vak")),"merged word");t.inventory.setItem(0,result);t.inventory.setItem(1,net.minecraft.world.item.Items.BOOK.getDefaultInstance());t.inventory.setItem(2,net.minecraft.world.item.Items.FEATHER.getDefaultInstance());t.inventory.setItem(3,net.minecraft.world.item.Items.INK_SAC.getDefaultInstance());var copied=t.inventory.getItem(4).copy();t.finish(p);h.assertTrue(!t.inventory.getItem(0).isEmpty()&&t.inventory.getItem(1).isEmpty(),"copy keeps original consumes materials");h.assertTrue(dev.kelmagic.item.CodexItem.knownWords(copied).equals(java.util.List.of("vak")),"copy keeps knowledge");var grid=net.minecraft.world.item.crafting.CraftingInput.of(2,1,java.util.List.of(result,dev.kelmagic.MagicArchive.PAGES.get("mar").toStack()));h.assertTrue(h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,grid,h.getLevel()).isEmpty(),"normal workbench cannot merge");h.succeed();}
    @GameTest(template="empty",timeoutTicks=60)
    public static void nineGrandRitualsRespectWordThresholds(GameTestHelper h){player(h);h.assertTrue(dev.kelmagic.core.GrandSpells.ALL.size()==9,"nine rites");for(var rite:dev.kelmagic.core.GrandSpells.ALL){var f=rite.formula();h.assertTrue(f.size()==rite.minimum(),"exact documented minimum "+rite.id());h.assertTrue(f.stream().allMatch(KelLexicon.WORDS::containsKey),"real Kel roots");var plan=SpellParser.plan(f);h.assertTrue(plan.effect()==SpellParser.Effect.GRAND&&plan.actions().getFirst().equals(rite.id()),"correct rite "+rite.id());var shuffled=new java.util.ArrayList<>(f);java.util.Collections.reverse(shuffled);h.assertTrue(SpellParser.plan(shuffled).equals(plan),"permutation invariant rite");}h.succeed();}
    @GameTest(template="empty",timeoutTicks=100)
    public static void spectralWingsWithoutArmor(GameTestHelper h){var p=player(h);p.setOnGround(false);h.assertTrue(SpellEffects.execute(p,SpellParser.plan(dev.kelmagic.core.GrandSpells.ALL.get(0).formula())),"wings granted");h.assertTrue(p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).isEmpty()&&p.tryToStartFallFlying(),"starts true elytra flight without item");p.aiStep();h.assertTrue(p.isFallFlying(),"vanilla update retains spectral flight");h.succeed();}
    @GameTest(template="empty",timeoutTicks=60)
    public static void islandActuallyMovesTerrain(GameTestHelper h){var p=player(h);clearIslandTestRoof(h);var at=h.absolutePos(new BlockPos(4,1,4));h.setBlock(4,1,4,Blocks.DIRT);h.assertTrue(GrandEffects.raiseIsland(p,at,1),"island lifted");h.assertTrue(h.getLevel().getBlockState(at).isAir()&&h.getLevel().getBlockState(at.above(12)).is(Blocks.DIRT),"moved not duplicated");h.succeed();}
    @GameTest(template="empty",timeoutTicks=80)
    public static void transcendRestoresOriginalModeAndLogout(GameTestHelper h){var p=player(h);h.assertTrue(GrandEffects.ascend(p)&&p.isCreative(),"temporary creative");p.setData(MagicAttachments.CREATIVE_END,h.getLevel().getGameTime());GrandEffects.playerTick(new PlayerTickEvent.Post(p));h.assertFalse(p.isCreative(),"restored at deadline");h.assertTrue(p.getData(MagicAttachments.CREATIVE_PREVIOUS)==-1,"lease cleared");GrandEffects.ascend(p);GrandEffects.logout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));h.assertFalse(p.isCreative(),"logout cannot retain creative");h.succeed();}
    @GameTest(template="empty",timeoutTicks=130)
    public static void temporaryGateExpires(GameTestHelper h){player(h);h.setBlock(5,1,5,dev.kelmagic.MagicPortals.END_GATE.get());h.runAfterDelay(101,()->{h.assertTrue(h.getBlockState(new BlockPos(5,1,5)).isAir(),"gate scheduled lifetime100 ticks");h.succeed();});}
    @GameTest(template="empty",timeoutTicks=60)
    public static void manaTideAddsFiftyThenExpires(GameTestHelper h){var p=player(h);double base=MagicServer.maximum(p);var rite=dev.kelmagic.core.GrandSpells.ALL.stream().filter(x->x.id().equals("mana_tide")).findFirst().orElseThrow();h.assertTrue(SpellEffects.execute(p,SpellParser.plan(rite.formula())),"mana rite self fallback");h.assertTrue(MagicServer.maximum(p)==base+50,"fixed plus50 not scale");p.setData(MagicAttachments.MANA_BOOST_END,h.getLevel().getGameTime());h.assertTrue(MagicServer.maximum(p)==base,"expired max exactly restored");h.succeed();}
    @GameTest(template="empty",batch="archives",timeoutTicks=100)
    public static void fifteenArchivesLoadWithTableAndLoot(GameTestHelper h){player(h);for(String biome:java.util.List.of("plains","desert","taiga","snowy","savanna")){var pool=h.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.TEMPLATE_POOL).get(net.minecraft.resources.ResourceLocation.withDefaultNamespace("village/"+biome+"/houses"));h.assertTrue(pool!=null,"vanilla pool loads "+biome);for(int v=1;v<=3;v++){var template=h.getLevel().getStructureManager().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic","archive/"+biome+"_"+v));h.assertTrue(template.isPresent()&&template.get().getSize().getX()>=9,"archive loaded "+biome+v);}}var t=h.getLevel().getStructureManager().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic","archive/plains_1")).orElseThrow();var at=h.absolutePos(new BlockPos(1,1,1));var snapshots=new java.util.ArrayList<net.neoforged.neoforge.common.util.BlockSnapshot>();for(int x=0;x<t.getSize().getX();x++)for(int y=0;y<t.getSize().getY();y++)for(int z=0;z<t.getSize().getZ();z++)snapshots.add(net.neoforged.neoforge.common.util.BlockSnapshot.create(h.getLevel().dimension(),h.getLevel(),at.offset(x,y,z)));try{h.assertTrue(t.placeInWorld(h.getLevel(),at,at,new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings().setIgnoreEntities(true),h.getLevel().random,2),"real template placed");h.assertTrue(h.getLevel().getBlockState(at.offset(5,1,11)).is(dev.kelmagic.MagicStudy.TABLE.get()),"archive real table");h.assertTrue(h.getLevel().getBlockEntity(at.offset(9,1,11)) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity,"loot chest placed"); }finally{for(int n=snapshots.size()-1;n>=0;n--)snapshots.get(n).restore();}h.succeed();}

    @GameTest(template="empty",timeoutTicks=200)
    public static void piglinThinksThenPaysCustomWoolLoot(GameTestHelper h){var p=player(h);p.setGameMode(GameType.CREATIVE);h.getLevel().addNewPlayer(p);var pig=h.spawn(EntityType.PIGLIN,4.5f,1,4.5f);pig.setNoAi(false);pig.setImmuneToZombification(true);var wool=dev.kelmagic.MagicMaterials.WOOL_ITEM.toStack();h.assertTrue(WoolBarter.trade(p,pig,wool),"direct wool accepted");h.runAfterDelay(150,()->{h.assertTrue(pig.getOffhandItem().isEmpty(),"finished vanilla admiration");var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,pig.getBoundingBox().inflate(8));h.assertTrue(!drops.isEmpty(),"custom reward after thinking");h.assertTrue(drops.stream().noneMatch(x->x.getItem().is(dev.kelmagic.MagicMaterials.GOLDEN_THREAD)||x.getItem().is(dev.kelmagic.MagicMaterials.WOOL_ITEM)),"no golden thread or refunded wool");p.discard();h.succeed();});}
    @GameTest(template="empty",timeoutTicks=280)
    public static void piglinWalksToDroppedWool(GameTestHelper h){player(h);var pig=h.spawn(EntityType.PIGLIN,1.5f,1,4.5f);pig.setNoAi(false);pig.setImmuneToZombification(true);var wool=new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),h.absoluteVec(new Vec3(5.5,1,4.5)).x,h.absoluteVec(new Vec3(5.5,1,4.5)).y,h.absoluteVec(new Vec3(5.5,1,4.5)).z,dev.kelmagic.MagicMaterials.WOOL_ITEM.toStack());wool.setPickUpDelay(0);h.getLevel().addFreshEntity(wool);h.runAfterDelay(80,()->h.assertTrue(wool.isRemoved()&&pig.getOffhandItem().is(dev.kelmagic.MagicMaterials.WOOL_ITEM),"actively approaches and picks up loved currency"));h.runAfterDelay(250,()->{h.assertTrue(pig.getOffhandItem().isEmpty(),"completes dropped-wool barter");h.succeed();});}
    @GameTest(template="empty",timeoutTicks=60)
    public static void duplicateCopiesInventoryWithFreshUuid(GameTestHelper h){var p=player(h);var donkey=h.spawnWithNoFreeWill(EntityType.DONKEY,3.5f,1,4.5f);donkey.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,net.minecraft.world.item.Items.DIAMOND.getDefaultInstance());var aim=new SpellEffects.Aim(donkey.position(),net.minecraft.world.phys.HitResult.Type.ENTITY,new net.minecraft.world.phys.BlockHitResult(p.getEyePosition().add(0,0,48),net.minecraft.core.Direction.NORTH,donkey.blockPosition().relative(net.minecraft.core.Direction.SOUTH,40),false),donkey);h.assertTrue(GrandEffects.duplicate(p,aim),"copy entity");var list=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.animal.horse.Donkey.class,donkey.getBoundingBox().inflate(4));h.assertTrue(list.size()==2&&list.stream().anyMatch(e->!e.getUUID().equals(donkey.getUUID())&&e.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).is(net.minecraft.world.item.Items.DIAMOND)),"fresh identity and copied components");h.succeed();}

    @GameTest(template="empty",timeoutTicks=60)
    public static void archiveMenuShiftTakeConsumesInputsExactlyOnce(GameTestHelper h){var p=player(h);h.setBlock(2,1,2,dev.kelmagic.MagicStudy.TABLE.get());var t=(dev.kelmagic.archive.ArchiveTableEntity)h.getBlockEntity(new BlockPos(2,1,2));var b=dev.kelmagic.MagicArchive.CODEX.toStack();b.set(dev.kelmagic.MagicComponents.CODEX_WORDS.get(),java.util.List.of("vak"));t.inventory.setItem(0,b);t.inventory.setItem(1,net.minecraft.world.item.Items.BOOK.getDefaultInstance());t.inventory.setItem(2,net.minecraft.world.item.Items.FEATHER.getDefaultInstance());t.inventory.setItem(3,net.minecraft.world.item.Items.INK_SAC.getDefaultInstance());var menu=new dev.kelmagic.archive.ArchiveTableMenu(1,p.getInventory(),t);h.assertTrue(menu.stillValid(p),"table validates distance and block");h.assertTrue(!menu.quickMoveStack(p,4).isEmpty(),"shift takes one copy");h.assertTrue(t.inventory.getItem(4).isEmpty()&&!t.inventory.getItem(0).isEmpty(),"no ghost output original stays");h.assertTrue(menu.quickMoveStack(p,4).isEmpty(),"second packet no duplicate");long count=p.getInventory().items.stream().filter(x->x.is(dev.kelmagic.MagicArchive.CODEX)).count();h.assertTrue(count==1,"one real copy");h.succeed();}
    @GameTest(template="empty",timeoutTicks=60)
    public static void allEightGarmentsAcceptVanillaSmithingTrim(GameTestHelper h){var p=player(h);for(var item:java.util.stream.Stream.concat(dev.kelmagic.MagicContent.PLATE_ITEMS.stream(),dev.kelmagic.MagicContent.ROBE_ITEMS.stream()).toList()){var base=item.toStack();base.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,Component.literal("My garment"));base.set(net.minecraft.core.component.DataComponents.DYED_COLOR,new net.minecraft.world.item.component.DyedItemColor(0x4477CC,false));var grid=new net.minecraft.world.item.crafting.SmithingRecipeInput(net.minecraft.world.item.Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE.getDefaultInstance(),base,net.minecraft.world.item.Items.GOLD_INGOT.getDefaultInstance());var recipe=h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMITHING,grid,h.getLevel()).orElseThrow();var result=recipe.value().assemble(grid,h.getLevel().registryAccess());h.assertTrue(result.has(net.minecraft.core.component.DataComponents.TRIM)&&result.getHoverName().getString().equals("My garment"),"vanilla trim and name");h.assertTrue(result.get(net.minecraft.core.component.DataComponents.DYED_COLOR).rgb()==0x4477CC,"color preserved");h.assertTrue(((dev.kelmagic.item.ManaArmorItem)result.getItem()).manaBonus==item.get().manaBonus,"mana bonus preserved");p.getInventory().add(result);}MagicProgress.inventory(p);var achievement=p.getServer().getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic","trim_magic"));h.assertTrue(p.getAdvancements().getOrStartProgress(achievement).isDone(),"trim achievement");h.succeed();}
    @GameTest(template="empty",timeoutTicks=60)
    public static void archivistPoiAndRareManuscriptProgression(GameTestHelper h){var p=player(h);var poi=net.minecraft.world.entity.ai.village.poi.PoiTypes.forState(dev.kelmagic.MagicStudy.TABLE.get().defaultBlockState()).orElseThrow();h.assertTrue(dev.kelmagic.MagicStudy.ARCHIVIST.get().heldJobSite().test(poi),"registered matching job block");h.assertTrue(poi.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.POINT_OF_INTEREST_TYPE,net.minecraft.resources.ResourceLocation.withDefaultNamespace("acquirable_job_site"))),"discoverable job POI");for(var tier:net.minecraft.world.item.Rarity.values()){var ancient=dev.kelmagic.MagicArchive.ANCIENT.toStack();ancient.set(net.minecraft.core.component.DataComponents.RARITY,tier);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ancient);dev.kelmagic.MagicArchive.ANCIENT.get().use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);var page=(dev.kelmagic.item.WordManuscriptItem)p.getMainHandItem().getItem();h.assertTrue(dev.kelmagic.core.WordRarity.of(page.word)==tier,"tier filtered random reveal "+tier);}h.succeed();}
    @GameTest(template="empty",timeoutTicks=60)
    public static void portableEnderMenuUsesOwnInventory(GameTestHelper h){var p=player(h);p.getEnderChestInventory().setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,7));var rite=dev.kelmagic.core.GrandSpells.ALL.stream().filter(x->x.id().equals("ender")).findFirst().orElseThrow();h.assertTrue(SpellEffects.execute(p,SpellParser.plan(rite.formula())),"portable ender opens");h.assertTrue(p.containerMenu instanceof net.minecraft.world.inventory.ChestMenu&&p.containerMenu.getSlot(0).getItem().getCount()==7,"uses player ender inventory");h.succeed();}
    @GameTest(template="empty",timeoutTicks=60)
    public static void ritualBlastActuallyScales(GameTestHelper h){var p=player(h);var radii=new java.util.ArrayList<Float>();java.util.function.Consumer<net.neoforged.neoforge.event.level.ExplosionEvent.Start> listener=e->{if(e.getExplosion().getDirectSourceEntity()==p){radii.add(e.getExplosion().radius());e.setCanceled(true);}};net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);try{SpellEffects.execute(p,SpellParser.plan(java.util.List.of("pir","tup","dun","por","muk")));SpellEffects.execute(p,SpellParser.plan(java.util.List.of("pir","tup","dun","por","muk","muk","muk")));h.assertTrue(radii.size()==2&&radii.get(1)>radii.get(0),"real explosion larger with muk");}finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);}h.succeed();}

    @GameTest(template="empty",timeoutTicks=60)
    public static void allAchievementsAnnounceAndPirEpic(GameTestHelper h){var p=player(h);for(String id:java.util.List.of("trim_magic","dye_robes","rite_wings","rite_transcend","archive_word")){var a=p.getServer().getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic",id));h.assertTrue(a.value().display().orElseThrow().shouldAnnounceChat(),"chat announcement enabled "+id);}h.assertTrue(dev.kelmagic.core.WordRarity.of("pir")==net.minecraft.world.item.Rarity.EPIC,"pir epic");h.succeed();}
    @GameTest(template="empty",timeoutTicks=60)
    public static void administrativeManaPersistentAndClamped(GameTestHelper h){var p=player(h);MagicCommands.setMaximum(p,80.5);MagicCommands.setCurrent(p,100);h.assertTrue(MagicServer.maximum(p)==80.5&&p.getData(MagicAttachments.MANA).current()==80.5,"maximum/current commands");MagicCommands.setHeart(p,true);h.assertTrue(p.getData(MagicAttachments.MANA).dragonUpgrade(),"heart grant");MagicCommands.setHeart(p,false);h.assertFalse(p.getData(MagicAttachments.MANA).dragonUpgrade(),"heart revoke");p.setData(MagicAttachments.BASE_MANA_MAX,-1d);h.assertTrue(MagicServer.maximum(p)==5,"reset natural base");MagicCommands.setCurrent(p,-5);h.assertTrue(p.getData(MagicAttachments.MANA).current()==0,"negative clamp");h.succeed();}
    @GameTest(template="empty",timeoutTicks=60)
    public static void commandTreePermissionsAndSuggestions(GameTestHelper h){var p=player(h);var d=new com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack>();MagicCommands.register(d);var source=p.createCommandSourceStack().withPermission(0);h.assertTrue(d.getRoot().getChild("kelmagic").getChild("admin")!=null,"admin tree");h.assertFalse(d.getRoot().getChild("kelmagic").getChild("admin").canUse(source),"no permission escalation");h.assertTrue(d.getRoot().getChild("kelmagic").getChild("admin").canUse(source.withPermission(2)),"operator access");h.succeed();}
    @GameTest(template="empty",timeoutTicks=60)
    public static void manaTideVisibleAndMilkClearsBoost(GameTestHelper h){var p=player(h);var r=dev.kelmagic.core.GrandSpells.ALL.stream().filter(x->x.id().equals("mana_tide")).findFirst().orElseThrow();SpellEffects.execute(p,SpellParser.plan(r.formula()));h.assertTrue(p.hasEffect(dev.kelmagic.MagicContent.MANA_TIDE)&&p.getEffect(dev.kelmagic.MagicContent.MANA_TIDE).getDuration()==1200,"status visible for60sec");p.removeEffect(dev.kelmagic.MagicContent.MANA_TIDE);h.assertTrue(MagicServer.maximum(p)==5,"removal cancels boost");h.succeed();}
    @GameTest(template="empty",timeoutTicks=100)
    public static void ghostFlightAcceptsVanillaRocket(GameTestHelper h){var p=player(h);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(dev.kelmagic.MagicContent.WINGS,200));p.setOnGround(false);h.assertTrue(p.tryToStartFallFlying(),"start ghost elytra");p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FIREWORK_ROCKET,2));var response=net.minecraft.world.item.Items.FIREWORK_ROCKET.use(h.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);h.assertTrue(response.getResult().consumesAction()&&p.getMainHandItem().getCount()==1,"vanilla rocket used");var rockets=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.FireworkRocketEntity.class,p.getBoundingBox().inflate(2));h.assertTrue(rockets.size()==1,"attached booster spawned");var before=p.getDeltaMovement();rockets.getFirst().tick();h.assertTrue(p.getDeltaMovement().subtract(before).dot(p.getLookAngle())>0,"rocket accelerates forward");h.succeed();}
    @GameTest(template="empty",timeoutTicks=80)
    public static void islandMovesBuildingAndSkipsInventory(GameTestHelper h){var p=player(h);clearIslandTestRoof(h);var center=h.absolutePos(new BlockPos(4,1,4));h.setBlock(4,1,4,Blocks.DIRT);h.setBlock(4,2,4,Blocks.OAK_PLANKS);h.setBlock(4,3,4,Blocks.GLASS);h.setBlock(5,2,4,Blocks.CHEST);var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)h.getBlockEntity(new BlockPos(5,2,4));chest.setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND,3));h.assertTrue(GrandEffects.raiseIsland(p,center,1),"building lifted");boolean found=false;for(int y=13;y<40;y++)if(h.getLevel().getBlockState(center.above(y-1)).is(Blocks.OAK_PLANKS))found=true;h.assertTrue(found&&h.getLevel().getBlockState(center.above()).isAir(),"planks moved not terrain-only");h.assertTrue(h.getBlockState(new BlockPos(5,2,4)).is(Blocks.CHEST)&&chest.getItem(0).getCount()==3,"inventory unchanged");h.succeed();}
    @GameTest(template="empty",timeoutTicks=60)
    public static void allMukContributeBeyondOldSixLimit(GameTestHelper h){player(h);var words=new java.util.ArrayList<>(java.util.List.of("pir","tup","dun","por"));for(int i=0;i<7;i++)words.add("muk");double a=SpellParser.plan(words).scale();words.add("muk");h.assertTrue(SpellParser.plan(words).scale()>a,"eighth muk contributes");while(words.size()<100)words.add("muk");h.assertTrue(SpellParser.plan(words).scale()==49,"no hidden scale cap,100word bound");h.succeed();}
    @GameTest(template="empty",timeoutTicks=130)
    public static void gateHasValidNativePortalRendererEntity(GameTestHelper h){player(h);h.setBlock(5,1,5,dev.kelmagic.MagicPortals.END_GATE.get());var be=h.getBlockEntity(new BlockPos(5,1,5));h.assertTrue(be instanceof net.minecraft.world.level.block.entity.TheEndPortalBlockEntity&&be.getType().isValid(be.getBlockState()),"native layered portal rendering with valid custom entity type");h.runAfterDelay(101,()->{h.assertTrue(h.getLevel().getBlockEntity(h.absolutePos(new BlockPos(5,1,5)))==null,"portal entity removed after5sec");h.succeed();});}
    @GameTest(template="empty",timeoutTicks=60)
    public static void magicLightCreativelyPlaceableAndAimSelected(GameTestHelper h){var p=player(h);h.assertTrue(dev.kelmagic.MagicLamps.TORCH_ITEM.get() instanceof net.minecraft.world.item.StandingAndWallBlockItem,"floor and wall creative item");var at=new BlockPos(3,0,5);var aim=blockAim(h,at);h.assertTrue(WorldSpells.execute(p,aim,SpellParser.plan(java.util.List.of("sol")),SpellProfiles.ALL.get("sol")),"aimed light");h.assertTrue(h.getBlockState(at.above()).is(dev.kelmagic.MagicLamps.TORCH.get()),"not under caster");h.assertTrue(dev.kelmagic.MagicLamps.torchState(net.minecraft.core.Direction.EAST).is(dev.kelmagic.MagicLamps.WALL_TORCH.get()),"wall direction supported");h.succeed();}

    // GameTest wraps its tiny templates in invisible barrier boxes. Remove only fixture roofs,
    // never weaken production obstruction/protection checks just to make an island pass.
    private static void clearIslandTestRoof(GameTestHelper h){for(int x=-5;x<=14;x++)for(int z=-5;z<=14;z++)for(int y=5;y<=60;y++){var at=h.absolutePos(new BlockPos(x,y,z));if(h.getLevel().hasChunkAt(at)&&h.getLevel().getBlockState(at).is(Blocks.BARRIER))h.getLevel().setBlock(at,Blocks.AIR.defaultBlockState(),2);}}
    @GameTest(template="empty",timeoutTicks=60)
    public static void rocketAchievementOnlyWithSpectralFlight(GameTestHelper h){var p=player(h);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.Items.FIREWORK_ROCKET.getDefaultInstance());var a=p.getServer().getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic","spectral_boost"));MagicProgress.rocket(new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem(p,net.minecraft.world.InteractionHand.MAIN_HAND));h.assertFalse(p.getAdvancements().getOrStartProgress(a).isDone(),"ordinary rocket not ghost boost");p.addEffect(new net.minecraft.world.effect.MobEffectInstance(dev.kelmagic.MagicContent.WINGS,200));p.setOnGround(false);p.tryToStartFallFlying();MagicProgress.rocket(new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem(p,net.minecraft.world.InteractionHand.MAIN_HAND));h.assertTrue(p.getAdvancements().getOrStartProgress(a).isDone(),"spectral rocket achievement");h.succeed();}
    @GameTest(template="empty",timeoutTicks=80)
    public static void untouchedMobsDoNotAllocateMagicAttachments(GameTestHelper h){var cow=EntityType.COW.create(h.getLevel());h.assertFalse(cow.hasData(MagicAttachments.ARCANE_BURN),"fresh cow has no burn data");h.assertFalse(cow.hasData(MagicAttachments.NEUTRAL_KIND),"fresh cow has no neutral data");var testPlayer=player(h);for(int i=0;i<100;i++){ArcaneFire.advance(cow);NeutralMagic.tick(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(cow));NeutralMagic.mayTarget(cow,testPlayer);}h.assertFalse(cow.hasData(MagicAttachments.ARCANE_BURN),"idle fire tick is allocation-free");h.assertFalse(cow.hasData(MagicAttachments.NEUTRAL_KIND),"idle AI hook is allocation-free");h.succeed();}
    private static net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece archiveTestPiece(ServerLevel level,String id,BlockPos pos){var element=net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement.single(id).apply(net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection.RIGID);return new net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece(level.getStructureManager(),element,pos,1,net.minecraft.world.level.block.Rotation.NONE,element.getBoundingBox(level.getStructureManager(),pos,net.minecraft.world.level.block.Rotation.NONE),net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings.APPLY_WATERLOGGING);}
    @GameTest(template="empty",timeoutTicks=80)
    public static void archiveSearchRecognizesRealPiecesOnly(GameTestHelper h){var p=player(h);var l=h.getLevel();var registry=l.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);var village=registry.get(net.minecraft.resources.ResourceLocation.withDefaultNamespace("village_plains"));for(String biome:java.util.List.of("plains","desert","taiga","snowy","savanna"))for(int variant=1;variant<=3;variant++){var piece=archiveTestPiece(l,"kelmagic:archive/"+biome+"_"+variant,p.blockPosition());var start=new net.minecraft.world.level.levelgen.structure.StructureStart(village,new net.minecraft.world.level.ChunkPos(p.blockPosition()),0,new net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer(java.util.List.of(piece)));var positions=dev.kelmagic.worldgen.ArchiveLocator.positions(start);h.assertTrue(positions.size()==1&&positions.getFirst().equals(piece.getBoundingBox().getCenter()),"exact archive center "+biome+variant);}h.assertTrue(dev.kelmagic.worldgen.ArchiveLocator.positions(net.minecraft.world.level.levelgen.structure.StructureStart.INVALID_START).isEmpty(),"invalid start ignored");h.succeed();}
    @GameTest(template="empty",timeoutTicks=100)
    public static void archiveRegisteredForVanillaLocateAndTag(GameTestHelper h){var p=player(h);var l=h.getLevel();var r=l.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);var holder=r.getHolder(dev.kelmagic.worldgen.ArchiveWorldgen.ARCHIVE).orElseThrow();h.assertTrue(holder.value() instanceof dev.kelmagic.worldgen.ArchiveStructure,"search registry target");var tag=r.getTag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.STRUCTURE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic","archives"))).orElseThrow();h.assertTrue(tag.contains(holder),"archive tag");var d=p.getServer().getCommands().getDispatcher();for(String command:java.util.List.of("locate structure kelmagic:archive","locate structure #kelmagic:archives")){var parse=d.parse(command,p.createCommandSourceStack().withPermission(2));h.assertTrue(parse.getExceptions().isEmpty()&&parse.getReader().getRemaining().isEmpty(),"vanilla command accepts "+command);}var source=p.createCommandSourceStack().withPermission(0);h.assertFalse(d.getRoot().getChild("locate").canUse(source),"vanilla operator permissions retained");h.succeed();}
    @GameTest(template="empty",timeoutTicks=100)
    public static void nativeLocateFindsArchiveVillageStart(GameTestHelper h){var p=player(h);var l=h.getLevel();var r=l.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);var village=r.getHolder(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE,net.minecraft.resources.ResourceLocation.withDefaultNamespace("village_plains"))).orElseThrow();var state=l.getChunkSource().getGeneratorState();var placement=state.getPlacementsForStructure(village).stream().filter(v->v instanceof net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement).map(v->(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement)v).findFirst().orElseThrow();var origin=new net.minecraft.world.level.ChunkPos(p.blockPosition());var candidate=placement.getPotentialStructureChunk(state.getLevelSeed(),origin.x,origin.z);var chunk=l.getChunk(candidate.x,candidate.z,net.minecraft.world.level.chunk.status.ChunkStatus.FULL);var old=chunk.getStartForStructure(village.value());var at=new BlockPos(candidate.getMiddleBlockX(),70,candidate.getMiddleBlockZ());var piece=archiveTestPiece(l,"kelmagic:archive/plains_1",at);var start=new net.minecraft.world.level.levelgen.structure.StructureStart(village.value(),candidate,0,new net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer(java.util.List.of(piece)));chunk.setStartForStructure(village.value(),start);try{var holder=r.getHolder(dev.kelmagic.worldgen.ArchiveWorldgen.ARCHIVE).orElseThrow();var result=l.getChunkSource().getGenerator().findNearestMapStructure(l,net.minecraft.core.HolderSet.direct(holder),at,0,false);h.assertTrue(result!=null&&result.getFirst().equals(piece.getBoundingBox().getCenter())&&result.getSecond().equals(holder),"vanilla generator path finds actual archive, not table; generation="+l.getServer().getWorldData().worldGenOptions().generateStructures()+" result="+result);var tag=r.getTag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.STRUCTURE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic","archives"))).orElseThrow();h.assertTrue(l.getChunkSource().getGenerator().findNearestMapStructure(l,tag,at,0,false)!=null,"native tag search");}finally{chunk.setStartForStructure(village.value(),old==null?net.minecraft.world.level.levelgen.structure.StructureStart.INVALID_START:old);}h.succeed();}
    @GameTest(template="empty",timeoutTicks=80)
    public static void archivistTradeAchievementOnlyForArchivist(GameTestHelper h){var p=player(h);var v=EntityType.VILLAGER.create(h.getLevel());var offer=new net.minecraft.world.item.trading.MerchantOffer(new net.minecraft.world.item.trading.ItemCost(net.minecraft.world.item.Items.EMERALD,1),net.minecraft.world.item.Items.BOOK.getDefaultInstance(),12,1,.05f);var a=p.getServer().getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic","archive_patron"));v.setVillagerData(v.getVillagerData().setProfession(net.minecraft.world.entity.npc.VillagerProfession.LIBRARIAN));MagicProgress.trade(new net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent(p,offer,v));h.assertFalse(p.getAdvancements().getOrStartProgress(a).isDone(),"other profession not credited");v.setVillagerData(v.getVillagerData().setProfession(dev.kelmagic.MagicStudy.ARCHIVIST.get()));MagicProgress.trade(new net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent(p,offer,v));h.assertTrue(p.getAdvancements().getOrStartProgress(a).isDone(),"archivist trade credited");h.succeed();}

    @GameTest(template="empty",timeoutTicks=80)
    public static void invisibleFireAgeDoesNotRequestClientMeshRebuild(GameTestHelper h){player(h);var fire=dev.kelmagic.MagicContent.MAGIC_FIRE.get().defaultBlockState();for(int age=0;age<16;age++)for(int pulse=0;pulse<4;pulse++){var aged=fire.setValue(net.minecraft.world.level.block.FireBlock.AGE,age).setValue(dev.kelmagic.block.MagicFireBlock.PULSE,pulse);h.assertTrue(dev.kelmagic.block.MagicFireBlock.updateFlags(fire,aged)==net.minecraft.world.level.block.Block.UPDATE_INVISIBLE,"server counter does not trigger client rebuild");}var visible=fire.setValue(net.minecraft.world.level.block.FireBlock.NORTH,true);h.assertTrue(dev.kelmagic.block.MagicFireBlock.updateFlags(fire,visible)==net.minecraft.world.level.block.Block.UPDATE_CLIENTS,"shape changes still reach client");h.succeed();}

 @GameTest(template="empty",timeoutTicks=160)
 public static void queuedLocateYieldsAndCompletesWithVanillaCoordinates(GameTestHelper h)throws Exception {var p=player(h);var l=h.getLevel();var r=l.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);var village=r.getHolder(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE,net.minecraft.resources.ResourceLocation.withDefaultNamespace("village_plains"))).orElseThrow();var state=l.getChunkSource().getGeneratorState();var placement=(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement)state.getPlacementsForStructure(village).getFirst();var center=new net.minecraft.world.level.ChunkPos(p.blockPosition());var candidate=placement.getPotentialStructureChunk(state.getLevelSeed(),center.x,center.z);var chunk=l.getChunk(candidate.x,candidate.z);var old=chunk.getStartForStructure(village.value());var at=new BlockPos(candidate.getMiddleBlockX(),70,candidate.getMiddleBlockZ());var piece=archiveTestPiece(l,"kelmagic:archive/plains_3",at);chunk.setStartForStructure(village.value(),new net.minecraft.world.level.levelgen.structure.StructureStart(village.value(),candidate,0,new net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer(java.util.List.of(piece))));var source=p.createCommandSourceStack().withPermission(2).withPosition(net.minecraft.world.phys.Vec3.atCenterOf(at));var argument=net.minecraft.commands.arguments.ResourceOrTagKeyArgument.resourceOrTagKey(net.minecraft.core.registries.Registries.STRUCTURE).parse(new com.mojang.brigadier.StringReader("kelmagic:archive"));h.assertTrue(dev.kelmagic.worldgen.ArchiveSearch.begin(source.withPermission(0),argument)==0,"unauthorized search refused");h.assertTrue(dev.kelmagic.worldgen.ArchiveSearch.begin(source,argument)==1,"first search queued");h.assertTrue(dev.kelmagic.worldgen.ArchiveSearch.begin(source,argument)==1&&dev.kelmagic.worldgen.ArchiveSearch.pending()==1,"repeat replaces own job");h.assertTrue(dev.kelmagic.worldgen.ArchiveSearch.cancel(source)==1&&dev.kelmagic.worldgen.ArchiveSearch.cancel(source)==0,"cancel closes only existing job");long begin=System.nanoTime();h.assertTrue(dev.kelmagic.worldgen.ArchiveSearch.begin(source,argument)==1&&dev.kelmagic.worldgen.ArchiveSearch.pending()==1,"command queues instead of completing synchronously");h.assertTrue(System.nanoTime()-begin<250_000_000,"queue call bounded, no chunk wait");h.runAfterDelay(12,()->{try{h.assertTrue(dev.kelmagic.worldgen.ArchiveSearch.pending()==0,"search completes across server ticks");var adv=p.getServer().getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic","archive_route"));h.assertTrue(adv==null,"command search achievement removed");h.succeed();}finally{chunk.setStartForStructure(village.value(),old==null?net.minecraft.world.level.levelgen.structure.StructureStart.INVALID_START:old);}});}

 @GameTest(template="empty",timeoutTicks=80)
 public static void loreKeepsWordsAndPracticalNotesSeparate(GameTestHelper h){player(h);var unique=new java.util.HashSet<String>();for(var word:dev.kelmagic.core.KelLexicon.WORDS.keySet()){var lore=dev.kelmagic.core.SpellProfiles.describe(word);var practical=dev.kelmagic.core.SpellProfiles.mechanics(word);h.assertTrue(lore.length()>40&&unique.add(lore),"distinct authored fragment "+word);h.assertTrue(!practical.isBlank()&&!lore.equals(practical),"practical notes separate "+word);}h.assertTrue(unique.size()==180,"all verified words retained");h.succeed();}

 @GameTest(template="empty",timeoutTicks=100,batch="copper")
 public static void allGreatWordsHaveNativeCopperChainsRecipesAndLoot(GameTestHelper h){var p=player(h);var l=h.getLevel();var nativeSet=GrandSpells.ALL.stream().flatMap(r->r.formula().stream()).collect(java.util.stream.Collectors.toSet());h.assertTrue(nativeSet.equals(new java.util.HashSet<>(dev.kelmagic.MagicCopper.WORDS))&&nativeSet.size()==74,"74 canonical rite words");for(String word:dev.kelmagic.MagicCopper.WORDS)for(int age=0;age<4;age++){var b=dev.kelmagic.MagicCopper.block(word,age,false);var state=b.defaultBlockState();var wax=dev.kelmagic.MagicCopper.block(word,age,true);h.assertTrue(b instanceof net.minecraft.world.level.block.WeatheringCopper&&((net.minecraft.world.level.block.WeatheringCopper)b).getAge().ordinal()==age,"native age "+word);h.assertTrue(state.isRandomlyTicking()==(age<3)&&!wax.defaultBlockState().isRandomlyTicking(),"random ticks stop at final/waxed stage");h.assertTrue(net.minecraft.world.level.block.WeatheringCopper.getFirst(b)==dev.kelmagic.MagicCopper.block(word,0,false),"lightning first stage preserves word");h.assertTrue(net.minecraft.world.level.block.WeatheringCopper.getNext(b).orElse(null)==(age<3?dev.kelmagic.MagicCopper.block(word,age+1,false):null),"next transition");h.assertTrue(net.minecraft.world.level.block.WeatheringCopper.getPrevious(b).orElse(null)==(age>0?dev.kelmagic.MagicCopper.block(word,age-1,false):null),"scrape transition");h.assertTrue(net.minecraft.world.item.HoneycombItem.getWaxed(state).orElseThrow().is(wax)&&net.neoforged.neoforge.common.DataMapHooks.getBlockUnwaxed(wax)==b,"wax/unwax roundtrip");for(boolean waxed:new boolean[]{false,true}){var id=dev.kelmagic.MagicCopper.id(word,age,waxed);var recipe=l.getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic",id));h.assertTrue(recipe.isPresent()&&recipe.get().value() instanceof net.minecraft.world.item.crafting.StonecutterRecipe,"stonecutting "+id);var drops=net.minecraft.world.level.block.Block.getDrops(dev.kelmagic.MagicCopper.block(word,age,waxed).defaultBlockState(),l,h.absolutePos(new BlockPos(1,1,1)),null,p,net.minecraft.world.item.Items.IRON_PICKAXE.getDefaultInstance());h.assertTrue(drops.size()==1&&drops.getFirst().is(dev.kelmagic.MagicCopper.ITEMS.get(id).get()),"word/state drop "+id);}}h.succeed();}
 @GameTest(template="empty",timeoutTicks=100,batch="copper")
 public static void nativeCopperRandomTickAndToolsWork(GameTestHelper h){var p=player(h);var at=h.absolutePos(new BlockPos(1,1,1));var l=h.getLevel();var old=l.getBlockState(at);try{for(int age=0;age<3;age++){var start=dev.kelmagic.MagicCopper.block("pir",age,false);l.setBlockAndUpdate(at,start.defaultBlockState());var random=net.minecraft.util.RandomSource.create(12345);for(int n=0;n<5000&&l.getBlockState(at).is(start);n++)l.getBlockState(at).randomTick(l,at,random);h.assertTrue(l.getBlockState(at).is(dev.kelmagic.MagicCopper.block("pir",age+1,false)),"native stochastic aging "+age);}for(int age=0;age<4;age++){l.setBlockAndUpdate(at,dev.kelmagic.MagicCopper.block("pir",age,false).defaultBlockState());p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.HONEYCOMB,2));var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(at),net.minecraft.core.Direction.UP,at,false);var context=new net.minecraft.world.item.context.UseOnContext(p,net.minecraft.world.InteractionHand.MAIN_HAND,hit);h.assertTrue(net.minecraft.world.item.Items.HONEYCOMB.useOn(context).consumesAction(),"honeycomb used");h.assertTrue(l.getBlockState(at).is(dev.kelmagic.MagicCopper.block("pir",age,true)),"wax stage");p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.Items.IRON_AXE.getDefaultInstance());h.assertTrue(net.minecraft.world.item.Items.IRON_AXE.useOn(new net.minecraft.world.item.context.UseOnContext(p,net.minecraft.world.InteractionHand.MAIN_HAND,hit)).consumesAction(),"axe removes wax");h.assertTrue(l.getBlockState(at).is(dev.kelmagic.MagicCopper.block("pir",age,false)),"wax off retains age");if(age>0){net.minecraft.world.item.Items.IRON_AXE.useOn(new net.minecraft.world.item.context.UseOnContext(p,net.minecraft.world.InteractionHand.MAIN_HAND,hit));h.assertTrue(l.getBlockState(at).is(dev.kelmagic.MagicCopper.block("pir",age-1,false)),"axe scrapes one age");}}}finally{l.setBlockAndUpdate(at,old);}h.succeed();}
 @GameTest(template="empty",timeoutTicks=100,batch="copper")
 public static void copperCommandChecksPermissionsAndGivesExactState(GameTestHelper h){var p=player(h);var source=p.createCommandSourceStack().withPermission(2);h.assertTrue(dev.kelmagic.server.MagicCommands.copper(source.withPermission(0),java.util.List.of(p),"pir","fresh",false)==0,"permission required");h.assertTrue(dev.kelmagic.server.MagicCommands.copper(source,java.util.List.of(p),"bad_word","fresh",false)==0,"unverified word rejected");h.assertTrue(dev.kelmagic.server.MagicCommands.copper(source,java.util.List.of(p),"pir","invalid",false)==0,"stage rejected");h.assertTrue(dev.kelmagic.server.MagicCommands.copper(source,java.util.List.of(p),"pir","weathered",true)==1,"gives exact requested copper");var stack=dev.kelmagic.MagicCopper.ITEMS.get(dev.kelmagic.MagicCopper.id("pir",2,true)).toStack();h.assertTrue(p.getInventory().contains(stack),"waxed weathered item delivered");net.minecraft.advancements.CriteriaTriggers.INVENTORY_CHANGED.trigger(p,p.getInventory(),stack);var adv=p.getServer().getAdvancements().get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("kelmagic","copper_word"));h.assertTrue(adv!=null&&p.getAdvancements().getOrStartProgress(adv).isDone(),"native copper achievement registered");h.succeed();}

 @GameTest(template="empty",timeoutTicks=100,batch="copper")
 public static void nativeLightningAndYoungerCopperNeighboursRespectGlyphs(GameTestHelper h){var p=player(h);var l=h.getLevel();var at=h.absolutePos(new BlockPos(1,1,1));var neighbour=at.east();var old=l.getBlockState(at);var before=l.getBlockState(neighbour);try{l.setBlockAndUpdate(at,dev.kelmagic.MagicCopper.block("sap",2,false).defaultBlockState());l.setBlockAndUpdate(neighbour,Blocks.COPPER_BLOCK.defaultBlockState());var rng=net.minecraft.util.RandomSource.create(42);for(int i=0;i<5000;i++)l.getBlockState(at).randomTick(l,at,rng);h.assertTrue(l.getBlockState(at).is(dev.kelmagic.MagicCopper.block("sap",2,false)),"vanilla younger neighbour suppresses weathering");l.setBlockAndUpdate(neighbour,Blocks.AIR.defaultBlockState());var bolt=EntityType.LIGHTNING_BOLT.create(l);bolt.setVisualOnly(true);bolt.moveTo(at.getX()+.5,at.getY()+1,at.getZ()+.5);bolt.tick();h.assertTrue(l.getBlockState(at).is(dev.kelmagic.MagicCopper.block("sap",0,false)),"native lightning resets copper and keeps word");bolt.discard();l.setBlockAndUpdate(at,dev.kelmagic.MagicCopper.block("sap",2,true).defaultBlockState());var waxBolt=EntityType.LIGHTNING_BOLT.create(l);waxBolt.setVisualOnly(true);waxBolt.moveTo(at.getX()+.5,at.getY()+1,at.getZ()+.5);waxBolt.tick();h.assertTrue(l.getBlockState(at).is(dev.kelmagic.MagicCopper.block("sap",2,true)),"wax resists lightning");waxBolt.discard();}finally{l.setBlockAndUpdate(at,old);l.setBlockAndUpdate(neighbour,before);}h.succeed();}

}
