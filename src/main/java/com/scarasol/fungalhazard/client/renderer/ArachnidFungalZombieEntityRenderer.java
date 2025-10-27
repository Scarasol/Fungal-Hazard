package com.scarasol.fungalhazard.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.scarasol.fungalhazard.client.model.FungalZombieEntityModel;
import com.scarasol.fungalhazard.entity.arachnid.AbstractArachnidFungalZombie;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;


/**
 * @author Scarasol
 */
public class ArachnidFungalZombieEntityRenderer<T extends AbstractArachnidFungalZombie> extends FungalZombieEntityRenderer<T>{

    public ArachnidFungalZombieEntityRenderer(EntityRendererProvider.Context renderManager, boolean glow) {
        this(renderManager, new FungalZombieEntityModel<>(), glow);
    }

    public ArachnidFungalZombieEntityRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model, boolean glow) {
        super(renderManager, model, glow);
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
