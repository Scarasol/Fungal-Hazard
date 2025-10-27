package com.scarasol.fungalhazard.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.scarasol.fungalhazard.entity.humanoid.AbstractMutilatableZombie;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

/**
 * @author Scarasol
 */
public class MutilatableZombieEntityRenderer <T extends AbstractMutilatableZombie> extends HumanoidFungalZombieEntityRenderer<T> {

    public MutilatableZombieEntityRenderer(EntityRendererProvider.Context renderManager, boolean glow) {
        super(renderManager, glow);
    }

    public MutilatableZombieEntityRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model, boolean glow) {
        super(renderManager, model, glow);
    }

    @Override
    public boolean canRenderMainHandItem(T animatable) {
        return animatable.getAppearance() != 1;
    }

    @Override
    public boolean canRenderOffHandItem(T animatable) {
        return animatable.getAppearance() != 2;
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        int appearance = animatable.getAppearance();
        if (appearance == 1 && "RightArm".equals(bone.getName())) {
            bone.setHidden(true);
        } else if (appearance == 2 && "LeftArm".equals(bone.getName())) {
            bone.setHidden(true);
        } else if (appearance == 3 && "RightLeg".equals(bone.getName())) {
            bone.setHidden(true);
        } else if (appearance == 4 && "LeftLeg".equals(bone.getName())) {
            bone.setHidden(true);
        } else if (appearance == 5 && bone.getName().contains("Leg")) {
            bone.setHidden(true);
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        bone.setHidden(false);
    }
}
