package com.scarasol.fungalhazard.entity;

import com.scarasol.fungalhazard.configuration.CommonConfig;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieState;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieStates;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * @author Scarasol
 */
public abstract class AbstractMutilatableZombie extends AbstractFungalZombie{

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    protected static final RawAnimation ATTACK2 = RawAnimation.begin().thenPlay("attack2");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation CREEP = RawAnimation.begin().thenLoop("creep");
    protected static final RawAnimation FALL = RawAnimation.begin().thenPlayAndHold("fall");
    protected static final RawAnimation FALL_TO_CREEP = RawAnimation.begin().thenPlay("fall_to_creep");
    protected static final RawAnimation CREEP_IDLE = RawAnimation.begin().thenLoop("creep_idle");
    protected static final RawAnimation CREEP_ATTACK = RawAnimation.begin().thenPlay("creep_attack");
    protected static final RawAnimation CREEP_ATTACK2 = RawAnimation.begin().thenPlay("creep_attack2");
    protected static final RawAnimation CREEP_DEATH = RawAnimation.begin().thenPlayAndHold("creep_death");

    private static final EntityDimensions CREEP_DIMENSIONS = EntityDimensions.scalable(0.9f, 0.7f);

    public static final EntityDataAccessor<Integer> APPEARANCE = SynchedEntityData.defineId(AbstractMutilatableZombie.class, EntityDataSerializers.INT);

    public static final UUID KNOCK_BACK_UUID = UUID.fromString("7A8893AD-9448-0847-FB7D-F8C5B2D7DBA3");
    public static final AttributeModifier MUTILATION_MODIFIER = new AttributeModifier(KNOCK_BACK_UUID, "MutilationModifier", 1, AttributeModifier.Operation.ADDITION);

    private boolean init;


    public AbstractMutilatableZombie(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public void setAppearance(int appearance) {
        this.entityData.set(APPEARANCE, appearance);
    }

    public int getAppearance() {
        return this.entityData.get(APPEARANCE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(APPEARANCE, 0);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (isAlive()) {
            if (isMutilation()) {
                return CREEP_DIMENSIONS.scale(getScale());
            }
        }
        return super.getDimensions(pose);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Appearance", getAppearance());

    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("Appearance")) {
            setAppearance(compoundTag.getInt("Appearance"));
        }
    }

    @Override
    public void tick() {
        if (!init) {
            int appearance = getAppearance();
            if (appearance == 1) {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            } else if (appearance == 2) {
                this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            }
            init = true;
            refreshDimensions();
        }
        super.tick();
    }

    public void addKnockBackAttributeModifier() {
        AttributeInstance instance = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (instance != null) {
            instance.removeModifier(KNOCK_BACK_UUID);
            instance.addPermanentModifier(MUTILATION_MODIFIER);
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        SpawnGroupData spawnGroupData1 = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
        int appearance = getAppearance();
        ServerLevel serverLevel = serverLevelAccessor.getLevel();
        RandomSource randomSource = serverLevel.random;
        if (randomSource.nextDouble() < CommonConfig.MUTILATION_CHANCE.get()) {
            appearance = randomSource.nextInt(1, 6);
            setAppearance(appearance);
            if (appearance > 2) {
                setState(FungalZombieStates.CREEP);
                addKnockBackAttributeModifier();
                refreshDimensions();
            }
        }
        if (appearance == 1) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        } else if (appearance == 2) {
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
        return spawnGroupData1;
    }

    @Override
    protected void jumpFromGround() {
        if (!isMutilation()) {
            super.jumpFromGround();
        }
    }

    @Override
    public void jumpInFluid(FluidType type) {
        if (!isMutilation()) {
            super.jumpInFluid(type);
        }
    }

    @Override
    public void registerStateRunner() {
        putStateRunner(FungalZombieStates.START_FALL, this::startFallState);
        putStateRunner(FungalZombieStates.FALL, this::fallState);
        putStateRunner(FungalZombieStates.START_CREEP, this::startCreepState);
    }

    public void startFallState(FungalZombieState state) {
        if (isAlive()) {
            setState(FungalZombieStates.FALL);
            addKnockBackAttributeModifier();
            setAnimationTick(fallAnimation());
            this.getNavigation().stop();
            refreshDimensions();
        }
    }

    public void fallState(FungalZombieState state) {
        if (getAnimationTick() == 0 && !level().isClientSide) {
            if (level().random.nextDouble() < 0.01) {
                setAnimationTick(startCreep());
                setState(FungalZombieStates.START_CREEP);
            }
        }
    }

    public void startCreepState(FungalZombieState state) {
        if (getAnimationTick() == 0 && !level().isClientSide) {
            setState(FungalZombieStates.CREEP);
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (!isMutilation()) {
            if (damageSource.isIndirect()) {
                Entity entity = damageSource.getDirectEntity();
                if (entity != null && level().random.nextDouble() < amount * CommonConfig.MUTILATION_COEFFICIENT.get() / getMaxHealth()) {
                    Vec3 sourcePosition = damageSource.getSourcePosition();
                    Vec3 distance = this.position().subtract(sourcePosition);
                    Vec3 velocity = entity.getDeltaMovement();
                    double angle = (Mth.atan2(velocity.y, velocity.horizontalDistance()) * (180 / Math.PI));
                    double horizontalDistance = distance.horizontalDistance() - getBbWidth() / 2;
                    double difference = Math.tan(Math.toRadians(angle)) * horizontalDistance;
                    double hitY = entity.getY() + difference;
                    if (hitY < getOnPos().getY() + 1 + getBbHeight() * 0.35) {
                        setState(FungalZombieStates.START_FALL);
                    }
                }
            }else if (damageSource.is(DamageTypeTags.IS_FALL) && level().random.nextDouble() < amount * 3 / getMaxHealth()) {
                setState(FungalZombieStates.START_FALL);
            }
        }
        return super.hurt(damageSource, amount);
    }

    @Override
    public void attackAnimation() {
        if (getState().equals(FungalZombieStates.CREEP)) {
            triggerAnim("creepAnimationController", getAttackName());
        } else {
            triggerAnim("animationController", getAttackName());
        }
    }

    public String getAttackName() {
        if (getState().equals(FungalZombieStates.CREEP)) {
            if (getAppearance() == 1) {
                return "creep_attack2";
            } else if (getAppearance() == 2) {
                return "creep_attack";
            } else if (!getMainHandItem().isEmpty()) {
                return "creep_attack";
            } else {
                return random.nextDouble() < 0.5 ? "creep_attack" : "creep_attack2";
            }

        } else {
            if (getAppearance() == 1) {
                return "attack2";
            } else if (getAppearance() == 2) {
                return "attack";
            } else if (!getMainHandItem().isEmpty()) {
                return "attack";
            } else {
                return random.nextDouble() < 0.5 ? "attack" : "attack2";
            }
        }
    }

    @Override
    public float getStepHeight() {
        if (getState().equals(FungalZombieStates.CREEP)) {
            return Math.max(super.getStepHeight(), 1);
        }
        return super.getStepHeight();
    }

    public abstract int fallAnimation();

    public abstract int startCreep();

    public abstract boolean isMutilation();

    @Override
    public boolean canJoinPatrol() {
        return !isMutilation();
    }

    public PlayState animationController(AnimationState<AbstractMutilatableZombie> event) {
        AnimationController<AbstractMutilatableZombie> controller = event.getController();
        if (!isMutilation() && this.isAlive()) {
            animationSwitch(event, controller, IDLE, WALK);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    public PlayState creepAnimationController(AnimationState<AbstractMutilatableZombie> event) {
        AnimationController<AbstractMutilatableZombie> controller = event.getController();
        if (isMutilation() && this.isAlive()) {
            animationSwitch(event, controller, CREEP_IDLE, CREEP);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    public void animationSwitch(AnimationState<AbstractMutilatableZombie> event, AnimationController<AbstractMutilatableZombie> controller, RawAnimation idle, RawAnimation move) {
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

    public PlayState deathAnimationController(AnimationState<AbstractMutilatableZombie> event) {
        if (!isAlive()) {
            if (isMutilation()) {
                if (!event.isCurrentAnimation(CREEP_DEATH)) {
                    event.setAndContinue(CREEP_DEATH);
                }
            } else {
                if (!event.isCurrentAnimation(DEATH)) {
                    event.setAndContinue(DEATH);
                }
            }
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }




    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "animationController", 3, this::animationController)
                .triggerableAnim("attack", ATTACK)
                .triggerableAnim("attack2", ATTACK2)
                .receiveTriggeredAnimations());
        data.add(new AnimationController<>(this, "creepAnimationController", 3, this::creepAnimationController)
                .triggerableAnim("creep", CREEP)
                .triggerableAnim("fall", FALL)
                .triggerableAnim("fall_to_creep", FALL_TO_CREEP)
                .triggerableAnim("creep_attack", CREEP_ATTACK)
                .triggerableAnim("creep_attack2", CREEP_ATTACK2)
                .receiveTriggeredAnimations());

        data.add(new AnimationController<>(this, "deathAnimationController", 0, this::deathAnimationController)
                .receiveTriggeredAnimations());
    }

}
