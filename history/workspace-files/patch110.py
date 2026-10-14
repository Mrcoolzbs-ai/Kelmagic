from pathlib import Path
import json
P=Path('/data/KelMagic'); J=P/'src/main/java/dev/kelmagic'; R=P/'src/main/resources'
def edit(p,a,b):
 t=p.read_text();assert a in t,str(p)+' missing '+a[:70];p.write_text(t.replace(a,b))
# already versioned
for p in (R/'data/kelmagic/advancement').glob('*.json'):
 d=json.loads(p.read_text());d['display']['announce_to_chat']=p.stem!='root';p.write_text(json.dumps(d,ensure_ascii=False,indent=2))
p=R/'data/kelmagic/magic/rarities.json'
d=json.loads(p.read_text());d['words']['pir']='epic';p.write_text(json.dumps(d,ensure_ascii=False,indent=2))
(J/'mixin/SpectralLocalPlayerMixin.java').write_text('''package dev.kelmagic.mixin;
import dev.kelmagic.MagicContent;import net.minecraft.client.player.LocalPlayer;import net.minecraft.world.entity.LivingEntity;import net.minecraft.world.item.ItemStack;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;
@Mixin(LocalPlayer.class) public abstract class SpectralLocalPlayerMixin {
 @Redirect(method="aiStep",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;canElytraFly(Lnet/minecraft/world/entity/LivingEntity;)Z")) private boolean kelmagic$clientWings(ItemStack s,LivingEntity e){return e.hasEffect(MagicContent.WINGS)||s.canElytraFly(e);}
}''')
(J/'mixin/SpectralElytraLayerMixin.java').write_text('''package dev.kelmagic.mixin;
import dev.kelmagic.MagicContent;import net.minecraft.client.renderer.entity.layers.ElytraLayer;import net.minecraft.client.model.ElytraModel;import net.minecraft.client.model.EntityModel;import net.minecraft.world.entity.LivingEntity;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.texture.OverlayTexture;import net.minecraft.resources.ResourceLocation;import com.mojang.blaze3d.vertex.PoseStack;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ElytraLayer.class) public abstract class SpectralElytraLayerMixin {
 @Shadow @Final private ElytraModel<LivingEntity> elytraModel;
 @Inject(method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",at=@At("HEAD"),cancellable=true)
 private void kelmagic$spectral(PoseStack pose,MultiBufferSource buffers,int light,LivingEntity e,float swing,float amount,float partial,float age,float yaw,float pitch,CallbackInfo ci){if(!e.hasEffect(MagicContent.WINGS)||e.isInvisible())return;pose.pushPose();pose.translate(0,0,.16);elytraModel.young=e.isBaby();elytraModel.crouching=e.isCrouching();elytraModel.setupAnim(e,swing,amount,age,yaw,pitch);elytraModel.renderToBuffer(pose,buffers.getBuffer(RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath("kelmagic","textures/entity/spectral_wings.png"))),light,OverlayTexture.NO_OVERLAY,0xCFFFFFFF);pose.popPose();ci.cancel();}
}''')
m=json.loads((R/'kelmagic.mixins.json').read_text());m['client']+=['SpectralLocalPlayerMixin','SpectralElytraLayerMixin','MagicTrimMixin'];(R/'kelmagic.mixins.json').write_text(json.dumps(m,indent=2))
# Base maximum override persisted; -1 restores equipment/heart default.
edit(J/'server/MagicAttachments.java','    private MagicAttachments() {}','    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Double>> BASE_MANA_MAX=TYPES.register("base_mana_max",()->AttachmentType.builder(()->-1d).serialize(Codec.doubleRange(-1,974)).copyOnDeath().build());\n    private MagicAttachments() {}')
edit(J/'server/MagicServer.java','return ManaMath.maximum(p.getData(MagicAttachments.MANA).dragonUpgrade(),equipmentBonus(p))','return (p.getData(MagicAttachments.BASE_MANA_MAX)>=0?p.getData(MagicAttachments.BASE_MANA_MAX)+equipmentBonus(p):ManaMath.maximum(p.getData(MagicAttachments.MANA).dragonUpgrade(),equipmentBonus(p)))')
edit(J/'MagicContent.java','    public static final DeferredHolder<MobEffect,MobEffect> WINGS=', '    public static final DeferredHolder<MobEffect,MobEffect> MANA_TIDE=EFFECTS.register("mana_tide",()->new FlowEffect());\n    public static final DeferredHolder<MobEffect,MobEffect> WINGS=')
edit(J/'MagicContent.java','out.accept(MagicStudy.TABLE_ITEM);','out.accept(MagicLamps.TORCH_ITEM);out.accept(MagicLamps.LANTERN_ITEM);out.accept(MagicStudy.TABLE_ITEM);')
# vanilla layered End-portal renderer through native block entity
edit(J/'MagicPortals.java','()->new Block(','()->new EndPortalBlock(')
edit(J/'MagicPortals.java','protected void entityInside','protected com.mojang.serialization.MapCodec<EndPortalBlock> codec(){return EndPortalBlock.CODEC;}\n protected void entityInside')
edit(J/'MagicPortals.java','BlockBehaviour.Properties.of().noCollission()','BlockBehaviour.Properties.of().noCollission().noOcclusion()')
(R/'assets/kelmagic/blockstates/temporary_end_gate.json').write_text(json.dumps({'variants':{'':{'model':'minecraft:block/end_portal'}}},indent=2))
# sol now requires aimed block, can be used alone. Wall selection added next.
f=J/'server/WorldSpells.java';t=f.read_text();a=t.index('if(a.equals("W_LIGHT")&&profile==');b=t.index(' if(aim.type()!=',a);t=t[:a]+t[b:];t=t.replace('case "W_LIGHT":return place(p,at,MagicLamps.TORCH.get().defaultBlockState(),false);','case "W_LIGHT":return place(p,at,MagicLamps.torchState(aim.block().getDirection()),false);');t=t.replace('Blocks.SEA_LANTERN.defaultBlockState()','MagicLamps.LANTERN.get().defaultBlockState()');f.write_text(t)
# no explosion-specific caps; linear amplification prevents 1.5^96 pathological radius
edit(J/'core/SpellParser.java','false,false,SpellProfiles.scale(words));','false,false,(grand.id().equals("blast")||grand.id().equals("meteor"))?GrandSpells.destructiveScale(words):SpellProfiles.scale(words));') if False else None
f=J/'core/SpellParser.java';t=f.read_text();old='new Plan(Effect.GRAND,List.of(grand.id()),"",false,false,SpellProfiles.scale(words))';assert old in t;t=t.replace(old,'new Plan(Effect.GRAND,List.of(grand.id()),"",false,false,(grand.id().equals("blast")||grand.id().equals("meteor"))?GrandSpells.destructiveScale(words):SpellProfiles.scale(words))');f.write_text(t)
edit(J/'core/GrandSpells.java',' private GrandSpells(){}',' public static double destructiveScale(Collection<String> words){return (1+.5*Collections.frequency(new ArrayList<>(words),"muk"))/(1+.5*Collections.frequency(new ArrayList<>(words),"nis"));}\n private GrandSpells(){}')
f=J/'server/GrandEffects.java';t=f.read_text().replace('(float)Math.min(16,2*plan.scale())','(float)(2*plan.scale())').replace('(float)Math.min(12,6*Math.sqrt(m.scale))','(float)(6*Math.sqrt(m.scale))');t=t.replace('target.setData(MagicAttachments.MANA_BOOST_END,target.serverLevel().getGameTime()+1200);','target.setData(MagicAttachments.MANA_BOOST_END,target.serverLevel().getGameTime()+1200);target.addEffect(new MobEffectInstance(MagicContent.MANA_TIDE,1200,0));');old='p.sendSystemMessage(Component.translatable("kelmagic.transcend.after1"));p.sendSystemMessage(Component.translatable("kelmagic.transcend.after2"));';new='''for(String key:List.of("kelmagic.transcend.after1","kelmagic.transcend.after2")){var speaker=Component.literal("???").withStyle(net.minecraft.ChatFormatting.OBFUSCATED,net.minecraft.ChatFormatting.DARK_RED);p.sendSystemMessage(Component.literal("<").withStyle(net.minecraft.ChatFormatting.DARK_RED).append(speaker).append(Component.literal("> ").withStyle(net.minecraft.ChatFormatting.DARK_RED)).append(Component.translatable(key).withStyle(net.minecraft.ChatFormatting.DARK_RED)));}p.serverLevel().setWeatherParameters(0,6000,true,false);for(int i=0;i<2;i++){var at=p.blockPosition().offset(i==0?5:-5,0,i==0?3:-3);at=p.serverLevel().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,at);if(p.serverLevel().hasChunkAt(at)){var bolt=EntityType.LIGHTNING_BOLT.create(p.serverLevel());if(bolt!=null){bolt.moveTo(Vec3.atBottomCenterOf(at));bolt.setVisualOnly(true);p.serverLevel().addFreshEntity(bolt);}}}''';assert old in t;t=t.replace(old,new);f.write_text(t)
print('base patch written')
