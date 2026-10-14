from pathlib import Path
p=Path('/data/KelMagic/src/main/java/dev/kelmagic/gametest/KelGameTests.java');s=p.read_text();a=s.index('    @GameTest(template="empty",timeoutTicks=60)\n    public static void worldProfilesDispatchWithRealFixtures');b=s.index('\n    @GameTest',a+10);old=s[a:b]
setup=old[old.index('            for(int x=3;'):old.index('            h.assertTrue(WorldSpells.execute')]
new='''    @GameTest(template="empty",timeoutTicks=240)
    public static void worldProfilesDispatchWithRealFixtures(GameTestHelper h){
        var p=player(h);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE));var checked=new java.util.concurrent.atomic.AtomicInteger();var sequence=h.startSequence();
        for(var entry:SpellProfiles.ALL.entrySet()){
            var profile=entry.getValue();if(!profile.action().startsWith("W_")&&!profile.action().equals("TURN"))continue;
            var target=new BlockPos[1];
            sequence.thenExecute(()->{
'''+setup+'''                target[0]=pos;
            });
            sequence.thenWaitUntil(()->{var action=profile.action();if(action.equals("W_PLANT")&&profile.status().equals("wheat")||java.util.Set.of("W_GROW","W_GROW_AREA","W_TICK","W_HARVEST").contains(action)){var crop=action.equals("W_PLANT")?target[0].above():target[0];h.assertTrue(h.getLevel().getRawBrightness(h.absolutePos(crop),0)>=8,"wait for actual crop light propagation");}});
            sequence.thenExecute(()->{h.assertTrue(WorldSpells.execute(p,blockAim(h,target[0]),SpellParser.plan(java.util.List.of(entry.getKey())),profile),"real world effect "+entry.getKey()+" / "+profile.action());checked.incrementAndGet();});
        }
        sequence.thenExecute(()->h.assertTrue(checked.get()>=60,"broad distinct world coverage")).thenSucceed();
    }
'''
s=s[:a]+new+s[b:];p.write_text(s)
