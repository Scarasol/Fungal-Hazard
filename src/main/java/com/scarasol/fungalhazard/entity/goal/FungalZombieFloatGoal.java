package com.scarasol.fungalhazard.entity.goal;

import com.scarasol.fungalhazard.entity.AbstractFungalZombie;
import net.minecraft.world.entity.ai.goal.FloatGoal;

/**
 * @author Scarasol
 */
public class FungalZombieFloatGoal extends FloatGoal {

    private final AbstractFungalZombie zombie;

    public FungalZombieFloatGoal(AbstractFungalZombie mob) {
        super(mob);
        this.zombie = mob;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && (zombie.getState().canMove() && (zombie.isPatrolling() || (this.zombie.getTarget() != null && !this.zombie.getTarget().isUnderWater())));
    }
}
