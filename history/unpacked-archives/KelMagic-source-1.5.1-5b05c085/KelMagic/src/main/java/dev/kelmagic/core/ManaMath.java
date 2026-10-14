package dev.kelmagic.core;

/** Pure heart/mana arithmetic. One missing mana is exactly TWO health points. */
public final class ManaMath {
    public record Spend(int remaining, int missing, float healthDamage) {}
    public static Spend spend(int mana, int cost) {
        if (mana < 0 || cost < 0) throw new IllegalArgumentException("Negative mana/cost");
        int missing = Math.max(0, cost - mana);
        return new Spend(Math.max(0, mana - cost), missing, missing * 2f);
    }
    public static int regenerate(int mana, int max) { return Math.min(max, Math.max(0, mana) + 1); }
    public static int maximum(boolean dragonUpgrade, int equipmentBonus) {
        return (dragonUpgrade ? 15 : 5) + Math.max(0, equipmentBonus);
    }
    public static int interval(boolean dragonUpgrade, boolean potion) { return potion ? 60 : dragonUpgrade ? 100 : 200; }
    private ManaMath() {}
}
