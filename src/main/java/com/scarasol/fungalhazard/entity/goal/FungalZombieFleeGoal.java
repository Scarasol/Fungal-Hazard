package com.scarasol.fungalhazard.entity.goal;

import com.scarasol.fungalhazard.entity.AbstractFungalZombie;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieStates;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * @author Scarasol
 */
public class FungalZombieFleeGoal extends Goal {
    private final AbstractFungalZombie mob;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final Level level;
    @Nullable
    private Vec3 lastFleePos;

    public FungalZombieFleeGoal(AbstractFungalZombie mob) {
        this.mob = mob;
        this.level = mob.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!mob.getState().equals(FungalZombieStates.FLEE) || mob.getTarget() != null) {
            return false;
        } else if (this.mob.isInWater() || this.mob.isVehicle()) {
            return false;
        } else {
            Vec3 vec3 = this.getFleePos();
            if (vec3 == null) {
                return false;
            } else {
                this.wantedX = vec3.x;
                this.wantedY = vec3.y;
                this.wantedZ = vec3.z;
                return true;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return mob.getState().equals(FungalZombieStates.FLEE) && !this.mob.getNavigation().isDone() && !this.mob.isVehicle();
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, mob.getState().speedModifier());
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }

    @Nullable
    private Vec3 getFleePos() {
        if (lastFleePos != null && this.mob.getWalkTargetValue(BlockPos.containing(lastFleePos), level) > 0 && this.mob.distanceToSqr(lastFleePos) < 256) {
            return lastFleePos;
        }
        for(int i = 0; i < 10; ++i) {
            Vec3 target = LandRandomPos.getPos(this.mob, 30, 7);
            if (target != null && this.mob.getWalkTargetValue(BlockPos.containing(target), level) > 0) {
                lastFleePos = target;
                return target;
            }
        }
        return null;
    }


    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

}
