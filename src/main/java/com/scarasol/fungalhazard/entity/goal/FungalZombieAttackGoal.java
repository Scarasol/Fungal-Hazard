package com.scarasol.fungalhazard.entity.goal;

import com.scarasol.fungalhazard.entity.AbstractFungalZombie;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * @author Scarasol
 */
public class FungalZombieAttackGoal extends MeleeAttackGoal {

    private final AbstractFungalZombie zombie;
    private int currentAnimationTick;
    private boolean startAttack;
    private long lastAttackTime;


    public FungalZombieAttackGoal(AbstractFungalZombie zombie, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(zombie, speedModifier, followingTargetEvenIfNotSeen);
        this.zombie = zombie;
    }

    @Override
    public boolean canUse() {
        return zombie.getState().canAttack() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return  (this.mob.isAlive() && startAttack && this.mob.getTarget() != null && zombie.getState().canAttack()) || super.canContinueToUse();
    }

    @Override
    public void start() {
        this.currentAnimationTick = zombie.getAttackAnimeTick();
        this.startAttack = false;
        super.start();
    }

    @Override
    public void tick() {
        LivingEntity target = this.zombie.getTarget();
        if (target != null) {
            double d0 = this.zombie.distanceToSqr(target);
            if (d0 < 25) {
                this.zombie.setPatrolling(false);
            }
            super.tick();
            if (d0 <= getAttackReachSqr(target) / 4) {
                this.zombie.getNavigation().stop();
            }
        }
    }

    @Override
    protected boolean isTimeToAttack() {
        return this.zombie.level().getGameTime() - lastAttackTime > this.zombie.getAttackCoolDown();
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target, double distanceSqr) {
        double d0 = this.getAttackReachSqr(target);
        if (distanceSqr <= d0 && isTimeToAttack() && !startAttack) {
            this.zombie.attackAnimation();
            startAttack = true;
        }
        if (startAttack) {
            currentAnimationTick = Math.max(0, currentAnimationTick - 1);
            if (currentAnimationTick <= 0) {
                if (distanceSqr <= d0) {
                    this.mob.doHurtTarget(target);
                }
                lastAttackTime = zombie.level().getGameTime();
                this.currentAnimationTick = zombie.getAttackAnimeTick();
                this.startAttack = false;
            }
        }
    }

    @Override
    protected double getAttackReachSqr(LivingEntity target) {
        return this.zombie.getAttackReachSqr(target);
    }


}
