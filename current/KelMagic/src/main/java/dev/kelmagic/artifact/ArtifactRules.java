package dev.kelmagic.artifact;
/** Pure, bounded arithmetic shared by gameplay and regression tests. */
public final class ArtifactRules {
    public static final int PARRY_WINDOW = 5, PARRY_COOLDOWN = 100, MAX_WORDS = 3;
    public static double finite(double value, double max) { return Double.isFinite(value) ? Math.clamp(value, 0, max) : 0; }
    public static double deposit(double current, double requested, double capacity) { return Math.min(finite(requested, capacity), Math.max(0, capacity-finite(current,capacity))); }
    public static boolean parry(int raisedTicks, boolean cooling, double incomingDot) { return raisedTicks >= 0 && raisedTicks <= PARRY_WINDOW && !cooling && incomingDot > .1; }
    public static double refund(double spellCost) { return finite(spellCost, 1024) * .5; }
    public static double regenFactor(boolean deepRing, int deepArmor) { return (deepRing ? .65 : 1) * (deepArmor > 0 ? Math.max(.6,1-deepArmor*.1) : 1); }
    private ArtifactRules() {}
}
