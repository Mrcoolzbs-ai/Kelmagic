package dev.kelmagic.core;

/** Pure heart/mana arithmetic. One missing mana is exactly TWO health points. */
public final class ManaMath {
    public record Spend(double remaining, double missing, float healthDamage) {}
    public static Spend spend(double mana, double cost) {
        if (!Double.isFinite(mana) || !Double.isFinite(cost) || mana < 0 || cost < 0) throw new IllegalArgumentException("Negative mana/cost");
        double missing = Math.max(0, cost - mana);
        return new Spend(Math.max(0, mana - cost), missing, (float)(missing * 2));
    }
    public static double regenerate(double mana, double max) { return Math.min(max, Math.max(0, mana) + 1); }
    public static double maximum(boolean dragonUpgrade, double equipmentBonus) {
        return (dragonUpgrade ? 15 : 5) + Math.max(0, equipmentBonus);
    }
    public static int interval(boolean dragonUpgrade, boolean potion) { return potion ? 60 : dragonUpgrade ? 100 : 200; }
    private ManaMath() {}
}
