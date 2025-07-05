package com.scarasol.fungalhazard.entity.goal;

import com.scarasol.fungalhazard.api.IGuardableZombie;
import com.scarasol.fungalhazard.entity.AbstractFungalZombie;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieStates;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * @author Scarasol
 */
public class FungalZombieGuardGoal<T extends AbstractFungalZombie & IGuardableZombie> extends Goal {

    private final T zombie;
    private int times;

    public FungalZombieGuardGoal(T zombie) {
        this.zombie = zombie;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        times = ++times % 5;
        if (times == 0) {
            return this.zombie.canGuard();
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.zombie.getTarget() != null && this.zombie.getState().equals(FungalZombieStates.GUARD);
    }

    @Override
    public void start() {
        this.zombie.setState(FungalZombieStates.GUARD);
        this.zombie.setAnimationTick(40);
        this.zombie.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.zombie.getTarget();
        if (target != null) {
            this.zombie.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distanceSqr = this.zombie.distanceToSqr(target);
            double attackReachSqr = this.zombie.getAttackReachSqr(target);
            if (distanceSqr > attackReachSqr) {
                this.zombie.getNavigation().moveTo(target, this.zombie.getState().speedModifier());
            } else if (distanceSqr <= attackReachSqr / 4) {
                this.zombie.getNavigation().stop();
            }
        }

    }

    public static double getTargetAttackReachSqr(AbstractFungalZombie zombie, LivingEntity target) {
        return target.getBbWidth() * 2 * target.getBbWidth() * 2 * 1.44 + zombie.getBbWidth();
    }

}
