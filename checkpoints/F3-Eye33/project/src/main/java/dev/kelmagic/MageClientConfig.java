package dev.kelmagic;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class MageClientConfig {
 private static final ModConfigSpec.Builder B=new ModConfigSpec.Builder();
 public static final ModConfigSpec.BooleanValue PHYSICS=B.comment("Cosmetic cloth-covered chest inertia for adult red mage NPCs. No gameplay effects.").define("mageBodyPhysics",true);
 public static final ModConfigSpec.BooleanValue ACCESSORIES=B.comment("Subtle hair, hat and tunic sway.").define("mageAccessoryMotion",true);
 public static final ModConfigSpec.BooleanValue BREATHING=B.comment("Cosmetic chest breathing and tiny lower-nose motion in experimental E. Does not select mouth textures.").define("mageBreathing",true);
 public static final ModConfigSpec.DoubleValue HAIR_HEAD_YAW_LIMIT=B.comment("Optional Hair7 extreme-pose fallback. 180 disables extra restriction; 60 is the preview alternative. Limits rendered head yaw only, not targeting/AI.").defineInRange("hairHeadYawLimit",180.0,35.0,180.0);
 public static final ModConfigSpec SPEC=B.build();private MageClientConfig(){}
}
