package dev.kelmagic.core;
import java.util.*;
/** Total unordered pair grammar: all 180*181/2 pairs, including deliberate double-word resonance. */
public final class PairRules {
 public static final int TOTAL=180*181/2;
 private static final Set<String> ACTORS=Set.of("mina","tuna","sila");
 public static SpellParser.Plan plan(List<String> words){
  var sorted=words.stream().sorted().toList();String a=sorted.get(0),b=sorted.get(1);double scale=SpellProfiles.scale(words);
  if(a.equals("rak")&&b.equals("rap"))return special(sorted,scale);
  if(a.equals(b))return special(sorted,scale);
  var ordinary=SpellParser.corePlan(words);
  if(ordinary.effect()!=SpellParser.Effect.UNDEFINED&&ordinary.effect()!=SpellParser.Effect.SELECTION_ONLY){
   if(ordinary.negate()&&!invertible(ordinary.actions()))return special(sorted,scale);
   return ordinary;
  }
  return special(sorted,scale);
 }
 private static SpellParser.Plan special(List<String> w,double s){return new SpellParser.Plan(SpellParser.Effect.PAIR,w,"",false,false,s);}
 public static boolean invertible(List<String> words){for(String word:words){if(word.equals("pir"))continue;var p=SpellProfiles.ALL.get(word);if(p==null)return false;if(!Set.of("BUFF","DAMAGE","HEAL","PUSH","PULL","FEED","VANILLA_FIRE","CLEANSE","OPEN","CLOSE","SCAN","W_FREEZE","W_WATER","W_POOL","W_PUDDLE","W_PLACE","W_CONVERT").contains(p.action()))return false;}return true;}
 public static String description(String a,String b){var p=plan(List.of(a,b));if(a.equals(b))return "Резонанс: усиленное одиночное действие; для чистых операторов — защитный отклик.";if(Set.of(a,b).equals(Set.of("rak","rap")))return "Рывок в точном направлении взгляда.";if(p.effect()==SpellParser.Effect.PAIR)return "Связь селекторов или обратный отклик: защита выбранных целей / заклинателя.";if(p.effect()==SpellParser.Effect.SYNERGY){var s=SpellSynergies.find(List.of(a,b));return s.name()+": "+s.description();}return "Составление действий, выбор цели или модификация слова; требуется подходящая цель.";}
 public static boolean actors(String w){return ACTORS.contains(w);}
 private PairRules(){}
}
