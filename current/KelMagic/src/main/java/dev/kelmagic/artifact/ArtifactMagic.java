package dev.kelmagic.artifact;
import dev.kelmagic.core.*;
import dev.kelmagic.server.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import java.util.*;
public final class ArtifactMagic {
    private static final class Cast {final ServerPlayer caster;final double cost;final SpellEffects.Aim aim;final boolean ordinary;boolean area;final List<ArtifactSpellBolt> bolts=new ArrayList<>();Cast(ServerPlayer p,double c,SpellEffects.Aim a,boolean o){caster=p;cost=c;aim=a;ordinary=o;}}
    private static final ThreadLocal<Cast> CURRENT=new ThreadLocal<>();
    public static boolean execute(ServerPlayer p,SpellParser.Plan plan,List<String> words,double cost,SpellEffects.Aim override){if(!ArtifactRuntime.canCast(p))return false;var old=CURRENT.get();var cast=new Cast(p,cost,override,plan.effect()!=SpellParser.Effect.GRAND);cast.area=plan.area();CURRENT.set(cast);try{double scale=plan.scale();if(plan.area()&&ArtifactRuntime.has(p,ArtifactContent.FAR.get()))scale*=.8;if(p.getMainHandItem().is(ArtifactContent.RAPIER.get())&&p.getPersistentData().getLong("KelRapierUntil")>p.serverLevel().getGameTime())scale*=1.15;var modified=new SpellParser.Plan(plan.effect(),plan.actions(),plan.selector(),plan.area(),plan.negate(),scale);boolean result=SpellEffects.execute(p,modified);ArtifactWorld.remember(p,words);return result;}finally{double share=cast.bolts.isEmpty()?0:cost/cast.bolts.size();for(var bolt:cast.bolts)bolt.cost(share);p.getPersistentData().remove("KelRapierUntil");if(old==null)CURRENT.remove();else CURRENT.set(old);}}
    public static SpellEffects.Aim aim(ServerPlayer p){var cast=CURRENT.get();return cast!=null&&cast.caster==p?cast.aim:null;}
    public static boolean protectForest(ServerPlayer p,BlockState state){var c=CURRENT.get();return c!=null&&c.ordinary&&ArtifactRuntime.has(p,ArtifactContent.FOREST.get())&&(state.is(BlockTags.LOGS)||state.is(BlockTags.LEAVES)||state.is(BlockTags.SAPLINGS)||state.is(BlockTags.FLOWERS)||state.getBlock() instanceof net.minecraft.world.level.block.BushBlock);}
    public static void bolt(ArtifactSpellBolt bolt){var c=CURRENT.get();if(c!=null)c.bolts.add(bolt);}
    public static boolean area(ServerPlayer p){var cast=CURRENT.get();return cast!=null&&cast.caster==p&&cast.area;}
    public static double cost(){var c=CURRENT.get();return c==null?0:c.cost;}
    public static boolean allowedTarget(LivingEntity entity){return !ArtifactRuntime.immune(entity);}
    public static void clear(){CURRENT.remove();}
    private ArtifactMagic(){}
}
