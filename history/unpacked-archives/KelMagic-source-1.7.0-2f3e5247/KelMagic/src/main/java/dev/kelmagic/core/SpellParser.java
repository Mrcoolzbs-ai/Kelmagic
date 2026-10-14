package dev.kelmagic.core;

import com.google.gson.*;
import java.util.*;

/** Multiset parser: sorting preserves duplicates; permutations cannot change the result. */
public final class SpellParser {
    public enum Effect { FIRE_BLOCK, FIRE_PLAYER, FIRE_MOB, FIRE_SELF, RESIST_SELF, FIRE_AREA,
        FIRE_PLAYERS, FIRE_MOBS, SELECTION_ONLY, FLAME_LIFE, SYNERGY, COMPOSITE, UNDEFINED }
    private static final Map<String, Effect> CATALOG = load();
    public static String key(Collection<String> words) {
        return String.join(" ", words.stream().sorted().toList());
    }
    public record Plan(Effect effect,List<String> actions,String selector,boolean area,boolean negate,double scale){}
    private static Plan invalid(){return new Plan(Effect.UNDEFINED,List.of(),"",false,false,1);}
    public static Effect parse(Collection<String> words){return plan(words).effect();}
    public static Plan plan(Collection<String> words){
        if(words.isEmpty() || words.size()>SpellSession.LIMIT || words.stream().anyMatch(w->!KelLexicon.WORDS.containsKey(w)))return invalid();
        double scale=SpellProfiles.scale(words);var base=words.stream().filter(w->!w.equals("muk")&&!w.equals("nis")).sorted().toList();
        if(base.isEmpty())return new Plan(Effect.SELECTION_ONLY,List.of(),"",false,false,scale);
        if(new HashSet<>(base).size()!=base.size())return invalid(); // Only the two scale operators may repeat.
        long selectors=base.stream().filter(w->Set.of("mina","tuna","sila").contains(w)).count();if(selectors>1)return invalid();
        String selector=base.stream().filter(w->Set.of("mina","tuna","sila").contains(w)).findFirst().orElse("");
        boolean area=base.contains("lu"),negate=base.contains("no");
        if(key(base).equals("liv mina pir"))return new Plan(Effect.FLAME_LIFE,List.of(),"mina",false,false,scale);
        var sy=SpellSynergies.find(base);if(sy!=null && (!selector.isEmpty() && !selector.equals("mina") || negate || area && Set.of("guardian","spring","magnet").contains(sy.id())))return invalid();if(sy!=null)return new Plan(Effect.SYNERGY,List.of(sy.id()),selector,area,negate,scale);
        var legacy=CATALOG.get(key(base));
        if(legacy!=null)return new Plan(legacy,base.contains("pir")?List.of("pir"):List.of(),selector,area,negate,scale);
        // Preserve unsupported historical self plurality rather than silently ignoring it.
        if(base.equals(List.of("lu","mina","pir")))return invalid();
        var actions=base.stream().filter(w->!Set.of("mina","tuna","sila","lu","no").contains(w)).toList();
        if(actions.isEmpty())return new Plan(Effect.SELECTION_ONLY,List.of(),selector,area,negate,scale);
        if(actions.size()>8 || actions.stream().anyMatch(w->!w.equals("pir")&&!SpellProfiles.ALL.containsKey(w)))return invalid();
        return new Plan(Effect.COMPOSITE,actions,selector,area,negate,scale);
    }
    private static Map<String, Effect> load() {
        Map<String, Effect> catalog = new LinkedHashMap<>();
        for (JsonElement element : KelLexicon.resource("spells").getAsJsonArray("spells")) {
            JsonObject j = element.getAsJsonObject();
            List<String> words = new ArrayList<>();
            for (JsonElement word : j.getAsJsonArray("words")) words.add(word.getAsString());
            if (words.isEmpty() || words.stream().anyMatch(w -> !KelLexicon.WORDS.containsKey(w))) throw new IllegalStateException("Unverified spell word");
            if (catalog.put(key(words), Effect.valueOf(j.get("effect").getAsString())) != null) throw new IllegalStateException("Duplicate spell");
        }
        return Map.copyOf(catalog);
    }
    public static int declaredCombinations() { return CATALOG.size(); }
    private SpellParser() {}
}
