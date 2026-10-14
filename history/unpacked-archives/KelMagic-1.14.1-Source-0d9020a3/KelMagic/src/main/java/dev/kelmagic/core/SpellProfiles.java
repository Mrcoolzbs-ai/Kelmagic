package dev.kelmagic.core;
import java.util.*;
import com.google.gson.*;
/** Gameplay interpretations are data, separate from canonical translations. */
public final class SpellProfiles {
 public record Profile(String action,String status,String description,boolean harmful,int duration,double power){}
 public static final Map<String,Profile> ALL=load();
 private static Map<String,Profile> load(){
  Map<String,Profile> out=new LinkedHashMap<>();
  for(var entry:KelLexicon.resource("effects").getAsJsonObject("effects").entrySet()){
   var j=entry.getValue().getAsJsonObject();if(!KelLexicon.WORDS.containsKey(entry.getKey()))throw new IllegalStateException("Unverified profile");
   out.put(entry.getKey(),new Profile(j.get("action").getAsString(),j.get("status").getAsString(),j.get("description").getAsString(),j.get("harmful").getAsBoolean(),j.get("baseDuration").getAsInt(),j.get("basePower").getAsDouble()));
  }return Collections.unmodifiableMap(out);
 }
 private static final Map<String,String> LORE=descriptions("words","words",true);
 private static final Map<String,String> MECHANICS=descriptions("effects","effects",false);
 private static Map<String,String> descriptions(String file,String key,boolean array){var out=new LinkedHashMap<String,String>();var root=KelLexicon.resource(file);if(array){for(var e:root.getAsJsonArray(key)){var j=e.getAsJsonObject();out.put(j.get("word").getAsString(),j.get("description").getAsString());}}else for(var e:root.getAsJsonObject(key).entrySet()){var j=e.getValue().getAsJsonObject();out.put(e.getKey(),j.has("gameplayDescription")?j.get("gameplayDescription").getAsString():j.get("description").getAsString());}return Collections.unmodifiableMap(out);}
 public static String describe(String word){return LORE.getOrDefault(word,"");}
 public static String mechanics(String word){
  var practical=MECHANICS.get(word);if(practical!=null)return practical;
  return switch(word){case "mina"->"Цель: заклинатель.";case "tuna"->"Цель: другой игрок.";case "sila"->"Цель: моб.";case "pir"->"Магический огонь. mina liv pir: жизнь в пламени.";case "muk"->"Каждый повтор: сила и время ×1,5.";case "nis"->"Каждый повтор: сила и время ÷1,5.";case "no"->"Обращает поддерживаемые эффекты.";default->"Множество целей в области.";};
 }
 public static double scale(Collection<String> words){int n=Collections.frequency(new ArrayList<>(words),"muk")-Collections.frequency(new ArrayList<>(words),"nis");return Math.pow(1.5,Math.max(-6,Math.min(6,n)));}
 public static int duration(int base,double scale){return Math.max(1,Math.min(3600,(int)Math.round(base*scale)));}
 private SpellProfiles(){}
}
