package com.scarasol.fungalhazard.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.scarasol.fungalhazard.api.IFungalZombie;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> {

    @Inject(method = "renderArmWithItem", cancellable = true, at = @At("HEAD"))
    private void fungalHazard$Render(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext p_270970_, HumanoidArm p_117188_, PoseStack p_117189_, MultiBufferSource p_117190_, int p_117191_, CallbackInfo ci) {
        if (livingEntity.getVehicle() instanceof IFungalZombie && !(itemStack.getItem() instanceof TieredItem)) {
            ci.cancel();
        }
    }

}
