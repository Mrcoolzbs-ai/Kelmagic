package dev.kelmagic.speech;
import java.util.*;
import dev.kelmagic.speech.MouthTimeline.*;
public final class TextVisemeTests {
 static int checks;
 static void ok(boolean c,String m){checks++;if(!c)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception {
  var k=new TextVisemeSource(TextVisemeSource.Language.KEL);
  ok(k.plan(null).equals(Track.empty())&&k.plan("  ").equals(Track.empty()),"empty/null");
  ok(k.segments("kel").get(1).phoneme().equals("e"),"Kel e remains its own phoneme");
  ok(k.plan("ā").durationMillis()==2*k.plan("a").durationMillis(),"macron long vowel");
  ok(k.plan("a\u0304").equals(k.plan("ā")),"Unicode NFC/NFD equivalence");
  ok(k.segments("kea").stream().map(TextVisemeSource.Segment::phoneme).toList().equals(List.of("k","e","a")),"hiatus not diphthong");
  ok(k.segments("aktter").stream().filter(s->s.phoneme().equals("t")).count()==2,"geminate preserved");
  ok(k.plan("tt").cues().size()==1&&k.plan("tt").durationMillis()==130,"coalescing retains geminate duration");
  ok(k.segments("kurmuk").stream().map(TextVisemeSource.Segment::phoneme).toList().equals(List.of("k","u","r","m","u","k")),"cluster not dropped or English digraph");
  ok(k.segments("j").getFirst().viseme()==Viseme.SPREAD_IE,"Kel j is approximant not English affricate");
  ok(k.plan("mina. rap").durationMillis()>k.plan("mina rap").durationMillis(),"sentence pause longer than word pause");
  ok(k.segments("ei").size()==2&&k.plan("ei").durationMillis()==290&&k.plan("ei").cues().size()==1,"separate vowel nuclei retain duration, no invented intraword pause");
  ok(k.segments("palvakman").stream().noneMatch(s->s.phoneme().equals("pause")),"no pause inside compound root");
  ok(k.plan("a!").equals(k.plan("A!")),"case independence");
  var r=new TextVisemeSource(TextVisemeSource.Language.RUSSIAN);
  ok(r.segments("яма").getFirst().phoneme().equals("j"),"Russian initial iotation approximation");
  var latin=new TextVisemeSource(TextVisemeSource.Language.LATIN_FALLBACK);
  ok(latin.segments("ship").getFirst().phoneme().equals("sh"),"Latin fallback groups known digraph");
  ok(k.segments("sh").size()==2,"Kel never inherits English sh rule");
  String json=new String(TextVisemeTests.class.getResourceAsStream("/data/kelmagic/magic/words.json").readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
  var wordsMatcher=java.util.regex.Pattern.compile("\"word\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
  int words=0;
  while(wordsMatcher.find()) {
   String word=wordsMatcher.group(1);var t=k.plan(word);
   ok(t.equals(k.plan(word)),"deterministic lexicon "+word);ok(t.durationMillis()>0,"nonempty lexicon "+word);
   var m=new MouthTimeline();m.selectEmotion(Emotion.SMIRK);m.start(0,t);
   ok(m.frame(t.durationMillis()).baseEmotion()==Emotion.SMIRK&&!m.hasPendingTrack(),"lexicon finish "+word);words++;
  }
  ok(words==180,"complete built-in 180-word fixture, not an empty/partial scan");
  ok(r.segments("ё").getFirst().viseme()==Viseme.ROUND_O,"Russian yo not lost by Unicode normalization");
  System.out.println("TextVisemeTests: "+checks+" assertions passed, "+words+" built-in words; TEXT APPROXIMATION ONLY");
 }
}
