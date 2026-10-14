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
 public static String describe(String word){
  var profile=ALL.get(word);if(profile!=null)return profile.description();
  return switch(word){case "mina"->"Цель: заклинатель.";case "tuna"->"Цель: другой игрок.";case "sila"->"Цель: моб.";case "pir"->"Магический огонь. mina liv pir: жизнь в пламени.";case "muk"->"Каждый повтор: сила и время ×1,5.";case "nis"->"Каждый повтор: сила и время ÷1,5.";case "no"->"Обращает поддерживаемые эффекты.";default->"Множество целей в области.";};
 }
 public static double scale(Collection<String> words){int n=Collections.frequency(new ArrayList<>(words),"muk")-Collections.frequency(new ArrayList<>(words),"nis");return Math.pow(1.5,Math.max(-6,Math.min(6,n)));}
 public static int duration(int base,double scale){return Math.max(1,Math.min(3600,(int)Math.round(base*scale)));}
 private SpellProfiles(){}
}
