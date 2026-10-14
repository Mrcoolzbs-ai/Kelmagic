package dev.kelmagic.core;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Verified magical subset, not a replacement for the complete language dictionary. */
public final class KelLexicon {
    public record Word(String word, String root, String translation, String category, String source) {}
    public static final Map<String, Word> WORDS = load();
    private static Map<String, Word> load() {
        var result = new LinkedHashMap<String, Word>();
        for (JsonElement element : resource("words").getAsJsonArray("words")) {
            JsonObject j = element.getAsJsonObject();
            String word = j.get("word").getAsString();
            if (!word.matches("[ptkbdgmnslvrjaeiou]{2,8}")) throw new IllegalStateException("Invalid Kel word: " + word);
            Word data = new Word(word, j.get("root").getAsString(), j.get("translation").getAsString(),
                    j.get("category").getAsString(), j.get("source").getAsString());
            if (result.put(word, data) != null) throw new IllegalStateException("Duplicate word " + word);
        }
        return Collections.unmodifiableMap(result);
    }
    static JsonObject resource(String name) {
        String path = "/data/kelmagic/magic/" + name + ".json";
        try (InputStream stream = KelLexicon.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) { throw new IllegalStateException("Cannot read " + path, e); }
    }
    /** Exact standalone lower-case token only. Sentences and punctuation stay ordinary chat. */
    public static Optional<Word> recognize(String raw) {
        if (raw == null) return Optional.empty();
        return Optional.ofNullable(WORDS.get(raw.strip()));
    }
    private KelLexicon() {}
}
