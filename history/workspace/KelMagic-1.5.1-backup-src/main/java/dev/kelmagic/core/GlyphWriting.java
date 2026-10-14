package dev.kelmagic.core;

/** PUA slots in the bitmap atlas: 13 consonants x 6 forms (bare, a,e,i,o,u). */
public final class GlyphWriting {
    public static final String CONSONANTS = "ptkbdgmnslvrj";
    public static final String VOWELS = "aeiou";
    public static String write(String word) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            int consonant = CONSONANTS.indexOf(word.charAt(i));
            if (consonant < 0) throw new IllegalArgumentException("Expected consonant at " + i);
            int vowel = 0;
            if (i + 1 < word.length()) {
                int next = VOWELS.indexOf(word.charAt(i + 1));
                if (next >= 0) { vowel = next + 1; i++; }
            }
            result.append((char)(0xE000 + consonant * 6 + vowel));
        }
        return result.toString();
    }
    private GlyphWriting() {}
}
