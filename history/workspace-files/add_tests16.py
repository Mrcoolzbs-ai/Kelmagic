from pathlib import Path
p=Path('/data/KelMagic/src/main/java/dev/kelmagic/gametest/KelGameTests.java');s=p.read_text();idx=s.rfind('    private KelGameTests()');print('constructor index',idx)
# Append just before the final class brace, keeping all previous regressions.
idx=s.rfind('}')
add='''
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
            var recipe=h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,h.getLevel()).orElseThrow().value();
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
        h.assertTrue(p.getData(MagicAttachments.ARCANE_BURN)==1200,"nis shortens base 1800 ticks");
        h.assertTrue(Math.abs(p.getData(MagicAttachments.ARCANE_POWER)-2f/3)<.0001,"nis weakens arcane damage");
        var mob=h.spawnWithNoFreeWill(EntityType.COW,3.5f,1,3.5f);ArcaneFire.ignite(mob,SpellProfiles.duration(1800,2.25),2.25f);
        h.assertTrue(mob.getData(MagicAttachments.ARCANE_BURN)==3600,"repeated muk respects duration cap");
        h.assertTrue(mob.getData(MagicAttachments.ARCANE_POWER)==2.25f,"repeated muk increases power");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=190)
    public static void scaledBlockFireLifetimes(GameTestHelper h){
        player(h);var fire=dev.kelmagic.MagicContent.MAGIC_FIRE.get();var smaller=new BlockPos(2,1,3),larger=new BlockPos(5,1,3);
        h.setBlock(smaller,fire.defaultBlockState().setValue(dev.kelmagic.block.MagicFireBlock.SCALE,5));h.setBlock(larger,fire.defaultBlockState().setValue(dev.kelmagic.block.MagicFireBlock.SCALE,7));
        h.runAfterDelay(66,()->h.assertTrue(h.getBlockState(smaller).is(fire),"nis fire lives until tick 67"));
        h.runAfterDelay(68,()->h.assertTrue(h.getBlockState(smaller).isAir(),"nis block expires at scaled time"));
        h.runAfterDelay(149,()->h.assertTrue(h.getBlockState(larger).is(fire),"muk block lives until tick 150"));
        h.runAfterDelay(151,()->{h.assertTrue(h.getBlockState(larger).isAir(),"muk block expires at scaled time");h.succeed();});
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void everyWordProfileExecutes(GameTestHelper h){
        var p=player(h);h.spawnWithNoFreeWill(EntityType.COW,3.5f,1,3.5f);
        for(var entry:SpellProfiles.ALL.entrySet()){
            var action=entry.getValue().action();if(action.equals("OPEN")||action.equals("CLOSE"))continue;
            p.removeAllEffects();p.invulnerableTime=0;p.setHealth(10);p.getFoodData().setFoodLevel(5);p.igniteForTicks(100);
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON,100));
            h.assertTrue(SpellEffects.execute(p,SpellParser.plan(java.util.List.of("mina",entry.getKey()))),"real handler executes "+entry.getKey());
        }h.succeed();
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
        var p=player(h);p.setHealth(10);h.assertTrue(SpellEffects.execute(p,SpellParser.plan(java.util.List.of("rap","mina","liv"))),"multi-profile spell executes");
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
'''
s=s[:idx]+add+s[idx:];p.write_text(s)
