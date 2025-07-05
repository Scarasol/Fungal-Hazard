package com.scarasol.fungalhazard.entity.goal;

import com.scarasol.fungalhazard.entity.AbstractFungalZombie;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

/**
 * @author Scarasol
 */
public class FungalZombieWaterAvoidingRandomStrollGoal extends WaterAvoidingRandomStrollGoal {
    protected final AbstractFungalZombie zombie;

    public FungalZombieWaterAvoidingRandomStrollGoal(AbstractFungalZombie pathfinderMob, double d1) {
        super(pathfinderMob, d1);
        this.zombie = pathfinderMob;
    }

    public FungalZombieWaterAvoidingRandomStrollGoal(AbstractFungalZombie pathfinderMob, double d1, float f1) {
        super(pathfinderMob, d1, f1);
        this.zombie = pathfinderMob;
    }

    @Override
    public boolean canUse() {
        return zombie.getState().canMove() && !zombie.isPatrolling() && super.canUse();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, zombie.getState().speedModifier());
    }


}
