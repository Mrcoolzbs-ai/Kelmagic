from pathlib import Path
p=Path('src/main/java/dev/kelmagic')
def replace(f,a,b):
 q=p/f;s=q.read_text();assert a in s,(f,a);q.write_text(s.replace(a,b))
q=p/'content/MagicGrassBlock.java';q.write_text('''package dev.kelmagic.content;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.*;
/** Vanilla turf ecology, with a static colour renderer (no block-entity tick/network updates). */
public final class MagicGrassBlock extends GrassBlock implements EntityBlock {
 public static final MapCodec<MagicGrassBlock> CODEC=simpleCodec(MagicGrassBlock::new);
 public MagicGrassBlock(Properties p){super(p);}
 @Override public MapCodec<MagicGrassBlock> codec(){return CODEC;}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Entity(p,s);}
 @Override public BlockState getToolModifiedState(BlockState state,UseOnContext c,ItemAbility action,boolean simulate){
  if(!c.getItemInHand().canPerformAction(action))return null;
  if(action==ItemAbilities.HOE_TILL&&c.getClickedFace()!=net.minecraft.core.Direction.DOWN&&c.getLevel().isEmptyBlock(c.getClickedPos().above()))return Blocks.FARMLAND.defaultBlockState();
  if(action==ItemAbilities.SHOVEL_FLATTEN&&c.getClickedFace()!=net.minecraft.core.Direction.DOWN&&c.getLevel().isEmptyBlock(c.getClickedPos().above()))return Blocks.DIRT_PATH.defaultBlockState();
  return super.getToolModifiedState(state,c,action,simulate);
 }
 public static final class Entity extends BlockEntity {public Entity(BlockPos p,BlockState s){super(MagicDreams.GRASS_ENTITY.get(),p,s);}}
}
''')
replace('content/MagicDreams.java','Properties.of().mapColor(net.minecraft.world.level.material.MapColor.COLOR_BLUE).strength(.6f).sound(SoundType.GRASS)','Properties.ofFullCopy(Blocks.GRASS_BLOCK).mapColor(net.minecraft.world.level.material.MapColor.COLOR_BLUE)')
replace('content/MagicDreams.java',' public static final DeferredBlock<DreamBedBlock> BED=', ' public static final DeferredHolder<Feature<?>,MidnightTreeFeature> NATURAL_TREE=FEATURES.register("natural_midnight_tree",()->new MidnightTreeFeature(true));\n public static final DeferredBlock<DreamBedBlock> BED=')
replace('content/MagicGrove.java',' public static final DeferredBlock<SaplingBlock> SAPLING=', ''' public static final DeferredBlock<LeavesBlock> NATURAL_LEAVES=block("natural_midnight_leaves",()->new LeavesBlock(copy(Blocks.OAK_LEAVES)));
 public static final DeferredBlock<LeavesBlock> NATURAL_GOLDEN_LEAVES=block("natural_golden_midnight_leaves",()->new LeavesBlock(copy(Blocks.OAK_LEAVES).lightLevel(s->9)));
 public static final DeferredBlock<SaplingBlock> SAPLING=''')
replace('content/MidnightTreeFeature.java',' public MidnightTreeFeature(){super(NoneFeatureConfiguration.CODEC);}', ' private final boolean natural;\n public MidnightTreeFeature(){this(false);}\n public MidnightTreeFeature(boolean natural){super(NoneFeatureConfiguration.CODEC);this.natural=natural;}')
f=p/'content/MidnightTreeFeature.java';s=f.read_text();a=s.index('  for(int y=0;');b=s.index('  for(var p:logs)if(!safe',a)
s=s[:a]+'''  // Origin is the centre of the 3x3 trunk. Four centred, two-high buttresses.
  for(int y=0;y<height;y++)for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)logs.add(origin.offset(x,y,z));
  for(var d:Direction.Plane.HORIZONTAL)for(int y=0;y<2;y++)logs.add(origin.relative(d,2).above(y));
  int len=5+random.nextInt(3),branchY=height-10;
  for(var d:Direction.Plane.HORIZONTAL){BlockPos tip=origin.above(branchY);
   for(int n=1;n<=len;n++){tip=origin.above(branchY+(n*n)/(len+1)).relative(d,n);logs.add(tip);logs.add(tip.above());}
   crown(leaves,tip.above(2),4,3);
  }
  crown(leaves,origin.above(height-1),5,4);
  leaves.removeAll(logs);
  for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){var below=origin.offset(x,-1,z);if(!level.getBlockState(below).isFaceSturdy(level,below,Direction.UP))return false;}
  for(var d:Direction.Plane.HORIZONTAL){var below=origin.relative(d,2).below();if(!level.getBlockState(below).isFaceSturdy(level,below,Direction.UP))return false;}
'''+s[b:]
s=s.replace('var block=random.nextInt(100)<5?MagicGrove.GOLDEN_LEAVES.get():MagicGrove.LEAVES.get();','var block=natural?(random.nextInt(100)<15?MagicGrove.NATURAL_GOLDEN_LEAVES.get():MagicGrove.NATURAL_LEAVES.get()):(random.nextInt(100)<5?MagicGrove.GOLDEN_LEAVES.get():MagicGrove.LEAVES.get());');f.write_text(s)
replace('client/ResearchContentClient.java','MagicGrove.GOLDEN_LEAVES,MagicGrove.DOOR','MagicGrove.GOLDEN_LEAVES,MagicGrove.NATURAL_LEAVES,MagicGrove.NATURAL_GOLDEN_LEAVES,MagicGrove.DOOR')
replace('core/GrandSpells.java','r("end_gate",20,"dor rin sin lok neb sap lun tom kan tem sav pen log kel tuk bal per liv mor ten"));','''r("end_gate",20,"dor rin sin lok neb sap lun tom kan tem sav pen log kel tuk bal per liv mor ten"),
 r("threshold_link",40,String.join(" ",Collections.nCopies(2,"dor rin sin lok neb sap lun tom kan tem sav pen log kel tuk bal per liv mor ten"))),
 r("grove_awakening",70,String.join(" ",Collections.nCopies(4,"ter gor nuk neb nok rok sel tur del gel lel mel tel lok kan liv tem sav"))));''')
f=p/'server/SpellEffects.java';s=f.read_text();a='if(!admin&&(rite==null||!dev.kelmagic.research.ResearchCatalog.learned(dev.kelmagic.research.Research.state(p),"rite:"+rite.id())))';b='if(!admin&&((rite!=null&&(rite.id().equals("threshold_link")||rite.id().equals("grove_awakening")))?!p.isCreative():(rite==null||!dev.kelmagic.research.ResearchCatalog.learned(dev.kelmagic.research.Research.state(p),"rite:"+rite.id()))))';assert a in s;s=s.replace(a,b);f.write_text(s)
replace('server/GrandEffects.java',' case "end_gate"->',' case "threshold_link"->ThresholdLinks.open(p);\n case "grove_awakening"->GroveAwakening.start(p);\n case "end_gate"->')
replace('server/GrandEffects.java','if(ok)MagicProgress.award(p,"rite_"+id);','if(ok&&!id.equals("grove_awakening"))MagicProgress.award(p,"rite_"+id);')
q=Path('gradle.properties');q.write_text(q.read_text().replace('1.31.0-r10-dreams-TEST','1.32.0-r11-grove-TEST'))
