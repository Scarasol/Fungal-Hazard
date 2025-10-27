package com.scarasol.fungalhazard.entity.goal;

import com.scarasol.fungalhazard.api.IFungalZombie;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * @author Scarasol
 */
public class FungalZombieNearestAttackableTargetGoal <T extends LivingEntity, M extends Mob & IFungalZombie> extends NearestAttackableTargetGoal<T> {
    
    private final M zombie;

    public FungalZombieNearestAttackableTargetGoal(M zombie, Class<T> targetClass, boolean mustSee) {
        super(zombie, targetClass, mustSee);
        this.zombie = zombie;
    }

    public FungalZombieNearestAttackableTargetGoal(M zombie, Class<T> targetClass, boolean mustSee, Predicate<LivingEntity> selector) {
        super(zombie, targetClass, mustSee, selector);
        this.zombie = zombie;
    }

    public FungalZombieNearestAttackableTargetGoal(M zombie, Class<T> targetClass, boolean mustSee, boolean mustReach) {
        super(zombie, targetClass, mustSee, mustReach);
        this.zombie = zombie;
    }

    public FungalZombieNearestAttackableTargetGoal(M zombie, Class<T> targetClass, int checkRate, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> selector) {
        super(zombie, targetClass, checkRate, mustSee, mustReach, selector);
        this.zombie = zombie;
    }

    @Override
    public boolean canUse() {
        if (zombie.isPatrolling() && !zombie.isPatrolLeader()) {
            if (zombie.getRandom().nextDouble() > 0.2) {
                return false;
            }
        }
        return zombie.getState().canTarget() && super.canUse();
    }

    public void notifyOthers(LivingEntity target) {
        zombie.level().getEntities(zombie, zombie.getBoundingBox().inflate(16), (Entity entity) -> entity instanceof IFungalZombie fungalZombie && fungalZombie.isPatrolling())
                .forEach(entity -> {
                    if (entity instanceof Mob mob && (mob.getTarget() == null || zombie.isPatrolLeader())) {
                        mob.setTarget(target);
                    }
                });
    }

    @Override
    protected void findTarget() {
        super.findTarget();
        if (this.zombie.isPatrolling() && this.target != null) {
            notifyOthers(target);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return zombie.getState().canTarget() && super.canContinueToUse();
    }

}
