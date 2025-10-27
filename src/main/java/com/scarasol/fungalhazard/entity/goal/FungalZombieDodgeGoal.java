package com.scarasol.fungalhazard.entity.goal;

import com.scarasol.fungalhazard.api.IDodgeableZombie;
import com.scarasol.fungalhazard.api.IFungalZombie;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * @author Scarasol
 */
public class FungalZombieDodgeGoal<T extends Mob & IFungalZombie & IDodgeableZombie> extends Goal {
    private final T zombie;


    public FungalZombieDodgeGoal(T zombie) {
        this.zombie = zombie;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        return this.zombie.canDodge();
    }

    @Override
    public void start() {
        this.zombie.dodge();
        this.zombie.getNavigation().stop();
    }
}
