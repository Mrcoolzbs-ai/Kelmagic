package dev.kelmagic;
import net.neoforged.neoforge.registries.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.state.BlockBehaviour;import net.minecraft.core.particles.ParticleTypes;
public final class MagicLamps {
 public static final DeferredBlock<TorchBlock> TORCH=KelMagic.BLOCKS.register("magic_torch",()->new TorchBlock(ParticleTypes.END_ROD,BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(s->14).noLootTable().sound(SoundType.WOOD)){
 public void animateTick(net.minecraft.world.level.block.state.BlockState s,net.minecraft.world.level.Level l,net.minecraft.core.BlockPos p,net.minecraft.util.RandomSource r){l.addParticle(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.76f,.60f,.96f),.7f),p.getX()+.5,p.getY()+.7,p.getZ()+.5,0,.005,0);}});
 public static final DeferredBlock<Block> LANTERN=KelMagic.BLOCKS.register("magic_lantern",()->new Block(BlockBehaviour.Properties.of().strength(.5f).lightLevel(s->15).noLootTable().sound(SoundType.LANTERN)){
 public void animateTick(net.minecraft.world.level.block.state.BlockState s,net.minecraft.world.level.Level l,net.minecraft.core.BlockPos p,net.minecraft.util.RandomSource r){if(r.nextInt(4)==0)l.addParticle(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.76f,.60f,.96f),.6f),p.getX()+.5,p.getY()+.8,p.getZ()+.5,0,.01,0);}});
 public static void init(){}private MagicLamps(){}
}