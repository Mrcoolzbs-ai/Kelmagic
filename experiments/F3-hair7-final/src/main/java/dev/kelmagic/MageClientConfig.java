package dev.kelmagic;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class MageClientConfig {
 private static final ModConfigSpec.Builder B=new ModConfigSpec.Builder();
 public static final ModConfigSpec.BooleanValue PHYSICS=B.comment("Cosmetic cloth-covered chest inertia for adult red mage NPCs. No gameplay effects.").define("mageBodyPhysics",true);
 public static final ModConfigSpec.BooleanValue ACCESSORIES=B.comment("Subtle hair, hat and tunic sway.").define("mageAccessoryMotion",true);
 public static final ModConfigSpec.BooleanValue BREATHING=B.comment("Cosmetic chest breathing and tiny lower-nose motion in experimental E. Does not select mouth textures.").define("mageBreathing",true);
 public static final ModConfigSpec SPEC=B.build();private MageClientConfig(){}
}
