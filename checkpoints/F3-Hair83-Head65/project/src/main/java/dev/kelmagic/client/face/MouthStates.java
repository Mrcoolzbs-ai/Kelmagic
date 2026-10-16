package dev.kelmagic.client.face;
import dev.kelmagic.entity.RedMage;
import dev.kelmagic.MageClientConfig;
import dev.kelmagic.speech.MouthTimeline;
import dev.kelmagic.speech.TextVisemeSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import java.util.WeakHashMap;
/** One PNG resolver; timing, eyelids, cheek geometry and breathing stay independent. */
public final class MouthStates {
 // Historical E QA API stays available as an explicit override, not a second automatic controller.
 public enum State { NEUTRAL,SMILE,SPEECH,SURPRISED;
  public ResourceLocation texture(){return ResourceLocation.fromNamespaceAndPath("kelmagic","textures/entity/mouth_e/"+name().toLowerCase(java.util.Locale.ROOT)+".png");}
 }
 private static final class Entry {
  State legacyOverride;Boolean breathingOverride;
  MouthTimeline.Emotion emotionOverride;
  final MouthTimeline timeline=new MouthTimeline();
  int seenSpeechId=-1;
 }
 private static final WeakHashMap<RedMage,Entry> STATES=new WeakHashMap<>();
 private static Entry entry(RedMage e){return STATES.computeIfAbsent(e,key->new Entry());}
 public static State state(RedMage e){var s=entry(e).legacyOverride;return s==null?State.NEUTRAL:s;}
 public static void select(RedMage e,State state){var v=entry(e);v.legacyOverride=java.util.Objects.requireNonNull(state);v.timeline.cancelAll();}
 public static void selectEmotion(RedMage e,MouthTimeline.Emotion emotion){var v=entry(e);v.legacyOverride=null;v.emotionOverride=java.util.Objects.requireNonNull(emotion);}
 public static void automaticEmotion(RedMage e){var v=entry(e);v.legacyOverride=null;v.emotionOverride=null;}
 public static boolean breathingEnabled(RedMage e){var v=entry(e).breathingOverride;return v==null?MageClientConfig.BREATHING.get():v;}
 public static void breathingOverride(RedMage e,Boolean value){entry(e).breathingOverride=value;}
 public static float noseOffset(float time,boolean enabled){return BreathingPresentation.noseY(RedMage.breathing(time),enabled);}
 public static MouthTimeline.Frame frame(RedMage e,float partial){
  var v=entry(e);
  var base=v.emotionOverride!=null?v.emotionOverride:switch(e.expression()){
   case 1->MouthTimeline.Emotion.LAUGH;
   case 2,3->MouthTimeline.Emotion.DISPLEASED;
   default->MouthTimeline.Emotion.NEUTRAL;
  };
  v.timeline.selectEmotion(base);
  var speech=e.characterSpeech();
  if(v.seenSpeechId!=speech.id()){
   v.seenSpeechId=speech.id();
   String text=speech.text();
   if(speech.kind()==RedMage.SpeechKind.TRANSLATION_KEY){
    String translated=Component.translatable(text).getString();text=translated.equals(text)?"":translated;
   }
   var language=speech.kind()==RedMage.SpeechKind.KEL?TextVisemeSource.Language.KEL:
       text.codePoints().anyMatch(c->Character.UnicodeScript.of(c)==Character.UnicodeScript.CYRILLIC)?
       TextVisemeSource.Language.RUSSIAN:TextVisemeSource.Language.LATIN_FALLBACK;
   v.timeline.startUtterance(Math.max(0,speech.startTick()*50),text,new TextVisemeSource(language));
  }
  if(!e.isAlive()||e.isRemoved()||e.kitten()||v.legacyOverride!=null)v.timeline.cancelAll();
  long now=Math.max(0,(long)((e.level().getGameTime()+Math.clamp(partial,0,1))*50));
  return v.timeline.frame(now);
 }
 public static ResourceLocation texture(RedMage e,float partial){
  var f=frame(e,partial);var v=entry(e);
  return v.legacyOverride!=null?v.legacyOverride.texture():ResourceLocation.fromNamespaceAndPath("kelmagic","textures/entity/mouth_f2/"+f.layer()+"/"+f.assetId()+".png");
 }
 public static void apply(software.bernie.geckolib.model.GeoModel<?> model,RedMage e,float partial){
  for(String n:new String[]{"mouth","lower_lip"})model.getBone(n).ifPresent(b->{b.setHidden(true);b.setChildrenHidden(true);});
  model.getBone("mouth_png").ifPresent(b->{b.setHidden(e.kitten());b.setChildrenHidden(e.kitten());b.setScaleX(1);b.setScaleY(1);b.setScaleZ(1);});
  model.getBone("nose_lower").ifPresent(b->{b.setPosY(b.getInitialSnapshot().getOffsetY()+noseOffset(e.tickCount+partial,breathingEnabled(e)));});
 }
 private MouthStates(){}
}
