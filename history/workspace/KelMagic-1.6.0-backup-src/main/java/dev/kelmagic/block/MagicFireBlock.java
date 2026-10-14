package dev.kelmagic.block;
import com.mojang.serialization.MapCodec;
import dev.kelmagic.*;
import dev.kelmagic.server.ArcaneFire;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
/** Vanilla fire geometry/sounds/hooks, with a faster bounded spread and independent magic damage. */
public final class MagicFireBlock extends FireBlock {
 public static final MapCodec<MagicFireBlock> CODEC=simpleCodec(MagicFireBlock::new);
 public static final IntegerProperty SCALE=IntegerProperty.create("scale",0,12);
 public static int scaleIndex(double scale){return Math.max(0,Math.min(12,6+(int)Math.round(Math.log(scale)/Math.log(1.5))));}
 public static final IntegerProperty PULSE=IntegerProperty.create("pulse",0,3);
 public MagicFireBlock(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(PULSE,0).setValue(SCALE,6));}
 @SuppressWarnings({"unchecked","rawtypes"})
 @Override public MapCodec<FireBlock> codec(){return (MapCodec)CODEC;}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(PULSE,SCALE);}
 public BlockState stateAt(BlockGetter l,BlockPos p){
  var below=p.below();var bs=l.getBlockState(below);var s=defaultBlockState();
  if(bs.isFaceSturdy(l,below,Direction.UP) || bs.isFlammable(l,below,Direction.UP))return s;
  return s.setValue(NORTH,flammable(l,p.north(),Direction.SOUTH)).setValue(SOUTH,flammable(l,p.south(),Direction.NORTH)).setValue(EAST,flammable(l,p.east(),Direction.WEST)).setValue(WEST,flammable(l,p.west(),Direction.EAST)).setValue(UP,flammable(l,p.above(),Direction.DOWN));
 }
 private boolean flammable(BlockGetter l,BlockPos p,Direction f){return l.getBlockState(p).isFlammable(l,p,f);}
 @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return stateAt(c.getLevel(),c.getClickedPos());}
 @Override protected BlockState getStateForPlacement(BlockGetter l,BlockPos p){return stateAt(l,p);}
 @Override protected BlockState updateShape(BlockState s,Direction d,BlockState neighbor,LevelAccessor l,BlockPos p,BlockPos other){return canSurvive(s,l,p)?stateAt(l,p).setValue(AGE,s.getValue(AGE)).setValue(PULSE,s.getValue(PULSE)).setValue(SCALE,s.getValue(SCALE)):Blocks.AIR.defaultBlockState();}
 public static final int BLOCK_BURN_TICKS=100;
 private static int lifetime(BlockState s){return dev.kelmagic.core.SpellProfiles.duration(BLOCK_BURN_TICKS,Math.pow(1.5,s.getValue(SCALE)-6));}
 private static int delay(BlockState s){int total=lifetime(s),stages=Math.min(64,total),step=s.getValue(AGE)*4+s.getValue(PULSE);return Math.max(1,total/stages+(step<total%stages?1:0));}
 @Override protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){if(!old.is(this)){l.scheduleTick(p,this,delay(s));}}
 public static boolean canOpenPortal(Level l,BlockPos p){
  return (l.dimension()==Level.OVERWORLD || l.dimension()==Level.NETHER) && net.minecraft.world.level.portal.PortalShape.findEmptyPortalShape(l,p,Direction.Axis.X).isPresent();
 }
 public static boolean tryOpenPortal(Level l,BlockPos p){
  if(l.isClientSide || (l.dimension()!=Level.OVERWORLD && l.dimension()!=Level.NETHER))return false;
  var shape=net.minecraft.world.level.portal.PortalShape.findEmptyPortalShape(l,p,Direction.Axis.X);
  shape=net.neoforged.neoforge.event.EventHooks.onTrySpawnPortal(l,p,shape);
  if(shape.isEmpty())return false;
  shape.get().createPortalBlocks();return true;
 }
 public static boolean lightCampfire(Level l,BlockPos p){var s=l.getBlockState(p);if(CampfireBlock.canLight(s)){l.setBlock(p,s.setValue(CampfireBlock.LIT,true),3);l.playSound(null,p,net.minecraft.sounds.SoundEvents.FLINTANDSTEEL_USE,net.minecraft.sounds.SoundSource.BLOCKS,1,1);return true;}return false;}
 @Override protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){
  // Defer portal creation until after the placement protection hook can cancel.
  if(tryOpenPortal(l,p))return;
  int step=s.getValue(AGE)*4+s.getValue(PULSE);
  if(!l.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)){l.scheduleTick(p,this,delay(s));return;}
  if(!canSurvive(s,l,p)){l.removeBlock(p,false);return;}
  boolean eternal=l.getBlockState(p.below()).isFireSource(l,p.below(),Direction.UP);
  if(!eternal && l.isRainingAt(p)){l.removeBlock(p,false);return;}
  // Six neighboring blocks only; fire spread inherits the scale and never loads new chunks.
  for(Direction d:Direction.values()){
   BlockPos q=p.relative(d);if(!l.hasChunkAt(q)||!l.getWorldBorder().isWithinBounds(q))continue;
   if(lightCampfire(l,q))continue;
   var target=l.getBlockState(q);int odds=target.getFlammability(l,q,d.getOpposite());
   if(odds>0 && r.nextInt(70)<odds){
    target.onCaughtFire(l,q,d.getOpposite(),null);l.removeBlock(q,false);
    var next=stateAt(l,q).setValue(SCALE,s.getValue(SCALE));if(next.canSurvive(l,q) && !l.isRainingAt(q))l.setBlock(q,next,3);
   }
  }
  // Bounded local spread, preserving purple fire; honors modded flammability and doFireTick.
  for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)for(int dy=0;dy<=1;dy++){
   if(dx==0&&dz==0&&dy==0)continue;BlockPos q=p.offset(dx,dy,dz);
   if(!l.hasChunkAt(q)||!l.getWorldBorder().isWithinBounds(q)||!l.isEmptyBlock(q)||l.isRainingAt(q))continue;
   int odds=0;for(Direction d:Direction.values())odds=Math.max(odds,l.getBlockState(q.relative(d)).getFireSpreadSpeed(l,q.relative(d),d.getOpposite()));
   var next=stateAt(l,q).setValue(SCALE,s.getValue(SCALE));if(odds>0 && r.nextInt(100)<odds && next.canSurvive(l,q))l.setBlock(q,next,3);
  }
  // Neighbor burns may remove the current block's support.
  if(!l.getBlockState(p).is(this))return;
  if(!eternal && step>=Math.min(64,lifetime(s))-1){l.removeBlock(p,false);return;}
  step=eternal?0:step+1;var aged=stateAt(l,p).setValue(AGE,step/4).setValue(PULSE,step%4).setValue(SCALE,s.getValue(SCALE));l.setBlock(p,aged,2);l.scheduleTick(p,this,delay(aged));
 }
 @Override protected void entityInside(BlockState s,Level l,BlockPos p,Entity e){
  if(l.isClientSide)return;
  if(e instanceof LivingEntity live){double scale=Math.pow(1.5,s.getValue(SCALE)-6);ArcaneFire.ignite(live,dev.kelmagic.core.SpellProfiles.duration(Config.FIRE_SECONDS.get()*60,scale),(float)scale);}
  else if(e instanceof ItemEntity item && !item.fireImmune())item.hurt(l.damageSources().inFire(),2);
 }
 // Inherits vanilla fire ambience and smoke. No following dust particles on entities.
}
