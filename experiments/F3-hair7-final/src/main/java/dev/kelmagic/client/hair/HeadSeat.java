package dev.kelmagic.client.hair;
/** Native per-draw placement, not scaling. Every face/hat/scalp child inherits the same offset. */
public final class HeadSeat {
 public static final float DROP_PIXELS = 1.2080000f;
 public static void apply(software.bernie.geckolib.cache.object.GeoBone head){
  head.setPosY(head.getInitialSnapshot().getOffsetY()-DROP_PIXELS);
 }
 private HeadSeat(){}
}
