package com.scarasol.fungalhazard.api;

import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieState;
import com.scarasol.fungalhazard.entity.ai.fsm.StateHandler;
import com.scarasol.fungalhazard.entity.arachnid.AbstractArachnidFungalZombie;
import com.scarasol.fungalhazard.entity.humanoid.AbstractMutilatableZombie;
import com.scarasol.fungalhazard.entity.humanoid.VolatileEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Scarasol
 */
public interface IFungalZombie extends IPatrolMob, IFungalHazardGeoEntity {

    double getAttackDamageModifier();

    double getHealthModifier();

    double getMovementModifier();

    double getArmorModifier();

    double getArmorToughnessModifier();

    default double getAttackRangeModifier() {
        return 1;
    }

    default boolean testAttackable(LivingEntity livingEntity) {
        if (canJoinPatrol()) {
            return !(livingEntity instanceof IPatrolLeader patrolLeader && patrolLeader.canBeLeader());
        }
        return true;
    }

    int getAttackCoolDown();

    void attackAnimation();

    int getAttackAnimeTick();


    void registerStateRunner();

    default void putStateRunner(FungalZombieState state, StateHandler handler) {
        getStateHandlers().put(state, handler);
    }

    @Nonnull
    Map<FungalZombieState, StateHandler> getStateHandlers();

    default void runState() {
        FungalZombieState state = getState();
        StateHandler.StateRunner runner = getStateHandlers().get(state).loopRunner();
        if (runner != null) {
            runner.run(state);
        }
    }

    default void switchState(@Nullable FungalZombieState stateOld, @Nonnull FungalZombieState stateNew) {
        if (!stateNew.equals(stateOld)) {
            StateHandler handler = getStateHandlers().get(stateOld);
            if (handler != null) {
                handler.endRunner().run(stateOld);
            }
            handler = getStateHandlers().get(stateNew);
            if (handler != null) {
                handler.startRunner().run(stateNew);
            }
        }
    }

    FungalZombieState getState();

    void setState(FungalZombieState stateNew);

    void setState(int stateIndex);

    FungalZombieState defaultStates();

    void defaultState(FungalZombieState state);

    double getAttackReachSqr(LivingEntity target);

    void setAnimationTick(int animationTick);

    int getAnimationTick();

    default <T extends IFungalZombie> void animationSwitch(AnimationState<T> event, AnimationController<T> controller, RawAnimation idle, RawAnimation move) {
        if (event.isMoving() || !(event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F)) {
            if ((!controller.isPlayingTriggeredAnimation())) {
                if (event.isCurrentAnimation(idle) || controller.getCurrentRawAnimation() == null || event.getController().hasAnimationFinished()) {
                    event.setAndContinue(move);
                }
            }
        } else {
            if (!controller.isPlayingTriggeredAnimation()) {
                if (event.isCurrentAnimation(move) || controller.getCurrentRawAnimation() == null || event.getController().hasAnimationFinished()) {
                    event.setAndContinue(idle);
                }
            }
        }
    }




}
