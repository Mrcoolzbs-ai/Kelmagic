package dev.kelmagic.speech;

import java.util.List;
import dev.kelmagic.speech.MouthTimeline.*;

public final class MouthTimelineTests {
    private static int checks;
    private static void ok(boolean condition, String message) {
        checks++; if (!condition) throw new AssertionError(message);
    }
    private static void rejects(Runnable action, String message) {
        boolean rejected = false;
        try { action.run(); } catch (IllegalArgumentException | ArithmeticException expected) { rejected = true; }
        ok(rejected, message);
    }
    public static Track manualTrack() {
        return new Track(List.of(new Cue(0,100,Viseme.CLOSED_MPB), new Cue(100,260,Viseme.OPEN_A),
                new Cue(260,420,Viseme.ROUND_O), new Cue(420,480,Viseme.REST)),480);
    }
    public static void main(String[] args) {
        var mouth = new MouthTimeline(); mouth.selectEmotion(Emotion.SMILE);
        ok(mouth.frame(0).viseme()==null && mouth.frame(0).baseEmotion()==Emotion.SMILE,"idle base");
        long first=mouth.start(100,manualTrack());
        ok(mouth.frame(99).viseme()==null,"no early speech");
        ok(mouth.frame(100).viseme()==Viseme.CLOSED_MPB,"start exact");
        ok(mouth.frame(199).viseme()==Viseme.CLOSED_MPB,"end-exclusive cue");
        ok(mouth.frame(200).viseme()==Viseme.OPEN_A,"next cue exact");
        mouth.selectEmotion(Emotion.LAUGH);
        ok(mouth.frame(210).viseme()==Viseme.OPEN_A && mouth.frame(210).baseEmotion()==Emotion.LAUGH,"new base retained during override");
        ok(mouth.frame(580).viseme()==null && mouth.frame(580).baseEmotion()==Emotion.LAUGH,"finish returns CURRENT base");
        ok(!mouth.hasPendingTrack() && mouth.queuedCueCount()==0,"deadline clears timers/queue");
        ok(!mouth.cancel(first),"finished token inactive");
        long second=mouth.start(600,manualTrack());
        ok(mouth.cancel(second),"cancel current");
        ok(!mouth.hasPendingTrack() && mouth.frame(610).baseEmotion()==Emotion.LAUGH,"cancel preserves base");
        long stale=mouth.start(1000,manualTrack()); long latest=mouth.start(1001,manualTrack());
        ok(latest>stale && !mouth.cancel(stale),"stale cancel cannot kill replacement");
        ok(mouth.frame(1002).speechId()==latest,"latest speech active");
        ok(mouth.frame(1481).viseme()==null && !mouth.hasPendingTrack(),"rapid replacement still finishes");
        mouth.start(2000,manualTrack());
        mouth.startUtterance(2001,"  ",ignored->{throw new AssertionError("empty text must bypass provider");});
        ok(!mouth.hasPendingTrack() && mouth.frame(2002).viseme()==null,"blank clears");
        mouth.startUtterance(2003,null,null);
        ok(!mouth.hasPendingTrack(),"null text clears");
        mouth.start(2004,manualTrack()); mouth.start(2005,Track.empty());
        ok(mouth.queuedCueCount()==0,"empty track clears");
        var gap = new Track(List.of(new Cue(20,40,Viseme.OPEN_A)),60);mouth.start(3000,gap);
        ok(mouth.frame(3001).viseme()==Viseme.REST,"leading gap uses rest");
        ok(mouth.frame(3045).viseme()==Viseme.REST,"trailing gap uses rest");
        ok(mouth.frame(3060).viseme()==null,"gap track finishes");
        // Determinism of an explicit timing provider, not proof of phonetic text conversion.
        TimingSource scriptedTiming=explicitNpcText->manualTrack();
        var a=new MouthTimeline();var b=new MouthTimeline();
        a.startUtterance(4000,"MANUAL_TEST_UTTERANCE",scriptedTiming);b.startUtterance(4000,"MANUAL_TEST_UTTERANCE",scriptedTiming);
        for(long t=4000;t<=4480;t+=20)ok(a.frame(t).equals(b.frame(t)),"same explicit track/utterance adapter deterministic");
        for(int fps:new int[]{15,30,60,144}){
            var fine=new MouthTimeline();var coarse=new MouthTimeline();fine.start(5000,manualTrack());coarse.start(5000,manualTrack());
            long last=5000;
            for(long t=5000;t<=5500;t+=20){
                long step=Math.max(1,1000/fps);for(long mid=last;mid<t;mid+=step)fine.frame(mid);
                ok(fine.frame(t).equals(coarse.frame(t)),"absolute time independent of sampling rate "+fps);last=t;
            }
        }
        var left=new MouthTimeline();var right=new MouthTimeline();left.selectEmotion(Emotion.SMIRK);right.selectEmotion(Emotion.SAD);left.start(6000,manualTrack());
        ok(!right.hasPendingTrack() && right.frame(6010).baseEmotion()==Emotion.SAD,"per-instance isolation");
        for(var emotion:Emotion.values()){
            mouth.selectEmotion(emotion);mouth.start(7000,manualTrack());mouth.cancelAll();
            ok(mouth.frame(7010).baseEmotion()==emotion && mouth.frame(7010).viseme()==null,"all 9 bases survive cancel");
        }
        rejects(()->new Cue(-1,10,Viseme.REST),"negative cue");
        rejects(()->new Cue(10,10,Viseme.REST),"zero-length cue");
        rejects(()->new Track(List.of(new Cue(0,20,Viseme.REST),new Cue(10,30,Viseme.OPEN_A)),30),"overlap rejected");
        rejects(()->new Track(List.of(new Cue(0,20,Viseme.REST)),10),"duration mismatch");
        rejects(()->mouth.start(Long.MAX_VALUE,manualTrack()),"deadline overflow");
        ok(mouth.frame(7010).baseEmotion()==Emotion.SAD,"bad input does not overwrite base");
        System.out.println("MOUTH_TIMELINE_TESTS_OK checks="+checks);
        System.out.println("Scope: isolated manual/audio-ready timing logic; no phonetic mapper, networking or Minecraft QA.");
    }
}
