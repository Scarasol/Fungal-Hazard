package com.scarasol.fungalhazard.entity.ai.fsm;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author Scarasol
 */
public class FungalZombieStates {

    public static int INDEX = 0;

    public static final Map<Integer, FungalZombieState> FUNGAL_ZOMBIE_STATES = Maps.newHashMap();

    public static final FungalZombieState IDLE = of(true, true, true, true, true, true, true, 0.7);
    public static final FungalZombieState RUN = of(true, true, true, true, true, true, true, 1.0);
    public static final FungalZombieState CHASING = of(true, true, true, true, true, true, true, 1.3);

    public static final FungalZombieState START_JUMP = of(false, false, false, true, false, true, false, 0.0);
    public static final FungalZombieState JUMP = of(false, false, false, true, false, true, false, 0.0);
    public static final FungalZombieState JUMPING = of(false, false, false, true, false, true, false, 0.0);
    public static final FungalZombieState EXECUTION = of(false, false, false, false, false, true, false, 0.0);
    public static final FungalZombieState GROUND = of(false, false, false, false, false, true, false, 0.0);
    public static final FungalZombieState START_RIDING = of(false, false, false, false, false, true, false, 0.0);
    public static final FungalZombieState RIDING = of(false, false, false, false, false, true, false, 0.0);

    public static final FungalZombieState DODGE = of(false, false, false, true, true, true, false, 0.0);

    public static final FungalZombieState GUARD = of(false, false, true, true, true, true, false, 0.6);

    public static final FungalZombieState START_FALL = of(false, false, false, false, false, false, false, 0.0);
    public static final FungalZombieState FALL = of(false, false, false, false, false, false, false, 0.0);
    public static final FungalZombieState START_CREEP = of(false, false, false, false, false, false, false, 0.0);
    public static final FungalZombieState CREEP = of(false, true, true, true, false, true, true, 0.7);

    public static final FungalZombieState FLEE = of(false, true, true, true, true, false, true, 1.0);



    public static FungalZombieState of(boolean canPatrol, boolean canAttack, boolean canMove, boolean canRot, boolean canControlHead, boolean canTarget, boolean isDefault, double speedModifier) {
        FungalZombieState state = new FungalZombieState(INDEX, canPatrol, canAttack, canMove, canRot, canControlHead, canTarget, isDefault, speedModifier);
        FUNGAL_ZOMBIE_STATES.put(INDEX++, state);
        return state;
    }
}
