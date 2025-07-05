package com.scarasol.fungalhazard.entity.ai.fsm;

/**
 * @author Scarasol
 */
public class FungalZombieState {
    private final int index;
    private final boolean canPatrol;
    private final boolean canAttack;
    private final boolean canMove;
    private final boolean canRot;
    private final boolean canControlHead;
    private final boolean canTarget;
    private final boolean isDefault;
    private final double speedModifier;



    FungalZombieState(int index, boolean canPatrol, boolean canAttack, boolean canMove, boolean canRot, boolean canControlHead, boolean canTarget, boolean isDefault, double speedModifier) {
        this.index = index;
        this.canPatrol = canPatrol;
        this.canAttack = canAttack;
        this.canMove = canMove;
        this.canRot = canRot;
        this.canControlHead = canControlHead;
        this.canTarget = canTarget;
        this.isDefault = isDefault;
        this.speedModifier = speedModifier;
    }

    public int index() {
        return index;
    }

    public boolean canPatrol() {
        return canPatrol;
    }

    public boolean canAttack() {
        return canAttack;
    }

    public boolean canMove() {
        return canMove;
    }

    public boolean canRot() {
        return canRot;
    }

    public boolean canControlHead() {
        return canControlHead;
    }

    public boolean canTarget() {
        return canTarget;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public double speedModifier() {
        return speedModifier;
    }
}
