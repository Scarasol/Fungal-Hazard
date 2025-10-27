package com.scarasol.fungalhazard.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.scarasol.fungalhazard.client.model.FungalZombieEntityModel;
import com.scarasol.fungalhazard.entity.humanoid.AbstractHumanoidFungalZombie;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

import java.util.Optional;

/**
 * @author Scarasol
 */
public class HumanoidFungalZombieEntityRenderer<T extends AbstractHumanoidFungalZombie> extends FungalZombieEntityRenderer<T> {

    private Matrix4f mainPose;
    private Matrix3f mainNormal;

    private Matrix4f offPose;
    private Matrix3f offNormal;

    public HumanoidFungalZombieEntityRenderer(EntityRendererProvider.Context renderManager, boolean glow) {
        this(renderManager, new FungalZombieEntityModel<>(), glow);
    }

    public HumanoidFungalZombieEntityRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model, boolean glow) {
        super(renderManager, model, glow);
//        addRenderLayer(new ItemArmorGeoLayer<>(this) {
//            @NotNull
//            @Override
//            protected EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, T animatable) {
//                return switch (bone.getName()) {
//                    case "MainBody", "LeftArm", "RightArm" -> EquipmentSlot.CHEST;
//                    case "RightLeg", "LeftLeg" -> EquipmentSlot.LEGS;
//                    case "Head" -> EquipmentSlot.HEAD;
//                    case "RightForeLeg", "LeftForeLeg" -> EquipmentSlot.FEET;
//                    default -> super.getEquipmentSlotForBone(bone, stack, animatable);
//                };
//            }
//
//            @Nullable
//            @Override
//            protected ItemStack getArmorItemForBone(GeoBone bone, T animatable) {
//                return switch (bone.getName()) {
//                    case "MainBody", "LeftArm", "RightArm" -> chestplateStack;
//                    case "RightLeg", "LeftLeg" -> leggingsStack;
//                    case "Head" -> helmetStack;
//                    case "RightForeLeg", "LeftForeLeg" -> bootsStack;
//                    default -> super.getArmorItemForBone(bone, animatable);
//                };
//            }
//
//            @NotNull
//            @Override
//            protected ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, T animatable, HumanoidModel<?> baseModel) {
//                return switch (bone.getName()) {
//                    case "MainBody" -> baseModel.body;
//                    case "LeftArm" -> baseModel.leftArm;
//                    case "RightArm" -> baseModel.rightArm;
//                    case "RightLeg" -> baseModel.rightLeg;
//                    case "LeftLeg" -> baseModel.leftLeg;
//                    case "Head" -> baseModel.head;
//                    default -> super.getModelPartForBone(bone, slot, stack, animatable, baseModel);
//                };
//            }
//        });
    }


    public boolean canRenderMainHandItem(T animatable) {
        return true;
    }

    public boolean canRenderOffHandItem(T animatable) {
        return true;
    }

    @Override
    public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        if (!isReRender) {
            if (mainPose != null) {
                Optional<GeoBone> mainHandBone = model.getBone("MainHandItem");
                if (mainHandBone.isPresent() && canRenderMainHandItem(animatable)) {
                    GeoBone bone = mainHandBone.get();
                    renderItem(bone, poseStack, mainPose, mainNormal, packedLight, bufferSource, true);
                }
            }

            if (offPose != null) {
                Optional<GeoBone> offHandBone = model.getBone("OffHandItem");
                if (offHandBone.isPresent() && canRenderOffHandItem(animatable)) {
                    GeoBone bone = offHandBone.get();
                    renderItem(bone, poseStack, offPose, offNormal, packedLight, bufferSource, false);
                }
            }
        }
    }

    public void renderItem(GeoBone bone, PoseStack poseStack, Matrix4f pose, Matrix3f normal, int packedLight, MultiBufferSource bufferSource, boolean mainHand) {
        poseStack.pushPose();
        poseStack.last().pose().zero().add(pose);
        poseStack.last().normal().zero().add(normal);
        ItemStack itemStack = mainHand ? animatable.getMainHandItem() : animatable.getOffhandItem();
        poseStack.translate(bone.getPivotX() / 16, bone.getPivotY() / 16, bone.getPivotZ() / 16);
        poseStack.mulPose(new Quaternionf().rotateZYX(bone.getRotZ(), bone.getRotY(), bone.getRotX()));

        if (!itemStack.isEmpty()) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            itemRenderer.render(
                    itemStack,
                    mainHand ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                    !mainHand,
                    poseStack,
                    bufferSource,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    itemRenderer.getModel(itemStack, Minecraft.getInstance().level, animatable, 0)
            );
        }
//        else if (mainHand && !(animatable instanceof VolatileEntity)) {
//
//            Minecraft.getInstance().getEntityRenderDispatcher().render(mob, 0, 0, 0, 180, partialTick, poseStack, bufferSource, packedLight);
//        } else {
//            poseStack.translate(-0.25, -0.25, -0.25);
//            poseStack.scale(0.5F, 0.5F, 0.5F);
//            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.ANVIL.defaultBlockState(), poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, net.minecraftforge.client.model.data.ModelData.EMPTY, RenderType.cutout());
//        }

        poseStack.popPose();
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if ("MainHandItem".equals(bone.getName())) {
            mainPose = new Matrix4f(poseStack.last().pose());
            mainNormal = new Matrix3f(poseStack.last().normal());
            bone.setHidden(true);
        } else if ("OffHandItem".equals(bone.getName())) {
            offPose = new Matrix4f(poseStack.last().pose());
            offNormal = new Matrix3f(poseStack.last().normal());
            bone.setHidden(true);
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }


}
