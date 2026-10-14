package dev.kelmagic.core;

import java.util.*;

/** Unsaved transient attachment. All mutations run on the server main thread. */
public final class SpellSession {
    public static final int LIMIT = 100;
    private final List<String> words = new ArrayList<>();
    private long generation;
    private int regenTicks;
    private int syncedMana=-1,syncedMaximum=-1;
    public boolean needsManaSync(int mana,int maximum) {
        if(mana==syncedMana && maximum==syncedMaximum) return false;
        syncedMana=mana;syncedMaximum=maximum;return true;
    }
    private long lastResyncTick = -1000;
    public boolean active() { return !words.isEmpty(); }
    public long generation() { return generation; }
    public int revision() { return words.size(); }
    public List<String> words() { return List.copyOf(words); }
    public boolean append(String word) {
        if (!KelLexicon.WORDS.containsKey(word)) throw new IllegalArgumentException("Unregistered token");
        if (words.size() == LIMIT) return false;
        if (words.isEmpty()) {
            do { generation = UUID.randomUUID().getMostSignificantBits(); } while (generation == 0);
        }
        words.add(word);
        return true;
    }
    public boolean authorize(long token, int revision) {
        return active() && token == generation && revision == words.size();
    }
    public boolean regenDue(int interval) {
        if (++regenTicks >= interval) { regenTicks = 0; return true; }
        return false;
    }
    public boolean allowResync(long tick) {
        if(tick-lastResyncTick<10) return false;
        lastResyncTick=tick; return true;
    }
    public void clear() { words.clear(); generation = 0; }
}
