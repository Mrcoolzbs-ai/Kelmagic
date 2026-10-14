package dev.kelmagic.core;

import java.util.*;

/** Dependency-free tests; executed by Gradle coreTest and usable with plain Java 21. */
public final class CoreTests {
    private static int checks;
    private static void check(boolean condition, String name) {
        checks++;
        if (!condition) throw new AssertionError(name);
    }
    private static void permutations(List<String> words, int from, SpellParser.Effect expected) {
        if (from == words.size()) { check(SpellParser.parse(words) == expected, "permutation " + words); return; }
        for (int i = from; i < words.size(); i++) {
            Collections.swap(words, from, i); permutations(words, from + 1, expected); Collections.swap(words, from, i);
        }
    }
    public static void main(String[] args) {
        check(KelLexicon.WORDS.size() == 6, "verified subset");
        for (String word : KelLexicon.WORDS.keySet()) check(KelLexicon.recognize(word).isPresent(), word);
        for (String text : List.of("hello", "mina no pir", "pir!", "Pir", "pol", "dul", "/pir", "")) check(KelLexicon.recognize(text).isEmpty(), "ordinary chat " + text);
        String[][] required = {{"pir"},{"tuna","pir"},{"sila","pir"},{"mina","no","pir"},{"lu","tuna"},{"lu","pir"},{"lu","tuna","pir"},{"lu","sila","pir"}};
        SpellParser.Effect[] expected = {SpellParser.Effect.FIRE_BLOCK, SpellParser.Effect.FIRE_PLAYER, SpellParser.Effect.FIRE_MOB, SpellParser.Effect.RESIST_SELF, SpellParser.Effect.SELECTION_ONLY, SpellParser.Effect.FIRE_AREA, SpellParser.Effect.FIRE_PLAYERS, SpellParser.Effect.FIRE_MOBS};
        for (int i = 0; i < required.length; i++) permutations(new ArrayList<>(List.of(required[i])), 0, expected[i]);
        check(SpellParser.parse(List.of("pir","pir")) == SpellParser.Effect.UNDEFINED, "no implicit amplification");
        check(SpellParser.parse(List.of("mina","tuna","pir")) == SpellParser.Effect.UNDEFINED, "ambiguous selector");
        int mana = 5; float damage = 0;
        for (int cost : new int[]{4,2,3}) { var spend = ManaMath.spend(mana,cost); mana = spend.remaining(); damage += spend.healthDamage(); }
        check(mana == 0 && damage == 8f, "continuous spell loses four hearts");
        mana = ManaMath.spend(5,4).remaining(); mana = ManaMath.regenerate(mana,5);
        var spend = ManaMath.spend(mana,2); check(spend.healthDamage() == 0, "no cost after 10 sec"); mana = spend.remaining();
        for (int i=0;i<3;i++) mana = ManaMath.regenerate(mana,5);
        check(ManaMath.spend(mana,3).healthDamage() == 0, "pir after 30 sec");
        check(ManaMath.maximum(false,0)==5 && ManaMath.maximum(true,0)==15, "base max");
        check(ManaMath.regenerate(5,5)==5, "regen cap");
        SpellSession s = new SpellSession(); check(!s.regenDue(200), "idle regen first tick"); s.append("mina");
        for(int i=0;i<198;i++) check(!s.regenDue(200), "tick boundary"); check(s.regenDue(200), "regen on tick 200");
        long old = s.generation(); check(s.authorize(old,1), "valid revision"); check(!s.authorize(old,0), "stale revision");
        s.clear(); check(!s.authorize(old,1), "replay after clear"); s.append("pir"); check(!s.authorize(old,1), "new session generation");
        while(s.revision()<100) check(s.append("pir"), "allowed token"); check(!s.append("pir") && s.revision()==100, "101 overflow without autoexecution");
        SpellSession idle = new SpellSession(); for(int i=0;i<100;i++) idle.regenDue(200); idle.append("mina"); idle.clear(); for(int i=0;i<99;i++) idle.regenDue(200); check(idle.regenDue(200), "completion and cancellation do not reset idle recovery");
        SpellSession other = new SpellSession(); other.append("no"); check(other.revision()==1 && s.revision()==100, "player isolation");
        check(GlyphWriting.write("mina").equals(""+(char)(0xE000+6*6+3)+(char)(0xE000+7*6+1)), "m+i n+a");
        check(GlyphWriting.write("pir").equals(""+(char)(0xE000+3)+(char)(0xE000+11*6)), "p+i r bare");
        for(String word:KelLexicon.WORDS.keySet()) check(!GlyphWriting.write(word).isEmpty(), "glyph writing "+word);
        check(s.allowResync(0), "first resync");check(!s.allowResync(1), "resync rate limited");check(s.allowResync(10), "resync interval");
        check(SpellParser.parse(List.of("lu","mina","pir"))==SpellParser.Effect.UNDEFINED,"undefined self plurality not silently ignored");
        System.out.println("CORE TESTS PASSED: " + checks + " checks");
    }
}
