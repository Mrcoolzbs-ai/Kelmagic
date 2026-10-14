package dev.kelmagic;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Bounded server settings; unlike the removed MDK demo these control real magic. */
public final class Config {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static final ModConfigSpec.IntValue RANGE = B.defineInRange("targetRange",48,4,96);
    public static final ModConfigSpec.BooleanValue RANGE_MIGRATED=B.comment("1.8 default range migration has run. Leave true to preserve your chosen range.").define("rangeMigration18",false);
    public static final ModConfigSpec.IntValue AREA_RADIUS = B.defineInRange("areaRadius",8,1,12);
    public static final ModConfigSpec.IntValue MAX_TARGETS = B.defineInRange("maxTargets",32,1,64);
    public static final ModConfigSpec.IntValue MAX_FIRES = B.defineInRange("maxAreaFires",16,1,32);
    public static final ModConfigSpec.IntValue FIRE_SECONDS = B.comment("Entity burn base duration; magic multiplies this by 3. Unmodified block fire lasts 5 seconds; muk/nis scale spell fire.").defineInRange("fireSeconds",10,1,60);
    public static final ModConfigSpec.IntValue RESIST_SECONDS = B.defineInRange("resistanceSeconds",60,1,300);
    public static final ModConfigSpec.IntValue OVERLOAD_POWER = B.comment("Never set a vanilla explosion to 200000. Hard bounded for server safety.")
        .defineInRange("overloadPower",12,4,16);
    public static final ModConfigSpec.IntValue OVERLOAD_BURSTS = B.comment("Ten bounded bursts instead of one: 10x ray/explosion budget, NOT 10x radius. Existing overloadPower remains per burst.").defineInRange("overloadBursts",10,1,10);
    public static final ModConfigSpec.BooleanValue OVERLOAD_TERRAIN = B.comment("Allow bounded overload explosion to damage terrain. Use only in backed-up test worlds.")
        .define("overloadDamagesTerrain",true);
    public static final ModConfigSpec SPEC = B.build();
    private Config() {}
}
