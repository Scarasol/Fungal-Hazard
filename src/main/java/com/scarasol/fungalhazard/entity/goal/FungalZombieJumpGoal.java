package com.scarasol.fungalhazard.entity.goal;

import com.scarasol.fungalhazard.api.IJumpZombie;
import com.scarasol.fungalhazard.entity.AbstractFungalZombie;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieStates;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.JumpGoal;

/**
 * @author Scarasol
 */
public class FungalZombieJumpGoal<T extends AbstractFungalZombie & IJumpZombie> extends JumpGoal {

    private final T zombie;


    public FungalZombieJumpGoal(T zombie) {
        this.zombie = zombie;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        return this.zombie.canJump();
    }

    @Override
    public void start() {
        LivingEntity livingentity = this.zombie.getTarget();
        if (livingentity != null) {
            this.zombie.getLookControl().setLookAt(livingentity, 60.0F, 30.0F);
            this.zombie.setState(FungalZombieStates.JUMP);
            this.zombie.setLastJumpTime(this.zombie.level().getGameTime());
            this.zombie.getNavigation().stop();
        }

    }


}
