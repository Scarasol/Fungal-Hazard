package com.scarasol.fungalhazard.entity.goal;

import com.scarasol.fungalhazard.entity.AbstractFungalZombie;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * @author Scarasol
 */
public class FungalZombieNearestAttackableTargetGoal <T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    
    private final AbstractFungalZombie zombie;

    public FungalZombieNearestAttackableTargetGoal(AbstractFungalZombie zombie, Class<T> targetClass, boolean mustSee) {
        super(zombie, targetClass, mustSee);
        this.zombie = zombie;
    }

    public FungalZombieNearestAttackableTargetGoal(AbstractFungalZombie zombie, Class<T> targetClass, boolean mustSee, Predicate<LivingEntity> selector) {
        super(zombie, targetClass, mustSee, selector);
        this.zombie = zombie;
    }

    public FungalZombieNearestAttackableTargetGoal(AbstractFungalZombie zombie, Class<T> targetClass, boolean mustSee, boolean mustReach) {
        super(zombie, targetClass, mustSee, mustReach);
        this.zombie = zombie;
    }

    public FungalZombieNearestAttackableTargetGoal(AbstractFungalZombie zombie, Class<T> targetClass, int checkRate, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> selector) {
        super(zombie, targetClass, checkRate, mustSee, mustReach, selector);
        this.zombie = zombie;
    }

    @Override
    public boolean canUse() {
        return zombie.getState().canTarget() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return zombie.getState().canTarget() && super.canContinueToUse();
    }

}
