package dev.kelmagic.core;
import java.util.*;
public final class SpellSynergies {
 public record Synergy(String id,String name,String description){}
 public static final Map<String,Synergy> ALL=load();
 private static Map<String,Synergy> load(){var out=new LinkedHashMap<String,Synergy>();for(var e:KelLexicon.resource("synergies").getAsJsonArray("synergies")){var o=e.getAsJsonObject();var words=new ArrayList<String>();for(var w:o.getAsJsonArray("words"))words.add(w.getAsString());if(out.put(SpellParser.key(words),new Synergy(o.get("id").getAsString(),o.get("name").getAsString(),o.get("description").getAsString()))!=null)throw new IllegalStateException("duplicate synergy");}return Collections.unmodifiableMap(out);}
 public static Synergy find(List<String> base){var actions=base.stream().filter(w->!Set.of("mina","tuna","sila","lu","no").contains(w)).toList();return ALL.get(SpellParser.key(actions));}
 private SpellSynergies(){}
}
