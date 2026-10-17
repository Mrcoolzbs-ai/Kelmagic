package dev.kelmagic.speech;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import dev.kelmagic.speech.MouthTimeline.*;
/** Render report data from the actual provider/controller, not from a hand-authored mock sequence. */
public final class TimingReport {
 public static void main(String[] args)throws Exception {
  Path dir=Path.of("build/reports/f3");Files.createDirectories(dir);
  var source=new TextVisemeSource(TextVisemeSource.Language.KEL);String text="mina rap";var track=source.plan(text);
  var cues=new StringBuilder("text,start_ms,end_ms,viseme\n");
  for(var c:track.cues())cues.append(text).append(',').append(c.startMillis()).append(',').append(c.endMillis()).append(',').append(c.viseme().assetId()).append('\n');
  Files.writeString(dir.resolve("kel-text-cues.csv"),cues,StandardCharsets.UTF_8);
  var frames=new StringBuilder("time_ms,layer,asset,base,speaking\n");
  var timeline=new MouthTimeline();timeline.selectEmotion(Emotion.SMILE);
  sample(frames,timeline,0);timeline.startUtterance(10,text,source);
  for(var cue:track.cues()){
   long at=cue.startMillis()+10;if(at>=track.durationMillis()/2)timeline.selectEmotion(Emotion.LAUGH);sample(frames,timeline,at);
  }
  sample(frames,timeline,track.durationMillis()+10);
  Files.writeString(dir.resolve("kel-controller-frames.csv"),frames,StandardCharsets.UTF_8);
  System.out.println("F3 timing report: actual text provider/controller; "+track.durationMillis()+"ms; "+track.cues().size()+" cues");
 }
 static void sample(StringBuilder out,MouthTimeline t,long now){var f=t.frame(now);out.append(now).append(',').append(f.layer()).append(',').append(f.assetId()).append(',').append(f.baseEmotion().assetId()).append(',').append(f.speaking()).append('\n');}
}
