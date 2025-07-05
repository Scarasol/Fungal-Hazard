package com.scarasol.fungalhazard.client.model;

import com.scarasol.fungalhazard.api.IFungalHazardGeoEntity;
import com.scarasol.fungalhazard.entity.AbstractFungalZombie;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

/**
 * @author Scarasol
 */
public class FungalZombieEntityModel <T extends AbstractFungalZombie> extends FungalHazardGeoEntityModel<T> {

    @Override
    public void setCustomAnimations(AbstractFungalZombie animatable, long instanceId, AnimationState animationState) {
        CoreGeoBone head = getAnimationProcessor().getBone("Head");
        if (head != null && animatable.getState().canControlHead()) {
            EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
