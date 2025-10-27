package com.scarasol.fungalhazard.client.renderer;

import com.scarasol.fungalhazard.api.IFungalZombie;
import com.scarasol.fungalhazard.client.model.FungalZombieEntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.model.GeoModel;

public class FungalZombieEntityRenderer<T extends Mob & IFungalZombie> extends FungalHazardGeoEntityRenderer<T> {

    public FungalZombieEntityRenderer(EntityRendererProvider.Context renderManager, boolean glow) {
        this(renderManager, new FungalZombieEntityModel<>(), glow);
    }

    public FungalZombieEntityRenderer(EntityRendererProvider.Context renderManager, GeoModel<T> model, boolean glow) {
        super(renderManager, model, glow);
    }

    @Override
    protected float getDeathMaxRotation(T entityLivingBaseIn) {
        return 0.0F;
    }

    @Override
    public int getPackedOverlay(T animatable, float u) {
        return OverlayTexture.pack(OverlayTexture.u(u), OverlayTexture.v(animatable.hurtTime > 0 && animatable.isAlive()));
    }
}
