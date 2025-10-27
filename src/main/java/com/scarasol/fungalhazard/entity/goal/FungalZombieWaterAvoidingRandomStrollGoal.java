package com.scarasol.fungalhazard.entity.goal;

import com.scarasol.fungalhazard.api.IFungalZombie;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

/**
 * @author Scarasol
 */
public class FungalZombieWaterAvoidingRandomStrollGoal<T extends PathfinderMob & IFungalZombie> extends WaterAvoidingRandomStrollGoal {
    protected final T zombie;

    public FungalZombieWaterAvoidingRandomStrollGoal(T pathfinderMob, double d1) {
        super(pathfinderMob, d1);
        this.zombie = pathfinderMob;
    }

    public FungalZombieWaterAvoidingRandomStrollGoal(T pathfinderMob, double d1, float f1) {
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
