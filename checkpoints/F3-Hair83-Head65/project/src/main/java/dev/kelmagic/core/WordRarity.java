package dev.kelmagic.core;
import java.util.*;
import net.minecraft.world.item.Rarity;
public final class WordRarity {
 public static final Map<String,Rarity> ALL=load();
 private static Map<String,Rarity> load(){var out=new LinkedHashMap<String,Rarity>();for(var e:KelLexicon.resource("rarities").getAsJsonObject("words").entrySet())out.put(e.getKey(),Rarity.valueOf(e.getValue().getAsString().toUpperCase(Locale.ROOT)));if(out.size()!=180)throw new IllegalStateException("Every word needs rarity");return Collections.unmodifiableMap(out);}
 public static Rarity of(String word){return ALL.getOrDefault(word,Rarity.COMMON);}
 public static String label(String w){return switch(of(w)){case COMMON->"Обычное";case UNCOMMON->"Необычное";case RARE->"Редкое";case EPIC->"Эпическое";};}
 private WordRarity(){}
}