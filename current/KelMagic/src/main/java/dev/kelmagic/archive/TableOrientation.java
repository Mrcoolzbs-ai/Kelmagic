package dev.kelmagic.archive;
import net.minecraft.core.BlockPos;import net.minecraft.core.Direction;import net.minecraft.world.level.block.state.BlockState;import net.minecraft.world.phys.Vec3;
/** Same four upright yaw rotations as blockstate JSON, shared by book rendering, picking and server. */
public final class TableOrientation {
 public static Vec3 rotate(Direction d,Vec3 v){double x=v.x,y=v.y,z=v.z;return switch(d){case EAST->new Vec3(-z,y,x);case SOUTH->new Vec3(-x,y,-z);case WEST->new Vec3(z,y,-x);default->v;};}
 public static Vec3 localToBlock(BlockState state,Vec3 p){return rotate((state.hasProperty(ArchiveTableBlock.FACING)?state.getValue(ArchiveTableBlock.FACING):Direction.NORTH),p.subtract(.5,.5,.5)).add(.5,.5,.5);}
 public static Vec3 blockToLocal(BlockState state,Vec3 p){var d=(state.hasProperty(ArchiveTableBlock.FACING)?state.getValue(ArchiveTableBlock.FACING):Direction.NORTH);var v=p.subtract(.5,.5,.5);return new Vec3(v.dot(rotate(d,new Vec3(1,0,0))),v.dot(rotate(d,new Vec3(0,1,0))),v.dot(rotate(d,new Vec3(0,0,1)))).add(.5,.5,.5);}
 public static Vec3 bookCenter(BlockPos pos,BlockState state){return localToBlock(state,new Vec3(.5,.79,.56)).add(pos.getX(),pos.getY(),pos.getZ());}
 private TableOrientation(){}
}
