package dev.kelmagic.server;
import dev.kelmagic.core.SpellParser;
import dev.kelmagic.network.PhraseVisual;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
/** Only called after successful, unchanged synergy execution. One cue, not particle packets. */
public final class PhraseVisuals {
 public static void applied(ServerPlayer p,SpellParser.Plan plan,SpellEffects.Aim aim){
  if(plan.actions().isEmpty())return;
  int kind=switch(plan.actions().getFirst()){case "fire_ring"->0;case "storm"->1;case "guardian"->2;default->-1;};
  if(kind<0)return;
  Vec3 at=kind==2?p.position():Vec3.atBottomCenterOf(aim.block().getBlockPos().above()).add(0,.06,0);
  float scale=kind==0?(float)Math.max(1,Math.min(3,Math.round(2*plan.scale()))/2.0):(float)Math.max(.7,Math.min(1.3,Math.sqrt(plan.scale())));
  var packet=new PhraseVisual(kind,at.x,at.y,at.z,scale,p.getRandom().nextInt(),kind==2?p.getId():-1);
  // Recipients in the same dimension near the actual effect, including the caster when near it.
  PacketDistributor.sendToPlayersNear(p.serverLevel(),null,at.x,at.y,at.z,48,packet);
 }
 private PhraseVisuals(){}
}
