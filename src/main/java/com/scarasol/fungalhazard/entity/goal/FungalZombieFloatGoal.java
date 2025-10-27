package com.scarasol.fungalhazard.entity.goal;

import com.scarasol.fungalhazard.api.IFungalZombie;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;

/**
 * @author Scarasol
 */
public class FungalZombieFloatGoal<T extends PathfinderMob & IFungalZombie> extends FloatGoal {

    private final T zombie;

    public FungalZombieFloatGoal(T mob) {
        super(mob);
        this.zombie = mob;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && (zombie.getState().canMove() && (zombie.isPatrolling() || (this.zombie.getTarget() != null && !this.zombie.getTarget().isUnderWater())));
    }
}
