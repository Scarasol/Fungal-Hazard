package com.scarasol.fungalhazard.mixin;

import com.scarasol.fungalhazard.api.IFungalZombie;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

/**
 * @author Scarasol
 */
@Mixin(NearestAttackableTargetGoal.class)
public abstract class NearestAttackableTargetGoalMixin extends TargetGoal {

    @Shadow @Nullable protected LivingEntity target;

    public NearestAttackableTargetGoalMixin(Mob mob, boolean mustSee) {
        super(mob, mustSee);
    }

    @Inject(method = "canUse", cancellable = true, at = @At("RETURN"))
    private void fungalHazard$CanUse(CallbackInfoReturnable<Boolean> cir) {
        if (this.target != null && this.mob instanceof IFungalZombie fungalZombie && !fungalZombie.testAttackable(this.target)) {
            cir.setReturnValue(false);
            this.target = null;
        }
    }
}
