package dev.kelmagic.client.face;
import dev.kelmagic.entity.RedMage;
import dev.kelmagic.MageClientConfig;
import net.minecraft.resources.ResourceLocation;
import java.util.WeakHashMap;
/** Presentation only: explicit mouth state independent of breath, eyes, cheeks, speech timers and moods. */
public final class MouthStates {
 public enum State { NEUTRAL,SMILE,SPEECH,SURPRISED;
  public ResourceLocation texture(){return ResourceLocation.fromNamespaceAndPath("kelmagic","textures/entity/mouth_e/"+name().toLowerCase(java.util.Locale.ROOT)+".png");}
 }
 private static final class Entry {State state=State.NEUTRAL;Boolean breathingOverride;}
 private static final WeakHashMap<RedMage,Entry> STATES=new WeakHashMap<>();
 private static Entry entry(RedMage e){return STATES.computeIfAbsent(e,key->new Entry());}
 public static State state(RedMage e){return entry(e).state;}
 public static void select(RedMage e,State state){entry(e).state=java.util.Objects.requireNonNull(state);}
 public static boolean breathingEnabled(RedMage e){var v=entry(e).breathingOverride;return v==null?MageClientConfig.BREATHING.get():v;}
 /** QA override only; null returns to the client cosmetic setting. No game commands/mechanics. */
 public static void breathingOverride(RedMage e,Boolean value){entry(e).breathingOverride=value;}
 public static float noseOffset(float time,boolean enabled){return BreathingPresentation.noseY(RedMage.breathing(time),enabled);}
 public static void apply(software.bernie.geckolib.model.GeoModel<?> model,RedMage e,float partial){
  for(String n:new String[]{"mouth","lower_lip"})model.getBone(n).ifPresent(b->{b.setHidden(true);b.setChildrenHidden(true);});
  model.getBone("mouth_png").ifPresent(b->{b.setHidden(e.kitten());b.setChildrenHidden(e.kitten());b.setScaleX(1);b.setScaleY(1);b.setScaleZ(1);});
  model.getBone("nose_lower").ifPresent(b->{b.setPosY(b.getInitialSnapshot().getOffsetY()+noseOffset(e.tickCount+partial,breathingEnabled(e)));});
 }
 private MouthStates(){}
}
