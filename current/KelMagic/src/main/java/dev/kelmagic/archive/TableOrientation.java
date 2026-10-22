package dev.kelmagic.archive;
import net.minecraft.core.BlockPos;import net.minecraft.core.Direction;import net.minecraft.world.level.block.state.BlockState;import net.minecraft.world.phys.Vec3;
/** Shared sloped-page transform and four blockstate yaw rotations for rendering, picking and server. */
public final class TableOrientation {
 public static final float BOOK_TILT=22.5f;
 public static final double COS=Math.cos(Math.toRadians(BOOK_TILT)),SIN=Math.sin(Math.toRadians(BOOK_TILT));
 public static final double BOOK_Y=.78125+.048*COS,BOOK_Z=.5+.048*SIN;
 public static Vec3 pageToLocal(Vec3 p){return new Vec3(.5+p.x,BOOK_Y+COS*p.y-SIN*p.z,BOOK_Z+SIN*p.y+COS*p.z);}
 public static Vec3 localToPage(Vec3 p){double y=p.y-BOOK_Y,z=p.z-BOOK_Z;return new Vec3(p.x-.5,COS*y+SIN*z,-SIN*y+COS*z);}
 public static Vec3 rotate(Direction d,Vec3 v){double x=v.x,y=v.y,z=v.z;return switch(d){case EAST->new Vec3(-z,y,x);case SOUTH->new Vec3(-x,y,-z);case WEST->new Vec3(z,y,-x);default->v;};}
 public static Vec3 localToBlock(BlockState state,Vec3 p){return rotate((state.hasProperty(ArchiveTableBlock.FACING)?state.getValue(ArchiveTableBlock.FACING):Direction.NORTH),p.subtract(.5,.5,.5)).add(.5,.5,.5);}
 public static Vec3 blockToLocal(BlockState state,Vec3 p){var d=(state.hasProperty(ArchiveTableBlock.FACING)?state.getValue(ArchiveTableBlock.FACING):Direction.NORTH);var v=p.subtract(.5,.5,.5);return new Vec3(v.dot(rotate(d,new Vec3(1,0,0))),v.dot(rotate(d,new Vec3(0,1,0))),v.dot(rotate(d,new Vec3(0,0,1)))).add(.5,.5,.5);}
 public static Vec3 bookCenter(BlockPos pos,BlockState state){return localToBlock(state,pageToLocal(Vec3.ZERO)).add(pos.getX(),pos.getY(),pos.getZ());}
 private TableOrientation(){}
}
