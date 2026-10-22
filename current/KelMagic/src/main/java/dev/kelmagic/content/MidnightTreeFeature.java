package dev.kelmagic.content;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** A rooted, branching crown, not a reskinned jungle tree. Plans first, writes only on success. */
public final class MidnightTreeFeature extends Feature<NoneFeatureConfiguration> {
 private final boolean natural;
 public MidnightTreeFeature(){this(false);}
 public MidnightTreeFeature(boolean natural){super(NoneFeatureConfiguration.CODEC);this.natural=natural;}
 @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){
  var level=c.level();var random=c.random();var origin=c.origin();int height=20+random.nextInt(5);
  var logs=new LinkedHashSet<BlockPos>();var leaves=new LinkedHashSet<BlockPos>();
  // Origin is the centre of the 3x3 trunk. Four centred, two-high buttresses.
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
  for(var p:logs){if(!safe(level,p))return false;var state=level.getBlockState(p);if(natural&&!state.isAir()&&!state.is(BlockTags.LEAVES)&&!state.is(MagicGrove.SAPLING.get()))return false;}
  for(var p:leaves)if(!safe(level,p))return false;
  // Vanilla leaf-distance semantics: never mark natural leaves persistent and never leave unreachable foliage.
  var distance=new HashMap<BlockPos,Integer>();var queue=new ArrayDeque<BlockPos>();for(var p:logs){distance.put(p,0);queue.add(p);}
  while(!queue.isEmpty()){var p=queue.remove();int n=distance.get(p)+1;if(n>6)continue;for(var d:Direction.values()){var q=p.relative(d);if(leaves.contains(q)&&!distance.containsKey(q)){distance.put(q,n);queue.add(q);}}}
  for(var p:logs)level.setBlock(p,MagicGrove.LOG.get().defaultBlockState(),3);
  for(var p:leaves){var dist=distance.get(p);if(dist==null)continue;var block=natural?(random.nextInt(100)<15?MagicGrove.NATURAL_GOLDEN_LEAVES.get():MagicGrove.NATURAL_LEAVES.get()):(random.nextInt(100)<5?MagicGrove.GOLDEN_LEAVES.get():MagicGrove.LEAVES.get());level.setBlock(p,block.defaultBlockState().setValue(LeavesBlock.DISTANCE,dist),3);}
  return true;
 }
 private static boolean safe(WorldGenLevel l,BlockPos p){if(l.isOutsideBuildHeight(p)||!l.hasChunk(p.getX()>>4,p.getZ()>>4))return false;var s=l.getBlockState(p);return s.isAir()||s.is(BlockTags.LEAVES)||s.canBeReplaced()||s.is(MagicGrove.SAPLING.get());}
 private static void crown(Set<BlockPos> leaves,BlockPos center,int r,int h){for(int x=-r;x<=r;x++)for(int y=-h;y<=h;y++)for(int z=-r;z<=r;z++)if((x*x+z*z)/(double)(r*r)+y*y/(double)(h*h)<=1.08)leaves.add(center.offset(x,y,z));}
}
