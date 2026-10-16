package dev.kelmagic.client;
import dev.kelmagic.*;import dev.kelmagic.entity.RedMage;import net.minecraft.client.model.HumanoidModel;import net.minecraft.client.model.geom.*;import net.minecraft.client.model.geom.builders.*;import net.minecraft.resources.ResourceLocation;
/** Generated mesh from art/red-mage/rig.json. Base skin identity is preserved. */
public final class RedMageModel extends HumanoidModel<RedMage> {
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(KelMagic.MODID,"red_mage"),"main");
 private final ModelPart chestLeft,chestRight,hairBack,tunicLeft,tunicRight;
 public RedMageModel(ModelPart root){super(root);chestLeft=body.getChild("chest_left");chestRight=body.getChild("chest_right");hairBack=head.getChild("hair_back");tunicLeft=body.getChild("tunic_left");tunicRight=body.getChild("tunic_right");}
 public static LayerDefinition layer(){var mesh=new MeshDefinition();var root=mesh.getRoot();
  var head=root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4.0F,-8.0F,-4.0F,8.0F,8.0F,8.0F),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
  var hair_crown=head.addOrReplaceChild("hair_crown",CubeListBuilder.create().texOffs(32,0).addBox(-4.0F,-8.0F,-4.0F,8.0F,8.0F,8.0F),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
  var hair_back=head.addOrReplaceChild("hair_back",CubeListBuilder.create().texOffs(64,44).addBox(-4.3F,-0.5F,3.6F,8.6F,8.5F,0.7F),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
  var hair_right=head.addOrReplaceChild("hair_right",CubeListBuilder.create().texOffs(86,44).addBox(-3.8F,-0.3F,-2.7F,1.6F,10.2F,0.65F),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
  var hair_left=head.addOrReplaceChild("hair_left",CubeListBuilder.create().texOffs(86,44).addBox(2.2F,-0.3F,-2.7F,1.6F,10.2F,0.65F),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
  var hat=root.addOrReplaceChild("hat",CubeListBuilder.create().texOffs(64,0).addBox(-7.0F,-9.0F,-6.0F,14.0F,1.0F,12.0F),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
  var hat_crown=hat.addOrReplaceChild("hat_crown",CubeListBuilder.create().texOffs(64,14).addBox(-4.0F,-12.0F,-4.0F,8.0F,3.0F,8.0F),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
  var hat_band=hat.addOrReplaceChild("hat_band",CubeListBuilder.create().texOffs(64,116).addBox(-4.25F,-10.0F,-4.25F,8.5F,1.0F,8.5F),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
  var hat_tier=hat.addOrReplaceChild("hat_tier",CubeListBuilder.create().texOffs(64,25).addBox(-2.6F,-14.0F,-3.0F,6.0F,2.0F,6.0F),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
  var hat_tip=hat.addOrReplaceChild("hat_tip",CubeListBuilder.create().texOffs(64,34).addBox(0.0F,-16.0F,-2.0F,4.0F,2.0F,4.0F),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
  var body=root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(16,16).addBox(-4.0F,0.0F,-2.0F,8.0F,12.0F,4.0F),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
  var chest_left=body.addOrReplaceChild("chest_left",CubeListBuilder.create().texOffs(64,80).addBox(-1.9F,0.0F,-3.9F,3.8F,5.0F,4.0F),PartPose.offsetAndRotation(1.9F,2.0F,-1.6F,-0.38F,-0.07F,0.0F));
  var chest_right=body.addOrReplaceChild("chest_right",CubeListBuilder.create().texOffs(96,80).addBox(-1.9F,0.0F,-3.9F,3.8F,5.0F,4.0F),PartPose.offsetAndRotation(-1.9F,2.0F,-1.6F,-0.38F,0.07F,0.0F));
  var tunic_left=body.addOrReplaceChild("tunic_left",CubeListBuilder.create().texOffs(64,64).addBox(-1.5F,0.0F,-0.3F,3.0F,5.0F,0.6F),PartPose.offsetAndRotation(2.2F,10.0F,-2.25F,0.0F,0.0F,0.0F));
  var tunic_right=body.addOrReplaceChild("tunic_right",CubeListBuilder.create().texOffs(78,64).addBox(-1.5F,0.0F,-0.3F,3.0F,5.0F,0.6F),PartPose.offsetAndRotation(-2.2F,10.0F,-2.25F,0.0F,0.0F,0.0F));
  var tunic_back=body.addOrReplaceChild("tunic_back",CubeListBuilder.create().texOffs(92,64).addBox(-4.0F,0.0F,-0.3F,8.0F,4.0F,0.6F),PartPose.offsetAndRotation(0.0F,10.0F,2.25F,0.0F,0.0F,0.0F));
  var belt_gem=body.addOrReplaceChild("belt_gem",CubeListBuilder.create().texOffs(100,92).addBox(-0.7F,-0.7F,-0.8F,1.4F,1.4F,0.8F),PartPose.offsetAndRotation(0.0F,9.7F,-2.25F,0.0F,0.0F,0.0F));
  var pouch=body.addOrReplaceChild("pouch",CubeListBuilder.create().texOffs(64,104).addBox(-0.8F,0.0F,-1.1F,2.5F,3.0F,2.2F),PartPose.offsetAndRotation(4.1F,12.3F,0.4F,0.0F,0.0F,0.0F));
  var pouch_strap=body.addOrReplaceChild("pouch_strap",CubeListBuilder.create().texOffs(92,104).addBox(-0.25F,0.0F,-1.0F,0.5F,2.6F,2.0F),PartPose.offsetAndRotation(4.15F,9.8F,0.4F,0.0F,0.0F,0.0F));
  var right_arm=root.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(40,16).addBox(-2.0F,-2.0F,-2.0F,3.0F,12.0F,4.0F),PartPose.offsetAndRotation(-5.0F,2.0F,0.0F,0.0F,0.0F,0.0F));
  var left_arm=root.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(32,48).addBox(-1.0F,-2.0F,-2.0F,3.0F,12.0F,4.0F),PartPose.offsetAndRotation(5.0F,2.0F,0.0F,0.0F,0.0F,0.0F));
  var bracelet=left_arm.addOrReplaceChild("bracelet",CubeListBuilder.create().texOffs(84,92).addBox(-1.1F,0.0F,-2.1F,3.2F,0.8F,4.2F),PartPose.offsetAndRotation(0.0F,7.2F,0.0F,0.0F,0.0F,0.0F));
  var right_leg=root.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(0,16).addBox(-2.0F,0.0F,-2.0F,4.0F,12.0F,4.0F),PartPose.offsetAndRotation(-1.9F,12.0F,0.0F,0.0F,0.0F,0.0F));
  var left_leg=root.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(16,48).addBox(-2.0F,0.0F,-2.0F,4.0F,12.0F,4.0F),PartPose.offsetAndRotation(1.9F,12.0F,0.0F,0.0F,0.0F,0.0F));
  var right_boot=right_leg.addOrReplaceChild("right_boot",CubeListBuilder.create().texOffs(64,92).addBox(-2.1F,0.0F,-2.1F,4.2F,1.2F,4.2F),PartPose.offsetAndRotation(0.0F,4.7F,0.0F,0.0F,0.0F,0.0F));
  var left_boot=left_leg.addOrReplaceChild("left_boot",CubeListBuilder.create().texOffs(64,92).addBox(-2.1F,0.0F,-2.1F,4.2F,1.2F,4.2F),PartPose.offsetAndRotation(0.0F,4.7F,0.0F,0.0F,0.0F,0.0F));
  return LayerDefinition.create(mesh,128,128);
 }
 @Override public void setupAnim(RedMage e,float walk,float speed,float age,float yaw,float pitch){super.setupAnim(e,walk,speed,age,yaw,pitch);float partial=net.minecraft.util.Mth.clamp(age-e.tickCount,0,1);chestLeft.y=chestRight.y=2F;chestLeft.x=1.9F;chestRight.x=-1.9F;chestLeft.xRot=chestRight.xRot=-.38F;chestLeft.yRot=-.07F;chestRight.yRot=.07F;if(MageClientConfig.PHYSICS.get()){chestLeft.y+=e.chestLeft.lift(partial);chestRight.y+=e.chestRight.lift(partial);chestLeft.xRot+=e.chestLeft.pitch(partial);chestRight.xRot+=e.chestRight.pitch(partial);chestLeft.x+=e.chestLeft.sway(partial);chestRight.x+=e.chestRight.sway(partial);}hairBack.xRot=tunicLeft.xRot=tunicRight.xRot=0;if(MageClientConfig.ACCESSORIES.get()){hairBack.xRot=(float)Math.sin(age*.055)*.018F+Math.min(speed,.6F)*.08F;tunicLeft.xRot=Math.abs(leftLeg.xRot)*.13F;tunicRight.xRot=Math.abs(rightLeg.xRot)*.13F;}}
}
