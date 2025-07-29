package com.scarasol.fungalhazard.entity.ai.fsm;

/**
 * @author Scarasol
 */
public record StateHandler(StateRunner startRunner, StateRunner loopRunner, StateRunner endRunner) {

    public static final StateRunner EMPTY_RUNNER = state -> {};

    @FunctionalInterface
    public interface StateRunner {
        void run(FungalZombieState state);
    }
}
