package com.scarasol.fungalhazard.client.renderer;

import com.scarasol.fungalhazard.api.IFungalHazardGeoEntity;
import com.scarasol.fungalhazard.client.model.FungalHazardGeoEntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * @author Scarasol
 */
public class FungalHazardGeoEntityRenderer<T extends Entity & IFungalHazardGeoEntity> extends GeoEntityRenderer<T> {

    public FungalHazardGeoEntityRenderer(EntityRendererProvider.Context renderManager, boolean glow) {
        this(renderManager, new FungalHazardGeoEntityModel<>(), glow);
    }

    public FungalHazardGeoEntityRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model, boolean glow) {
        super(renderManager, model);
        this.shadowRadius = 0.5f;
        if (glow) {
            addRenderLayer(new AutoGlowingGeoLayer<>(this));
        }
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

}
