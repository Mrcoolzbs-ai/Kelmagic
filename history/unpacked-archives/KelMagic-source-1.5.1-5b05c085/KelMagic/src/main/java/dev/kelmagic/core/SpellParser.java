package dev.kelmagic.core;

import com.google.gson.*;
import java.util.*;

/** Multiset parser: sorting preserves duplicates; permutations cannot change the result. */
public final class SpellParser {
    public enum Effect { FIRE_BLOCK, FIRE_PLAYER, FIRE_MOB, FIRE_SELF, RESIST_SELF, FIRE_AREA,
        FIRE_PLAYERS, FIRE_MOBS, SELECTION_ONLY, UNDEFINED }
    private static final Map<String, Effect> CATALOG = load();
    public static String key(Collection<String> words) {
        return String.join(" ", words.stream().sorted().toList());
    }
    public static Effect parse(Collection<String> words) {
        if (words.size() > SpellSession.LIMIT || words.stream().anyMatch(w -> !KelLexicon.WORDS.containsKey(w))) return Effect.UNDEFINED;
        return CATALOG.getOrDefault(key(words), Effect.UNDEFINED);
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
