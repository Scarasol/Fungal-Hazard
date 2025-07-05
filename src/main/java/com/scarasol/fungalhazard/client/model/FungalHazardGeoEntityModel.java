package com.scarasol.fungalhazard.client.model;

import com.scarasol.fungalhazard.FungalHazardMod;
import com.scarasol.fungalhazard.api.IFungalHazardGeoEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

/**
 * @author Scarasol
 */
public class FungalHazardGeoEntityModel <T extends IFungalHazardGeoEntity> extends GeoModel<T> {

    @Override
    public ResourceLocation getAnimationResource(IFungalHazardGeoEntity entity) {
        return new ResourceLocation(FungalHazardMod.MODID, entity.getAnimation());
    }

    @Override
    public ResourceLocation getModelResource(IFungalHazardGeoEntity entity) {
        return new ResourceLocation(FungalHazardMod.MODID, entity.getModel());
    }

    @Override
    public ResourceLocation getTextureResource(IFungalHazardGeoEntity entity) {
        return new ResourceLocation(FungalHazardMod.MODID, entity.getTexture());
    }


}
