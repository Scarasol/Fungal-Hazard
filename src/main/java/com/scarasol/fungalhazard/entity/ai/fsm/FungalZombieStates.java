package com.scarasol.fungalhazard.entity.ai.fsm;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author Scarasol
 */
public class FungalZombieStates {

    public static int INDEX = 0;

    public static final Map<Integer, FungalZombieState> FUNGAL_ZOMBIE_STATES = Maps.newHashMap();

    public static final FungalZombieState IDLE = of("idle", true, true, true, true, true, true, true, 0.7);
    public static final FungalZombieState RUN = of("run", true, true, true, true, true, true, true, 1.0);
    public static final FungalZombieState CHASING = of("chasing", true, true, true, true, true, true, true, 1.3);

    public static final FungalZombieState JUMP = of("jump", false, false, false, true, false, true, false, 0.0);
    public static final FungalZombieState JUMPING = of("jumping", false, false, false, true, false, true, false, 0.0);
    public static final FungalZombieState EXECUTION = of("execution", false, false, false, false, false, true, false, 0.0);
    public static final FungalZombieState GROUND = of("ground", false, false, false, false, false, true, false, 0.0);
    public static final FungalZombieState RIDING = of("riding", false, false, false, false, false, true, false, 0.0);

    public static final FungalZombieState DODGE = of("dodge", false, false, false, true, true, true, false, 0.0);

    public static final FungalZombieState GUARD = of("guard", false, false, true, true, true, true, false, 0.6);

    public static final FungalZombieState FALL = of("fall", false, false, false, false, false, false, false, 0.0);
    public static final FungalZombieState CREEP = of("creep", false, true, true, true, false, true, true, 0.7);

    public static final FungalZombieState FLEE = of("flee", false, true, true, true, true, false, true, 1.0);



    public static FungalZombieState of(String name, boolean canPatrol, boolean canAttack, boolean canMove, boolean canRot, boolean canControlHead, boolean canTarget, boolean isDefault, double speedModifier) {
        FungalZombieState state = new FungalZombieState(INDEX, name, canPatrol, canAttack, canMove, canRot, canControlHead, canTarget, isDefault, speedModifier);
        FUNGAL_ZOMBIE_STATES.put(INDEX++, state);
        return state;
    }
}
