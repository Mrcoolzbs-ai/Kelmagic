from pathlib import Path
r=Path('/data/KelMagic')
f=r/'src/main/java/dev/kelmagic/server/ArcaneFire.java';s=f.read_text();s=s.replace('  e.setData(MagicAttachments.ARCANE_BURN,left-1);\n','');s=s.replace('  if(left==1 || e.tickCount%20==0)sync(e);','  // Keep the status alive during the final damage call so its death loot is cooked too.\n  e.setData(MagicAttachments.ARCANE_BURN,left-1);\n  if(left==1 || e.tickCount%20==0)sync(e);');f.write_text(s)
f=r/'src/main/java/dev/kelmagic/gametest/KelGameTests.java';s=f.read_text();idx=s.index('    @GameTest(template="empty",timeoutTicks=60)\n    public static void manuscriptReveal')
new='''    @GameTest(template="empty",timeoutTicks=130)
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
'''
s=s[:idx]+new+s[idx:];f.write_text(s)
print('Added 9 gameplay regressions (52 total).')
