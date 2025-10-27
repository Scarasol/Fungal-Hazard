package com.scarasol.fungalhazard.entity.humanoid;

import com.scarasol.fungalhazard.configuration.CommonConfig;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieState;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieStates;
import com.scarasol.fungalhazard.entity.ai.fsm.StateHandler;
import com.scarasol.fungalhazard.init.FungalHazardEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.network.PlayMessages;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import javax.annotation.Nullable;

/**
 * @author Scarasol
 */
public class InfectedEntity extends AbstractMutilatableZombie {

    protected static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    protected static final RawAnimation WALK2 = RawAnimation.begin().thenLoop("walk2");

    public static final EntityDataAccessor<Boolean> CAN_RUN = SynchedEntityData.defineId(InfectedEntity.class, EntityDataSerializers.BOOLEAN);


    public InfectedEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public InfectedEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(FungalHazardEntities.INFECTED.get(), world);
    }

    public boolean canRun() {
        return getEntityData().get(CAN_RUN);
    }

    public void setCanRun(boolean canRun) {
        getEntityData().set(CAN_RUN, canRun);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CAN_RUN, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("CanRun", canRun());

    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("CanRun")) {
            setCanRun(compoundTag.getBoolean("CanRun"));
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        if (level().random.nextDouble() < CommonConfig.INFECTED_RUN_CHANCE.get()) {
            setCanRun(true);
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override
    public double getAttackDamageModifier() {
        return CommonConfig.INFECTED_ATTACK_DAMAGE.get() - CommonConfig.INFECTED_ATTACK_DAMAGE.getDefault();
    }

    @Override
    public double getHealthModifier() {
        return CommonConfig.INFECTED_HEALTH.get() - CommonConfig.INFECTED_HEALTH.getDefault();
    }

    @Override
    public double getMovementModifier() {
        return CommonConfig.INFECTED_MOVEMENT.get() - CommonConfig.INFECTED_MOVEMENT.getDefault();
    }

    @Override
    public double getArmorModifier() {
        return CommonConfig.INFECTED_ARMOR.get() - CommonConfig.INFECTED_ARMOR.getDefault();
    }

    @Override
    public double getArmorToughnessModifier() {
        return CommonConfig.INFECTED_ARMOR_TOUGHNESS.get() - CommonConfig.INFECTED_ARMOR_TOUGHNESS.getDefault();
    }

    @Override
    protected boolean isSunSensitive() {
        return CommonConfig.INFECTED_BURN_IN_SUN.get() && super.isSunSensitive();
    }

    @Override
    public void registerStateRunner() {
        super.registerStateRunner();
        putStateRunner(FungalZombieStates.RUN, new StateHandler(StateHandler.EMPTY_RUNNER, this::defaultState,StateHandler.EMPTY_RUNNER));
        putStateRunner(FungalZombieStates.CHASING, new StateHandler(StateHandler.EMPTY_RUNNER, this::defaultState,StateHandler.EMPTY_RUNNER));
        putStateRunner(FungalZombieStates.IDLE, new StateHandler(StateHandler.EMPTY_RUNNER, this::defaultState,StateHandler.EMPTY_RUNNER));
    }

    @Override
    public void defaultState(FungalZombieState state) {
        if (!level().isClientSide()) {
            setState(defaultStates());
        }
    }


    @Override
    public String getModel() {
        return "geo/infected.geo.json";
    }

    @Override
    public String getTexture() {
        return "textures/entities/infected1.png";
    }

    @Override
    public String getAnimation() {
        return "animations/infected.animation.json";
    }

    @Override
    public int getAttackCoolDown() {
        return 30;
    }

    @Override
    public void attackAnimation() {
        if (getState().equals(FungalZombieStates.CHASING)) {
            triggerAnim("chasingAnimationController", getAttackName());
        } else {
            super.attackAnimation();
        }
    }

    @Override
    public int getAttackAnimeTick() {
        if (getTarget() instanceof Player) {
            return 15;
        }
        return 0;
    }

    @Override
    public FungalZombieState defaultStates() {
        if (getTarget() != null) {
            if (canRun()) {
                return FungalZombieStates.CHASING;
            }else {
                return FungalZombieStates.RUN;
            }
        } else {
            return FungalZombieStates.IDLE;
        }
    }

    @Override
    public int fallAnimation() {
        triggerAnim("creepAnimationController", "fall");
        return 30;
    }

    @Override
    public int startCreep() {
        triggerAnim("creepAnimationController", "fall_to_creep");
        return 30;
    }

    @Override
    public boolean isMutilation() {
        FungalZombieState state = getState();
        return !state.equals(FungalZombieStates.CHASING) && !state.equals(FungalZombieStates.RUN) && !state.equals(FungalZombieStates.IDLE);
    }

    @Override
    protected void tickDeath() {
        super.tickDeath();
        deathTime++;
        if (this.deathTime >= 70 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(Entity.RemovalReason.KILLED);
        }
        setPose(Pose.DYING);
    }

    @Override
    public PlayState animationController(AnimationState<AbstractMutilatableZombie> event) {
        if (!getState().equals(FungalZombieStates.CHASING)) {
            AnimationController<AbstractMutilatableZombie> controller = event.getController();
            if (!isMutilation() && this.isAlive()) {
                if (!isLeftHanded()) {
                    animationSwitch(event, controller, IDLE, WALK);
                } else {
                    animationSwitch(event, controller, IDLE, WALK2);
                }

                return PlayState.CONTINUE;
            }
        }
        return PlayState.STOP;
    }


    public PlayState chasingAnimationController(AnimationState<AbstractMutilatableZombie> event) {
        if (getState().equals(FungalZombieStates.CHASING)) {
            if (!isMutilation() && isAlive()) {
                AnimationController<AbstractMutilatableZombie> controller = event.getController();
                animationSwitch(event, controller, IDLE, RUN);
                if (event.isCurrentAnimation(RUN)) {
                    controller.setAnimationSpeed(2);
                } else {
                    controller.setAnimationSpeed(1);
                }
                return PlayState.CONTINUE;
            }
        }
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        super.registerControllers(data);
        data.add(new AnimationController<>(this, "chasingAnimationController", 3, this::chasingAnimationController)
                .triggerableAnim("attack", ATTACK)
                .triggerableAnim("attack2", ATTACK2)
                .receiveTriggeredAnimations());
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder.add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.ATTACK_DAMAGE, 3)
                .add(Attributes.FOLLOW_RANGE, 35)
                .add(Attributes.ARMOR, 2)
                .add(Attributes.ARMOR_TOUGHNESS)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        return builder;
    }

}
