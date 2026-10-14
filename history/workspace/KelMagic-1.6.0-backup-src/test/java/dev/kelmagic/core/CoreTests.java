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
        check(KelLexicon.WORDS.size() == 180, "expanded verified subset");
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
        check(KelLexicon.WORDS.containsKey("nis")&&!KelLexicon.WORDS.containsKey("zin"),"confirmed nis replacement");
        check(KelLexicon.WORDS.get("sin").translation().equals("выход"),"sin retains source meaning");
        check(SpellProfiles.ALL.size()==172,"all non-operator new words have profiles");
        for(String word:SpellProfiles.ALL.keySet()){
            check(SpellParser.parse(List.of(word))==SpellParser.Effect.COMPOSITE,"standalone profile "+word);
            check(SpellParser.plan(List.of("mina",word,"muk","nis")).equals(SpellParser.plan(List.of("nis",word,"mina","muk"))),"order-independent profile "+word);
            check(SpellProfiles.ALL.get(word).description().length()>0,"documented effect "+word);
        }
        permutations(new ArrayList<>(List.of("mina","liv","pir")),0,SpellParser.Effect.FLAME_LIFE);
        permutations(new ArrayList<>(List.of("muk","mina","liv","pir","nis")),0,SpellParser.Effect.FLAME_LIFE);
        check(Math.abs(SpellProfiles.scale(List.of("muk","muk"))-2.25)<.00001,"repeated muk multiplies");
        check(Math.abs(SpellProfiles.scale(List.of("nis","nis"))-1/2.25)<.00001,"repeated nis divides");
        check(SpellProfiles.scale(List.of("muk","muk","nis","nis"))==1,"operators cancel regardless of order");
        check(SpellProfiles.duration(1800,1.5)==2700,"scaled fire duration");
        check(SpellProfiles.duration(1800,100)==3600,"duration cap");
        check(SpellProfiles.scale(Collections.nCopies(94,"muk"))<=12,"scale cap");
        check(SpellParser.parse(Collections.nCopies(101,"muk"))==SpellParser.Effect.UNDEFINED,"scale does not bypass 100 word cap");
        check(SpellParser.parse(List.of("muk"))==SpellParser.Effect.SELECTION_ONLY,"modifier alone incomplete");
        check(SpellParser.parse(List.of("liv","liv"))==SpellParser.Effect.UNDEFINED,"no accidental repeated action");
        check(SpellParser.parse(List.of("muk","tuna","pir"))==SpellParser.Effect.FIRE_PLAYER,"legacy scaled fire still valid");
        check(SpellParser.parse(List.of("mina","liv","pir","no"))!=SpellParser.Effect.FLAME_LIFE,"healer exact special combination");
        for(char c:GlyphWriting.CONSONANTS.toCharArray())for(char v:GlyphWriting.VOWELS.toCharArray())check(GlyphWriting.write(""+c+v).length()==1,"every composed glyph "+c+v);
        System.out.println("CORE TESTS PASSED: " + checks + " checks");
    }
}
