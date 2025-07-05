package com.scarasol.fungalhazard.mixin;

import com.scarasol.fungalhazard.entity.AbstractFungalZombie;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

/**
 * @author Scarasol
 */
@Mixin(Entity.class)
public abstract class EntityMixin {


    @Shadow @Nullable public abstract Entity getVehicle();

    @Shadow private float yRot;

//    @Inject(method = "getRootVehicle", cancellable = true, at = @At("RETURN"))
//    private void fungalHazard$GetRootVehicle(CallbackInfoReturnable<Entity> cir) {
//        if (cir.getReturnValue() instanceof AbstractFungalZombie && !((Entity) ((Object)this) instanceof AbstractFungalZombie)) {
//            cir.setReturnValue((Entity) ((Object)this));
//        }
//    }

    @Inject(method = "setYRot", cancellable = true, at = @At("HEAD"))
    private void fungalHazard$SetYRot(float yRot, CallbackInfo ci) {
        if (getVehicle() instanceof AbstractFungalZombie abstractFungalZombie && !((Entity) ((Object)this) instanceof AbstractFungalZombie)) {
            this.yRot = Mth.wrapDegrees(abstractFungalZombie.getYRot() + 180);
            ci.cancel();
        }
    }
}
