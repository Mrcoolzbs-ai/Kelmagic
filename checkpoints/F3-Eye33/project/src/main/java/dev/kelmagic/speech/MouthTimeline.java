package dev.kelmagic.speech;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Isolated timing/arbitration logic only: no Minecraft, texture paths or GeckoLib state writers. */
public final class MouthTimeline {
    public enum Emotion {
        NEUTRAL, SMILE, WIDE_SMILE, SMIRK, DISPLEASED, SURPRISE, FEAR, LAUGH, SAD;
        public String assetId() { return name().toLowerCase(Locale.ROOT); }
    }
    public enum Viseme {
        CLOSED_MPB, OPEN_A, ROUND_O, ROUND_U, SPREAD_IE, TEETH_FV, TONGUE_TDNL, HUSH_SH, REST;
        public String assetId() { return name().toLowerCase(Locale.ROOT); }
    }
    /** Offset times in milliseconds, end-exclusive. Gaps become REST while speech remains active. */
    public record Cue(long startMillis, long endMillis, Viseme viseme) {
        public Cue {
            Objects.requireNonNull(viseme);
            if (startMillis < 0 || endMillis <= startMillis) throw new IllegalArgumentException("cue times");
        }
    }
    public record Track(List<Cue> cues, long durationMillis) {
        public Track {
            cues = List.copyOf(cues);
            if (durationMillis < 0) throw new IllegalArgumentException("duration");
            long previousEnd = 0;
            for (Cue cue : cues) {
                if (cue.startMillis < previousEnd || cue.endMillis > durationMillis)
                    throw new IllegalArgumentException("unsorted, overlapping or out-of-duration cues");
                previousEnd = cue.endMillis;
            }
        }
        public static Track empty() { return new Track(List.of(), 0); }
    }
    /** Replaceable timing boundary. A real text/phonetic or audio provider belongs outside this controller. */
    @FunctionalInterface public interface TimingSource { Track plan(String characterUtterance); }
    public record Frame(Emotion baseEmotion, Viseme viseme, boolean speaking, long speechId) {
        public String assetId() { return viseme == null ? baseEmotion.assetId() : viseme.assetId(); }
        public String layer() { return viseme == null ? "emotions" : "visemes"; }
    }
    private Emotion emotion = Emotion.NEUTRAL;
    private Track track;
    private long generation, activeId, startMillis, deadlineMillis;

    public void selectEmotion(Emotion emotion) { this.emotion = Objects.requireNonNull(emotion); }
    public Emotion baseEmotion() { return emotion; }
    public boolean hasPendingTrack() { return track != null; }
    public int queuedCueCount() { return track == null ? 0 : track.cues().size(); }
    public long start(long nowMillis, Track newTrack) {
        Objects.requireNonNull(newTrack);
        if (nowMillis < 0) throw new IllegalArgumentException("negative time");
        long end = Math.addExact(nowMillis, newTrack.durationMillis());
        long token = Math.incrementExact(generation);
        generation = token;
        clear(); // Replace old speech; base emotion is intentionally retained.
        if (newTrack.durationMillis() == 0) return token;
        track = newTrack; activeId = token; startMillis = nowMillis; deadlineMillis = end;
        return token;
    }
    /** Only explicit character utterances may call this adapter; never subscribe it to general chat. */
    public long startUtterance(long nowMillis, String text, TimingSource source) {
        if (text == null || text.isBlank()) return start(nowMillis, Track.empty());
        return start(nowMillis, Objects.requireNonNull(source).plan(text));
    }
    /** Cancel matching speech only, so a delayed old cancel cannot kill its replacement. */
    public boolean cancel(long token) {
        if (track == null || activeId != token) return false;
        clear(); return true;
    }
    public void cancelAll() { clear(); }
    private void clear() { track = null; activeId = startMillis = deadlineMillis = 0; }
    public Frame frame(long nowMillis) {
        if (nowMillis < 0) throw new IllegalArgumentException("negative time");
        if (track == null) return new Frame(emotion, null, false, 0);
        if (nowMillis < startMillis) return new Frame(emotion, null, false, activeId);
        if (nowMillis >= deadlineMillis) { clear(); return new Frame(emotion, null, false, 0); }
        long offset = nowMillis - startMillis;
        for (Cue cue : track.cues()) {
            if (offset >= cue.startMillis && offset < cue.endMillis)
                return new Frame(emotion, cue.viseme, true, activeId);
        }
        return new Frame(emotion, Viseme.REST, true, activeId);
    }
}
