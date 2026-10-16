package dev.kelmagic.speech;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import dev.kelmagic.speech.MouthTimeline.*;

/** Text-only approximate timing. Does NOT claim acoustic alignment or full grapheme-to-phoneme analysis. */
public final class TextVisemeSource implements TimingSource {
    public enum Language { KEL, RUSSIAN, LATIN_FALLBACK }
    private final Language language;
    public TextVisemeSource(Language language) { this.language = java.util.Objects.requireNonNull(language); }
    public record Segment(String phoneme, Viseme viseme, long durationMillis) {}
    public List<Segment> segments(String text) {
        if (text == null || text.isBlank()) return List.of();
        String n = Normalizer.normalize(text.toLowerCase(Locale.ROOT), language==Language.KEL ? Normalizer.Form.NFD : Normalizer.Form.NFC);
        List<Segment> out = new ArrayList<>();
        boolean previousVowel = false;
        for (int i = 0; i < n.length(); i++) {
            char c = n.charAt(i);
            if (c == '\u0304') continue; // Consumed as vowel length below; never interpreted as its own sound.
            if (Character.getType(c) == Character.NON_SPACING_MARK) continue;
            if (Character.isWhitespace(c)) { addPause(out,90); previousVowel = false; continue; }
            if (",;:—–-".indexOf(c) >= 0) { addPause(out,220); previousVowel = false; continue; }
            if (".!?…".indexOf(c) >= 0) { addPause(out,350); previousVowel = false; continue; }
            if ("'’\"«»()".indexOf(c) >= 0) continue; // Stress/quotation marks do not produce a phoneme.
            String p = String.valueOf(c);
            if (language == Language.LATIN_FALLBACK && i+1<n.length()) {
                String pair=n.substring(i,i+2);
                if (List.of("sh","ch","ph","th","ee","oo").contains(pair)) {p=pair;i++;}
            }
            boolean vowel = isVowel(p);
            boolean longVowel = vowel && i+1<n.length() && n.charAt(i+1)=='\u0304';
            if (longVowel) i++;
            // Adjacent Kel vowels stay separate phoneme segments, WITHOUT inventing an intraword pause.
            // Same-shape visual cues may merge, but durations and phoneme segments are retained.
            if (language==Language.RUSSIAN && "яюе".contains(p) && (out.isEmpty() || previousVowel || out.getLast().phoneme().equals("pause")))
                out.add(new Segment("j",Viseme.SPREAD_IE,55));
            Viseme v=map(p);
            out.add(new Segment(p+(longVowel?"ː":""),v,(vowel?145:65)*(longVowel?2:1)));
            previousVowel=vowel;
        }
        return List.copyOf(out);
    }
    private static void addPause(List<Segment> out,long ms) {
        if (out.isEmpty()) return;
        if (out.getLast().phoneme().equals("pause")) {
            var s=out.removeLast();out.add(new Segment("pause",Viseme.REST,Math.max(ms,s.durationMillis())));
        } else out.add(new Segment("pause",Viseme.REST,ms));
    }
    private boolean isVowel(String p) {
        return p.length()==1 && (language==Language.RUSSIAN ? "аэеиоуыяюё" : "aeiou").contains(p)
            || language==Language.LATIN_FALLBACK && (p.equals("ee")||p.equals("oo"));
    }
    private Viseme map(String p) {
        if (language==Language.RUSSIAN) return switch(p) {
            case "а","я" -> Viseme.OPEN_A;
            case "о","ё" -> Viseme.ROUND_O;
            case "у","ю" -> Viseme.ROUND_U;
            case "э","е","и","ы","й" -> Viseme.SPREAD_IE;
            case "п","б","м" -> Viseme.CLOSED_MPB;
            case "ф","в" -> Viseme.TEETH_FV;
            case "т","д","н","л","р" -> Viseme.TONGUE_TDNL;
            case "с","з","ш","ж","ч","щ","ц" -> Viseme.HUSH_SH;
            default -> Viseme.REST;
        };
        return switch(p) {
            case "a" -> Viseme.OPEN_A;
            case "o" -> Viseme.ROUND_O;
            case "u","oo" -> Viseme.ROUND_U;
            case "e","i","j","ee" -> Viseme.SPREAD_IE;
            case "p","b","m" -> Viseme.CLOSED_MPB;
            case "v","f","ph" -> Viseme.TEETH_FV;
            case "t","d","n","l","r","th" -> Viseme.TONGUE_TDNL;
            case "s","z","sh","ch" -> Viseme.HUSH_SH;
            default -> Viseme.REST; // Kel k/g are posterior gestures, not invented vowel openings.
        };
    }
    @Override public Track plan(String text) {
        List<Cue> cues=new ArrayList<>();long t=0;
        for(Segment s:segments(text)) {
            long end=Math.addExact(t,s.durationMillis());
            if(!cues.isEmpty()&&cues.getLast().viseme()==s.viseme()) {
                Cue prev=cues.removeLast();cues.add(new Cue(prev.startMillis(),end,s.viseme()));
            } else cues.add(new Cue(t,end,s.viseme()));
            t=end;
        }
        return new Track(cues,t);
    }
}
