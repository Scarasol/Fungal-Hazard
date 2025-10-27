package com.scarasol.fungalhazard.entity.arachnid;

import com.scarasol.fungalhazard.FungalHazardMod;
import com.scarasol.fungalhazard.api.IFungalZombie;
import com.scarasol.fungalhazard.api.IJumpZombie;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieState;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieStates;
import com.scarasol.fungalhazard.entity.ai.fsm.StateHandler;
import com.scarasol.fungalhazard.entity.goal.FungalZombieAttackGoal;
import com.scarasol.fungalhazard.entity.goal.FungalZombieJumpGoal;
import com.scarasol.fungalhazard.entity.goal.FungalZombieNearestAttackableTargetGoal;
import com.scarasol.fungalhazard.entity.goal.FungalZombieWaterAvoidingRandomStrollGoal;
import com.scarasol.fungalhazard.init.FungalHazardEntities;
import com.scarasol.fungalhazard.init.FungalHazardTags;
import com.scarasol.sona.init.SonaMobEffects;
import com.scarasol.sona.manager.InfectionManager;
import com.scarasol.sona.util.SonaMath;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
public class LurkerEntity extends AbstractArachnidFungalZombie implements IJumpZombie {

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation DEATH1 = RawAnimation.begin().thenPlayAndHold("death1");
    protected static final RawAnimation DEATH2 = RawAnimation.begin().thenPlayAndHold("death2");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation START_JUMP = RawAnimation.begin().thenPlayAndHold("start_jump");
    protected static final RawAnimation START_RIDING = RawAnimation.begin().thenPlay("start_riding");
    protected static final RawAnimation RIDING = RawAnimation.begin().thenLoop("riding");

    private static final EntityDimensions RIDING_DIMENSIONS = EntityDimensions.scalable(0.5f, 0.5f);

    private long lastJumpTime = 0L;

    public LurkerEntity(EntityType<? extends Spider> entityType, Level level) {
        super(entityType, level);
    }

    public LurkerEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(FungalHazardEntities.LURKER.get(), world);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (isAlive()) {
            if (getState().equals(FungalZombieStates.RIDING)) {
                return RIDING_DIMENSIONS.scale(getScale());
            }
        }
        return super.getDimensions(pose);
    }

    @Override
    public boolean onGround() {
        if (!level().isClientSide() && isVehicle()) {
            return getFirstPassenger().onGround();
        }
        return super.onGround();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new FungalZombieAttackGoal<>(this, 1.0D, false));
        this.goalSelector.addGoal(5, new FungalZombieWaterAvoidingRandomStrollGoal<>(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new FungalZombieJumpGoal<>(this));
        this.targetSelector.addGoal(2, new FungalZombieNearestAttackableTargetGoal<>(this, Player.class, true, this::testAttackable));
        this.targetSelector.addGoal(3, new FungalZombieNearestAttackableTargetGoal<>(this, AbstractVillager.class, false, this::testAttackable));
        this.targetSelector.addGoal(3, new FungalZombieNearestAttackableTargetGoal<>(this, IronGolem.class, true, this::testAttackable));
        this.targetSelector.addGoal(5, new FungalZombieNearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, livingEntity -> testAttackable(this) && Turtle.BABY_ON_LAND_SELECTOR.test(this)));
    }

    @Override
    public boolean testAttackable(LivingEntity livingEntity) {
        return !(livingEntity instanceof Mob mob) || mob.getMobType() != MobType.UNDEAD;
    }

    @Override
    public String getModel() {
        return "geo/lurker.geo.json";
    }

    @Override
    public String getTexture() {
        return "textures/entities/lurker.png";
    }

    @Override
    public String getAnimation() {
        return "animations/lurker.animation.json";
    }

    @Override
    public double getAttackDamageModifier() {
        return 0;
    }

    @Override
    public double getHealthModifier() {
        return 0;
    }

    @Override
    public double getMovementModifier() {
        return 0;
    }

    @Override
    public double getArmorModifier() {
        return 0;
    }

    @Override
    public double getArmorToughnessModifier() {
        return 0;
    }

    @Override
    public int getAttackCoolDown() {
        return 12;
    }

    @Override
    public void attackAnimation() {

    }

    @Override
    public boolean canRiderInteract() {
        return true;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        return null;
    }

    @Override
    protected void positionRider(Entity entity, Entity.MoveFunction moveFunction) {
        if (this.hasPassenger(entity)) {
            Vec3 angle = new Vec3(getLookAngle().x, 0, getLookAngle().z).normalize().scale(0.4);
            moveFunction.accept(entity, this.getX() + angle.x, this.getY() - entity.getEyeHeight(entity.getPose()) + getBbHeight() / 4, this.getZ() + angle.z);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return passenger.position();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isVehicle()) {
            Entity rider = this.getFirstPassenger();
            if (rider != null) {
                boolean riderTouchingGround = isEntityTouchingGround(rider);
                if (riderTouchingGround) {
                    Vec3 motion = this.getDeltaMovement();
                    if (motion.y < 0.0) {
                        this.setDeltaMovement(motion.x, 0.0, motion.z);
                    }
                    this.setOnGround(true);
                    this.fallDistance = 0.0F;
                } else {
                    this.setOnGround(false);
                }

            }
        }
    }


    private boolean isEntityTouchingGround(Entity entity) {
        AABB test = entity.getBoundingBox().move(0.0, -0.05, 0.0);
        return !this.level().noCollision(test);
    }


    @Override
    protected void tickDeath() {
        super.tickDeath();
        deathTime++;
        if (isVehicle()) {
            getFirstPassenger().stopRiding();
        }
        if (this.deathTime >= 40 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(Entity.RemovalReason.KILLED);
        }
        setPose(Pose.DYING);
    }

    @Override
    public int getAttackAnimeTick() {
        return 0;
    }

    @Override
    public boolean canJump() {
        LivingEntity livingEntity = getTarget();
        return getState().canMove() && level().getGameTime() - lastJumpTime > 100 && livingEntity != null && livingEntity.distanceToSqr(livingEntity) <= 25 && getSensing().hasLineOfSight(livingEntity);
    }

    @Override
    public void setLastJumpTime(long lastJumpTime) {
        this.lastJumpTime = lastJumpTime;
    }

    @Override
    public void push(Entity target) {
        if (getState().equals(FungalZombieStates.JUMPING) && target instanceof LivingEntity livingEntity && !(target.getVehicle() instanceof IFungalZombie) && InfectionManager.canBeInfected(livingEntity)) {
            if (livingEntity.getUseItem().is(FungalHazardTags.SHIELD)) {
                level().playSound(null, this, SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1, 1);
                livingEntity.stopUsingItem();
                setDeltaMovement(Vec3.ZERO);
                setState(defaultStates());
            } else {
                targetRiding(livingEntity);
            }
        } else {
            super.push(target);
        }
    }

    public void targetRiding(LivingEntity target) {
        target.setYRot(Mth.wrapDegrees(getYRot() + 180));
        target.stopUsingItem();
        target.setDeltaMovement(Vec3.ZERO);
        setDeltaMovement(Vec3.ZERO);
        target.startRiding(this);
        setState(FungalZombieStates.RIDING);
        if (isEntityTouchingGround(target)) {
            setPos(getX(), target.getY() + target.getEyeHeight(target.getPose()) + target.getBbHeight() * 0.5, getZ());
            this.setOnGround(true);
            this.fallDistance = 0.0F;
        }
    }

    public boolean targetJump(LivingEntity target) {
        lookAt(target, getMaxHeadXRot(), getMaxHeadYRot());
        double vMax = 5.0;
        Vec3 jump = getMinimalJumpVec(this, target, vMax);
        if (!jump.equals(Vec3.ZERO)) {
            this.setDiscardFriction(true);
            this.setDeltaMovement(jump);
            this.setState(FungalZombieStates.JUMPING);
            return true;
        }
        return false;
    }

    @Override
    public void registerStateRunner() {
        putStateRunner(FungalZombieStates.RUN, new StateHandler(StateHandler.EMPTY_RUNNER, this::defaultState, StateHandler.EMPTY_RUNNER));
        putStateRunner(FungalZombieStates.IDLE, new StateHandler(StateHandler.EMPTY_RUNNER, this::defaultState, StateHandler.EMPTY_RUNNER));
        putStateRunner(FungalZombieStates.RIDING, new StateHandler(this::startRidingState, this::ridingState, this::endRidingState));
        putStateRunner(FungalZombieStates.JUMP, new StateHandler(this::startJumpState, this::jumpState, StateHandler.EMPTY_RUNNER));
        putStateRunner(FungalZombieStates.JUMPING, new StateHandler(StateHandler.EMPTY_RUNNER, this::jumpingState, this::endJumpingState));
    }

    private void startJumpState(FungalZombieState state) {
        if (!level().isClientSide()) {
            triggerAnim("animationController", "start_jump");
            setAnimationTick(20);
        }
    }

    private void jumpState(FungalZombieState state) {
        if (!level().isClientSide() && getAnimationTick() == 0) {
            LivingEntity target = this.getTarget();
            if (target != null && targetJump(target)) {
                return;
            }
            this.setState(defaultStates());
        }
    }

    private void jumpingState(FungalZombieState state) {
        if (!level().isClientSide()) {
            if (onGround() || isInFluidType()) {
                setState(defaultStates());
            } else {
                LivingEntity target = getTarget();
                if (target != null) {
                    Vec3 vector = target.getEyePosition().subtract(this.position());
                    if (SonaMath.vectorDegreeCalculate(vector, getDeltaMovement()) < 60) {
                        double disSqr = distanceToSqr(target);
                        if (disSqr < 0.64) {
                            targetRiding(target);
                        }
                    }
                }
            }
        }
    }

    private void endJumpingState(FungalZombieState state) {
        setDiscardFriction(false);
    }

    private void endRidingState(FungalZombieState state) {
        if (isVehicle()) {
            getFirstPassenger().stopRiding();
        }
        refreshDimensions();
    }

    private void ridingState(FungalZombieState state) {
        if (!level().isClientSide() && (level().getGameTime() + getId()) % 20 == 0) {
            Entity entity = getFirstPassenger();
            if (entity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.INFECTION.get(), 100, 3, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1, false, false));
            } else {
                setState(defaultStates());
            }
        }
    }

    private void startRidingState(FungalZombieState state) {
        Entity entity = getFirstPassenger();
        if (entity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
            triggerAnim("ridingAnimationController", "start_riding");
            setAnimationTick(25);
            livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.INFECTION.get(), 100, 3, false, false));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1, false, false));
            if (entity instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 1, true, false));
                player.resetAttackStrengthTicker();
            } else {
                livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.STUN.get(), 20, 0, false, false));
            }
        } else {
            if (!level().isClientSide()) {
                setState(defaultStates());
            }
        }


        refreshDimensions();
    }

    @Override
    public void defaultState(FungalZombieState state) {
        if (!level().isClientSide()) {
            setState(defaultStates());
        }
    }

    @Override
    public FungalZombieState defaultStates() {
        if (getTarget() != null) {
            return FungalZombieStates.RUN;
        } else {
            return FungalZombieStates.IDLE;
        }
    }


    @Override
    public boolean canJoinPatrol() {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder.add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, 8)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.FOLLOW_RANGE, 16)
                .add(Attributes.ARMOR, 2)
                .add(Attributes.ARMOR_TOUGHNESS);
        return builder;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "animationController", 3, this::animationController)
                .triggerableAnim("start_jump", START_JUMP)
                .receiveTriggeredAnimations());
        data.add(new AnimationController<>(this, "deathAnimationController", 0, this::deathAnimationController)
                .receiveTriggeredAnimations());
        data.add(new AnimationController<>(this, "ridingAnimationController", 0, this::ridingAnimationController)
                .triggerableAnim("start_riding", START_RIDING)
                .receiveTriggeredAnimations());
    }

    public PlayState animationController(AnimationState<LurkerEntity> event) {
        AnimationController<LurkerEntity> controller = event.getController();
        if (this.isAlive() && !this.isVehicle()) {
            if (this.onGround()) {
                if (event.isCurrentAnimation(START_JUMP)) {
                    event.setAndContinue(WALK);
                    return PlayState.STOP;
                }
                animationSwitch(event, controller, IDLE, WALK);
            }
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private PlayState deathAnimationController(AnimationState<LurkerEntity> event) {
        if (!isAlive()) {
            if (!event.isCurrentAnimation(DEATH1) && !event.isCurrentAnimation(DEATH2)) {
                if (getState().equals(RIDING)) {
                    event.setAndContinue(DEATH2);
                } else {
                    event.setAndContinue(DEATH1);
                }
            }

            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private PlayState ridingAnimationController(AnimationState<LurkerEntity> event) {
        AnimationController<LurkerEntity> controller = event.getController();

        if (isAlive() && isVehicle()) {
            if (!controller.isPlayingTriggeredAnimation()) {
                if (getState().equals(FungalZombieStates.RIDING)) {
                    if (controller.getCurrentRawAnimation() == null || event.getController().hasAnimationFinished()) {
                        event.setAndContinue(RIDING);
                    }
                }
            }
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }


}
