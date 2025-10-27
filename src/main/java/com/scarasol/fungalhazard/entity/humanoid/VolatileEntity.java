package com.scarasol.fungalhazard.entity.humanoid;

import com.scarasol.fungalhazard.api.IDodgeableZombie;
import com.scarasol.fungalhazard.api.IFungalZombie;
import com.scarasol.fungalhazard.api.IGuardableZombie;
import com.scarasol.fungalhazard.api.IJumpZombie;
import com.scarasol.fungalhazard.configuration.CommonConfig;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieState;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieStates;
import com.scarasol.fungalhazard.entity.ai.fsm.StateHandler;
import com.scarasol.fungalhazard.entity.goal.*;
import com.scarasol.fungalhazard.init.FungalHazardEntities;
import com.scarasol.fungalhazard.init.FungalHazardSounds;
import com.scarasol.fungalhazard.init.FungalHazardTags;
import com.scarasol.fungalhazard.network.NetworkHandler;
import com.scarasol.fungalhazard.network.VolatileGuardPacket;
import com.scarasol.sona.effect.PhysicalEffect;
import com.scarasol.sona.entity.SoundDecoy;
import com.scarasol.sona.init.SonaMobEffects;
import com.scarasol.sona.util.SonaMath;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * @author Scarasol
 */
public class VolatileEntity extends AbstractHumanoidFungalZombie implements IDodgeableZombie, IGuardableZombie, IJumpZombie {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation ATTACK2 = RawAnimation.begin().thenPlay("attack2");
    private static final RawAnimation PARRY = RawAnimation.begin().thenPlayAndHold("parry");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SPRINT = RawAnimation.begin().thenLoop("sprint");
    private static final RawAnimation START_JUMP = RawAnimation.begin().thenPlayAndHold("start_jump");
    private static final RawAnimation GROUND = RawAnimation.begin().thenPlay("ground");
    private static final RawAnimation RIDING = RawAnimation.begin().thenLoop("riding");
    private static final RawAnimation STAGGER = RawAnimation.begin().thenPlay("stagger");
    private static final RawAnimation GUARD = RawAnimation.begin().thenLoop("guard");
    private static final RawAnimation GUARD_WALK = RawAnimation.begin().thenLoop("guard_walk");
    private static final RawAnimation EXECUTION = RawAnimation.begin().thenLoop("execution");
    private static final RawAnimation START_DODGE = RawAnimation.begin().thenPlayAndHold("start_dodge");
    private static final RawAnimation EXECUTION_GROUND = RawAnimation.begin().thenPlay("execution_ground");

    private static final EntityDimensions RIDING_DIMENSIONS = EntityDimensions.scalable(0.9f, 2.5f);

    private boolean stagger;

    private int dodgeCount;
    private int direction;
    private long lastDodgeTime;

    private float damageInRiding;
    private int ridingTick;

    private long lastGuardTime;
    private int guardCount;
    private boolean canGuard = true;
    private boolean successGuard;

    private double lastHurtAmount;
    private long lastSoundTime;
    private int nextSoundTime = 30 + new Random().nextInt(20);

    private long lastJumpTime;

    private boolean init;


    public VolatileEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(FungalHazardEntities.VOLATILE.get(), world);
    }

    public VolatileEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public String getModel() {
        return "geo/volatile.geo.json";
    }

    @Override
    public String getTexture() {
        return "textures/entities/volatile.png";
    }

    @Override
    public String getAnimation() {
        return "animations/volatile.animation.json";
    }

    @Override
    public int getDodgeCount() {
        return dodgeCount;
    }

    public boolean isSuccessGuard() {
        return successGuard;
    }

    public void setSuccessGuard(boolean successGuard) {
        this.successGuard = successGuard;
    }

    public boolean isCanGuard() {
        return canGuard;
    }

    public void setCanGuard(boolean canGuard) {
        this.canGuard = canGuard;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (getState().equals(FungalZombieStates.IDLE)) {
            return FungalHazardSounds.VOLATILE_IDLE.get();
        } else if (getState().equals(FungalZombieStates.RUN)) {
            return FungalHazardSounds.VOLATILE_SPRINT.get();
        }
        return SoundEvents.EMPTY;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return FungalHazardSounds.VOLATILE_DEATH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        long gameTime = level().getGameTime();
        if (gameTime - lastSoundTime > nextSoundTime) {
            nextSoundTime = 30 + level().random.nextInt(20);
            lastSoundTime = gameTime;
            return lastHurtAmount < getMaxHealth() * 0.25 ? FungalHazardSounds.VOLATILE_HURT_LIGHT.get() : FungalHazardSounds.VOLATILE_HURT_HEAVY.get();
        }
        return SoundEvents.EMPTY;
    }

    public void setDodgeCount(int dodgeCount) {
        this.dodgeCount = dodgeCount;
    }

    public void setLastDodgeTime(long lastDodgeTime) {
        this.lastDodgeTime = lastDodgeTime;
    }

    @Override
    public boolean canJump() {
        if (!getState().canMove()) {
            return false;
        }
        LivingEntity target = this.getTarget();
        if (target == null || target instanceof SoundDecoy || !target.isAlive() || target.getVehicle() instanceof IFungalZombie) {
            return false;
        }
        if (Math.abs(target.getY() - getY()) > 6) {
            return false;
        }
        double distanceSqr = target.distanceToSqr(this);
        return distanceSqr <= 144 && distanceSqr >= 36 && this.getSensing().hasLineOfSight(target) && canJumpWithoutDistance();
    }

    public boolean canJumpWithoutDistance() {
        return this.onGround() && this.level().getGameTime() - this.lastJumpTime > CommonConfig.VOLATILE_EXECUTION_COOLDOWN.get() * 20;
    }

    public double jumpToTime(double vy, double y) {
        AttributeInstance gravityAttribute = this.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        double time = -1;
        if (gravityAttribute != null) {
            double gravity = gravityAttribute.getValue();
            double sqrt = Math.sqrt(vy * vy + 8 * gravity * y);
            if (Double.isNaN(sqrt)) {
                return time;
            }
            if (y >= 0) {
                time = (vy + sqrt) / (2 * gravity);
            } else {
                time = (vy - sqrt) / (2 * gravity);
            }
        }
        return time;
    }

    @Override
    public void setLastJumpTime(long lastJumpTime) {
        this.lastJumpTime = lastJumpTime;
    }

    @Override
    public boolean dodge() {
        LivingEntity target = this.getTarget();
        if (target != null) {
            this.getLookControl().setLookAt(target, 60.0F, 30.0F);
            Vec3 jumpDirection;
            Vec3 vec3 = target.position().subtract(this.position());
            vec3 = vec3.subtract(0, vec3.y, 0).normalize();
            if (getState().equals(FungalZombieStates.GUARD)) {
                jumpDirection = vec3.scale(-1);
                if (!canDodge(jumpDirection)) {
                    lastDodgeTime = level().getGameTime();
                    return false;
                }
            } else {
                int value = Math.random() < 0.7 ? direction : direction * -1;
                jumpDirection = vec3.yRot(Mth.PI * value / 3);
                if (!canDodge(jumpDirection)) {
                    this.direction = value * -1;
                    jumpDirection = vec3.yRot(Mth.PI * value * -1 / 3);
                    if (!canDodge(jumpDirection)) {
                        setState(defaultStates());
                        lastDodgeTime = level().getGameTime();
                        return false;
                    }
                }
            }
            setState(FungalZombieStates.DODGE);
            dodgeCount--;
            jumpDirection = jumpDirection.scale(1.5);
            this.setDeltaMovement(new Vec3(jumpDirection.x, 0.25, jumpDirection.z));
            triggerAnim("animationController", "start_dodge");
            return true;
        }
        return false;
    }

    @Override
    public boolean canGuard() {
        if (this.level().random.nextDouble() < -0.2 + (1 - this.getHealth() / this.getMaxHealth())) {
            FungalZombieState state = this.getState();
            if (state.isDefault() && state.canAttack() && this.level().getGameTime() - this.getLastGuardTime() > CommonConfig.VOLATILE_GUARD_COOLDOWN.get() * 20) {
                LivingEntity target = this.getTarget();
                if (target != null && target.isAlive() && this.getSensing().hasLineOfSight(target)) {
                    double distanceSqr = target.distanceToSqr(this);
                    AttributeInstance attributeInstance = target.getAttribute(ForgeMod.ENTITY_REACH.get());
                    double reach = attributeInstance == null ? FungalZombieGuardGoal.getTargetAttackReachSqr(this, target) : attributeInstance.getValue() * attributeInstance.getValue() * 1.44;
                    if (distanceSqr <= reach) {
                        if (target instanceof Player) {
                            ItemStack itemStack = target.getMainHandItem();
                            if (itemStack.getItem() instanceof TieredItem || itemStack.is(FungalHazardTags.MELEE_WEAPON)) {
                                double angle = SonaMath.vectorDegreeCalculate(target.getLookAngle(), this.getEyePosition().subtract(target.getEyePosition()));
                                return angle < 60 + Math.max(20 - 5 * Math.sqrt(distanceSqr), 0);
                            }
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance mobEffectInstance) {
        return mobEffectInstance.getEffect() instanceof PhysicalEffect && !mobEffectInstance.getEffect().equals(SonaMobEffects.STUN.get());
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (!level().isRainingAt(blockPos) && level().canSeeSky(blockPos)) {
            return -1;
        }
        int skyLight = levelReader.getBrightness(LightLayer.SKY, blockPos);
        return 15 - skyLight;
    }

    @Override
    public boolean canDodge() {
        if (this.getState().canMove() && this.onGround()) {
            if (this.getDodgeCount() > 0 && this.level().getGameTime() - this.getLastDodgeTime() > 20) {
                LivingEntity target = this.getTarget();
                if (target != null && target.isAlive()) {
                    ItemStack itemStack = target.getMainHandItem();
                    String name = itemStack.getItem().getClass().getName();
                    boolean flag1 = itemStack.is(FungalHazardTags.GUN) || name.toLowerCase().contains("gun");
                    Entity vehicle = target.getVehicle();
                    boolean flag2 = vehicle != null && "zombiekit:heavy_machine_gun".equals(ForgeRegistries.ENTITY_TYPES.getKey(vehicle.getType()).toString());
                    if (flag1 || flag2) {
                        double distance = target.distanceTo(this);
                        double angle = SonaMath.vectorDegreeCalculate(target.getLookAngle(), this.getEyePosition().subtract(target.getEyePosition()));
                        return angle < 5 + Math.max(40 - 5 * distance, 0);
                    }
                }
            }
        }
        return false;
    }

    public boolean canDodge(Vec3 directionNormal) {
        Vec3 direction = directionNormal.scale(6.5);
        Vec3 position = position();
        Vec3 eyePosition = getEyePosition();
        HitResult hitResult = level().clip(new ClipContext(position, position.add(direction), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));
        HitResult eyeHitResult = level().clip(new ClipContext(eyePosition, eyePosition.add(direction), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));
        Vec3 location = hitResult.getLocation();
        Vec3 eyeLocation = eyeHitResult.getLocation();
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            location.subtract(directionNormal);
        }
        if (eyeHitResult.getType() == HitResult.Type.BLOCK) {
            eyeLocation.subtract(directionNormal);
        }
        double d1 = location.distanceToSqr(position);
        double d2 = eyeLocation.distanceToSqr(eyePosition);
        if (getState().equals(FungalZombieStates.GUARD) && Math.min(d1, d2) < 36) {
            return false;
        }
        if (d1 > 12 && d2 > 12) {
            BlockPos blockPos = d1 <= d2 ? BlockPos.containing(location) : BlockPos.containing(eyeLocation).below();
            BlockPos targetPos = blockPos.below();
            int i = 0;
            for (; i < 5; i++) {
                if (level().getBlockState(targetPos).getCollisionShape(level(), targetPos).isEmpty()) {
                    targetPos = targetPos.below();
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public long getLastDodgeTime() {
        return lastDodgeTime;
    }

    @Override
    public long getLastGuardTime() {
        return lastGuardTime;
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
    public float getStepHeight() {
        if (getState().equals(FungalZombieStates.RUN) || getState().equals(FungalZombieStates.FLEE)) {
            return Math.max(super.getStepHeight(), 1);
        }
        return super.getStepHeight();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        FungalZombieState state = getState();
        boolean flag1 = state.equals(FungalZombieStates.RIDING);
        boolean flag2 = state.equals(FungalZombieStates.EXECUTION);
        if (flag1 || flag2) {
            Entity entity = damageSource.getEntity();
            if (entity != null) {
                damageInRiding += amount;
                if (entity.is(getFirstPassenger())) {
                    if (flag1) {
                        amount *= 0.4;
                    } else {
                        return false;
                    }
                }
            }
        } else if (state.equals(FungalZombieStates.GUARD) && !level().isClientSide()) {
            Entity entity = damageSource.getEntity();
            Vec3 source = damageSource.getSourcePosition();
            if (entity != null && source != null) {
                source = source.subtract(this.position());
                Vec3 lookAngle = this.calculateViewVector(0, Mth.wrapDegrees(yBodyRot));
                double angle = SonaMath.vectorDegreeCalculate(source.subtract(0, source.y, 0), lookAngle.subtract(0, lookAngle.y, 0));
                if (angle <= 75) {
                    amount *= CommonConfig.VOLATILE_GUARD_MODIFIER.get();
                    playSound(SoundEvents.PLAYER_ATTACK_WEAK);
                    if (canGuard) {
                        if (guardCount < 2) {
                            if (!damageSource.isIndirect()) {
                                setAnimationTick(20);
                                guardCount++;
                            }
                        } else {
                            canGuard = false;
                            if (level().random.nextDouble() < 0.8 && canJumpWithoutDistance() && getDodgeCount() > 0 && dodge()) {
                                setAnimationTick(0);
                            } else {
                                setAnimationTick(27);
                                attackAnimation();
                                level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(5),
                                        livingEntity -> canParry(lookAngle, livingEntity))
                                        .forEach(livingEntity -> livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.STUN.get(), (int) (CommonConfig.VOLATILE_GUARD_STUN_TIME.get() * 20), 0, true, false)));
                            }
                        }
                    }
                    NetworkHandler.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(), new VolatileGuardPacket(getId()));
                } else if (canGuard) {
                    setAnimationTick(0);
                }
            }
        }
        lastHurtAmount = amount;
        return super.hurt(damageSource, amount);
    }

    public boolean canParry(Vec3 lookAngle, LivingEntity livingEntity) {
        if (distanceToSqr(livingEntity) <= getAttackReachSqr(livingEntity)) {
            Vec3 position = livingEntity.position().subtract(this.position());
            double angleToTarget = SonaMath.vectorDegreeCalculate(position.subtract(0, position.y, 0), lookAngle.subtract(0, lookAngle.y, 0));
            return angleToTarget < 75 && canAttack(livingEntity) && !(livingEntity instanceof Mob mob && mob.getMobType() == MobType.UNDEAD);
        }
        return false;
    }

    @Override
    public void handleDamageEvent(DamageSource damageSource) {
        super.handleDamageEvent(damageSource);
        if (successGuard) {
            this.hurtTime = 0;
            this.hurtDuration = 0;
            successGuard = false;
        }

    }

    @Override
    public void push(Entity target) {
        if (getState().equals(FungalZombieStates.JUMPING) && target.equals(getTarget()) && target instanceof LivingEntity livingEntity && !(target.getVehicle() instanceof IFungalZombie)) {
            if (livingEntity.getUseItem().is(Items.SHIELD)) {
                level().playSound(null, this, SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1, 1);
                livingEntity.stopUsingItem();
                if (livingEntity instanceof Player player) {
                    player.getCooldowns().addCooldown(Items.SHIELD, 140);
                }
                setDeltaMovement(getDeltaMovement().subtract(0, getDeltaMovement().y, 0).scale(-0.2));
                stagger = true;
            } else if (!stagger) {
                target.setYRot(Mth.wrapDegrees(getYRot() + 180));
                target.startRiding(this);
                target.setDeltaMovement(Vec3.ZERO);
                livingEntity.stopUsingItem();
                if (!(livingEntity instanceof Player)) {
                    livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.STUN.get(), 30, 0, true, false));
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, CommonConfig.VOLATILE_EXECUTION_TIME.get() * 20, 1, true, false));
                    if (livingEntity instanceof Mob mob) {
                        mob.setTarget(null);
                    }
                } else {
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, CommonConfig.VOLATILE_EXECUTION_TIME.get() * 20, 1, true, false));
                }
                setDeltaMovement(getDeltaMovement().scale(0.5));
                setState(FungalZombieStates.RIDING);
            }
        } else {
            super.push(target);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean flag = super.doHurtTarget(target);
        if (flag && target instanceof LivingEntity livingEntity) {
            Level level = level();
            double damage = this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            damage += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), livingEntity.getMobType());
            ItemStack itemStack = livingEntity.getUseItem();
            if (level.random.nextDouble() < (-9 + damage) / 8 && itemStack.is(FungalHazardTags.SHIELD)) {
                level.playSound(null, livingEntity, SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1, 1);
                livingEntity.stopUsingItem();
                if (livingEntity instanceof Player player) {
                    player.getCooldowns().addCooldown(itemStack.getItem(), 100);
                    this.level().broadcastEntityEvent(player, (byte) 30);
                }
            }
        }
        return flag;
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
            Vec3 angle = new Vec3(getLookAngle().x, 0, getLookAngle().z).normalize().scale(1.3);
            moveFunction.accept(entity, this.getX() + angle.x, this.getY(), this.getZ() + angle.z);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 angle = new Vec3(passenger.getLookAngle().x, 0, passenger.getLookAngle().z).normalize().scale(-0.5);
        return angle.add(passenger.position());
    }

    @Override
    public void attackAnimation() {
        playSound(FungalHazardSounds.VOLATILE_ATTACK.get());
        String animation;
        if (getState().equals(FungalZombieStates.GUARD)) {
            triggerAnim("guardAnimationController", "parry");
        } else {
            if (!getMainHandItem().isEmpty()) {
                animation = "attack";
            } else {
                animation = random.nextDouble() < 0.5 ? "attack" : "attack2";
            }
            triggerAnim("animationController", animation);
        }

    }

    public void staggerAnimation() {
        triggerAnim("animationController", "stagger");
        setAnimationTick(30);
    }

    public void groundAnimation() {
        triggerAnim("animationController", "ground");
        setAnimationTick(15);
    }

    public void executionGroundAnimation() {
        triggerAnim("animationController", "execution_ground");
        setAnimationTick(20);
    }

    @Override
    public void registerStateRunner() {
        putStateRunner(FungalZombieStates.RIDING, new StateHandler(this::startRidingState, this::ridingState, this::endRidingState));
        putStateRunner(FungalZombieStates.EXECUTION, new StateHandler(this::startExecutionState, this::executionState, StateHandler.EMPTY_RUNNER));
        putStateRunner(FungalZombieStates.DODGE, new StateHandler(StateHandler.EMPTY_RUNNER, this::hangState, StateHandler.EMPTY_RUNNER));
        putStateRunner(FungalZombieStates.JUMP, new StateHandler(this::startJumpState, this::jumpState, StateHandler.EMPTY_RUNNER));
        putStateRunner(FungalZombieStates.JUMPING, new StateHandler(StateHandler.EMPTY_RUNNER, this::hangState, StateHandler.EMPTY_RUNNER));
        putStateRunner(FungalZombieStates.GROUND, new StateHandler(this::startGroundState, this::groundState, StateHandler.EMPTY_RUNNER));
        putStateRunner(FungalZombieStates.IDLE, new StateHandler(StateHandler.EMPTY_RUNNER, this::defaultState, StateHandler.EMPTY_RUNNER));
        putStateRunner(FungalZombieStates.RUN, new StateHandler(StateHandler.EMPTY_RUNNER, this::defaultState, StateHandler.EMPTY_RUNNER));
        putStateRunner(FungalZombieStates.FLEE, new StateHandler(StateHandler.EMPTY_RUNNER, this::defaultState, StateHandler.EMPTY_RUNNER));
        putStateRunner(FungalZombieStates.GUARD, new StateHandler(StateHandler.EMPTY_RUNNER, this::guardState, this::endGuardState));
    }

    public void ridingState(FungalZombieState state) {
        if (!level().isClientSide()) {
            Entity entity = getFirstPassenger();
            if (entity != null) {
                if (damageInRiding > getMaxHealth() * CommonConfig.VOLATILE_ESCAPE_DAMAGE.get()) {
                    entity.stopRiding();
                    staggerAnimation();
                    playSound(FungalHazardSounds.VOLATILE_EXECUTION_FAILURE.get());
                    setState(FungalZombieStates.GROUND);
                } else {
                    if (ridingTick++ > CommonConfig.VOLATILE_EXECUTION_TIME.get() * 20) {
                        setState(FungalZombieStates.EXECUTION);
                    }
                }
            } else {
                setState(FungalZombieStates.GROUND);
                executionGroundAnimation();
            }
        }
    }

    public void endRidingState(FungalZombieState state) {
        damageInRiding = 0;
        ridingTick = 0;
    }

    public void startExecutionState(FungalZombieState state) {
        if (!level().isClientSide()) {
            if (!CommonConfig.VOLATILE_FLEE_IN_SUN.get() || !(level().isDay() && level().canSeeSky(blockPosition()) && !level().isRainingAt(blockPosition()))) {
                setAnimationTick(43);
                playSound(FungalHazardSounds.VOLATILE_EXECUTION.get());
            } else {
                Entity entity = getFirstPassenger();
                if (entity != null) {
                    entity.stopRiding();
                }
                setState(FungalZombieStates.GROUND);
                executionGroundAnimation();
            }
        }
    }

    public void executionState(FungalZombieState state) {
        if (!level().isClientSide()) {
            Entity entity = getFirstPassenger();
            if (entity != null) {
                if (getAnimationTick() == 20) {
                    entity.hurt(level().damageSources().mobAttack(this), (float) (getAttributeValue(Attributes.ATTACK_DAMAGE) * CommonConfig.VOLATILE_EXECUTION_DAMAGE.get()));
                } else if (getAnimationTick() == 0) {
                    entity.stopRiding();
                    setState(FungalZombieStates.GROUND);
                    executionGroundAnimation();
                }
            } else {
                setState(FungalZombieStates.GROUND);
                executionGroundAnimation();
            }
        }

    }

    public void hangState(FungalZombieState state) {
        if (!level().isClientSide() && (onGround() || isInFluidType())) {
            if (!stagger) {
                groundAnimation();
            } else {
                staggerAnimation();
            }
            lastDodgeTime = level().getGameTime();
            if ((state.equals(FungalZombieStates.JUMPING))) {
                setState(FungalZombieStates.GROUND);
            } else {
                setState(defaultStates());
            }
            setDiscardFriction(false);
            stagger = false;
        }
    }

    public void startJumpState(FungalZombieState state) {
        if (!level().isClientSide()) {
            if (!isSilent()) {
                playSound(FungalHazardSounds.VOLATILE_JUMP_ATTACK.get(), 2, 1);
            }
            triggerAnim("animationController", "start_jump");
            setAnimationTick(10);
        }
    }

    @Override
    public void defaultState(FungalZombieState state) {
        if (!level().isClientSide()) {
            if (CommonConfig.VOLATILE_FLEE_IN_SUN.get() && level().isDay() && level().canSeeSky(blockPosition()) && !level().isRainingAt(blockPosition())) {
                setState(FungalZombieStates.FLEE);
            } else {
                setState(defaultStates());
            }
        }
    }

    public void startGroundState(FungalZombieState state) {
        refreshDimensions();
    }

    public void groundState(FungalZombieState state) {
        if (!level().isClientSide()) {
            if (getAnimationTick() == 0) {
                setState(defaultStates());
            }
        }
    }

    public void startRidingState(FungalZombieState state) {
        setDiscardFriction(false);
        Entity entity = getFirstPassenger();
        if (entity != null) {
            if (!CommonConfig.VOLATILE_FLEE_IN_SUN.get() || !(level().isDay() && level().canSeeSky(blockPosition()) && !level().isRainingAt(blockPosition()))) {
                playSound(FungalHazardSounds.VOLATILE_RIDING.get());
                if (entity instanceof Player player) {
                    player.setXRot(-30);
                    player.resetAttackStrengthTicker();
                }
                return;
            }
        }
        executionGroundAnimation();
        if (!level().isClientSide()) {
            setState(FungalZombieStates.GROUND);
        }
        refreshDimensions();
    }

    public void jumpState(FungalZombieState state) {
        if (getAnimationTick() == 0) {
            LivingEntity livingEntity = getTarget();
            if (livingEntity != null) {
                this.getLookControl().setLookAt(livingEntity, 60.0F, 30.0F);
                double distanceSqr = distanceToSqr(livingEntity);
                double heightDifference = getY() - livingEntity.getY();
                Vec3 vec3 = new Vec3(livingEntity.getX() - this.getX(), -heightDifference, livingEntity.getZ() - this.getZ());
                double horizontalDistance = vec3.horizontalDistance();
                if (heightDifference <= 0) {
                    double height = 0.45 + 0.1 * (distanceSqr - 36) / 108;
                    vec3 = vec3.normalize();
                    double y = vec3.y;
                    vec3 = vec3.subtract(0, y, 0).scale(2.5).add(0, height + y, 0);
                } else {
                    setDiscardFriction(true);
                    double time = jumpToTime(0.2, heightDifference);
                    vec3 = vec3.subtract(0, vec3.y, 0).normalize().scale(horizontalDistance / time).add(0, 0.2, 0);
                }
                this.setDeltaMovement(vec3);
            }
            setState(FungalZombieStates.JUMPING);
        }
    }

    public void guardState(FungalZombieState state) {
        this.yBodyRot = this.yHeadRot;
        this.setYRot(this.yHeadRot);
        if (!level().isClientSide()) {
            if (getAnimationTick() <= 0) {
                setState(defaultStates());
            } else if (getAnimationTick() == 10 && !canGuard) {
                Vec3 lookAngle = this.calculateViewVector(0, Mth.wrapDegrees(yBodyRot));
                level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(5),
                        livingEntity -> canParry(lookAngle, livingEntity))
                        .forEach(this::doHurtTarget);
            }
        }
    }

    public void endGuardState(FungalZombieState state) {
        canGuard = true;
        guardCount = 0;
        lastGuardTime = level().getGameTime();
    }

    @Override
    public boolean wantsToPickUp(ItemStack itemStack) {
        return CommonConfig.VOLATILE_EQUIPMENT.get() && super.wantsToPickUp(itemStack);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide()) {
            if (!init) {
                if (!CommonConfig.VOLATILE_EQUIPMENT.get()) {
                    for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                        setItemSlot(equipmentSlot, ItemStack.EMPTY);
                    }
                }
                init = true;
            }
            if ((dodgeCount <= 0 || getTarget() == null) && level().getGameTime() - lastDodgeTime > CommonConfig.VOLATILE_DODGE_COOLDOWN.get() * 20) {
                setDodgeCount(CommonConfig.VOLATILE_DODGE_COUNT.get());
                direction = Math.random() < 0.5 ? -1 : 1;
            }
        }
    }

    @Override
    public int getMaxFallDistance() {
        if (this.getTarget() == null) {
            return 6;
        } else {
            int i = (int) (this.getHealth() - this.getMaxHealth() * 0.33F);
            i -= (3 - this.level().getDifficulty().getId()) * 4;
            if (i < 0) {
                i = 0;
            }

            return i + 6;
        }
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState blockState, BlockPos blockPos) {
        if (onGround) {
            this.fallDistance = Math.max(0, this.fallDistance - 3);
        }
        super.checkFallDamage(y, onGround, blockState, blockPos);
    }


    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(0, new FungalZombieFleeGoal<>(this));
        this.goalSelector.addGoal(2, new FungalZombieGuardGoal<>(this));
        this.goalSelector.addGoal(1, new FungalZombieJumpGoal<>(this));
        this.goalSelector.addGoal(1, new FungalZombieDodgeGoal<>(this));
    }

    @Override
    public double getAttackDamageModifier() {
        return CommonConfig.VOLATILE_ATTACK_DAMAGE.get() - CommonConfig.VOLATILE_ATTACK_DAMAGE.getDefault();
    }

    @Override
    public double getHealthModifier() {
        return CommonConfig.VOLATILE_HEALTH.get() - CommonConfig.VOLATILE_HEALTH.getDefault();
    }

    @Override
    public double getMovementModifier() {
        return CommonConfig.VOLATILE_MOVEMENT.get() - CommonConfig.VOLATILE_MOVEMENT.getDefault();
    }

    @Override
    public double getArmorModifier() {
        return CommonConfig.VOLATILE_ARMOR.get() - CommonConfig.VOLATILE_ARMOR.getDefault();
    }

    @Override
    public double getArmorToughnessModifier() {
        return CommonConfig.VOLATILE_ARMOR_TOUGHNESS.get() - CommonConfig.VOLATILE_ARMOR_TOUGHNESS.getDefault();
    }

    @Override
    public double getAttackRangeModifier() {
        return 1.44;
    }

    @Override
    public int getAttackCoolDown() {
        return 40;
    }

    @Override
    public boolean testAttackable(LivingEntity livingEntity) {
        if (!CommonConfig.VOLATILE_FLEE_IN_SUN.get()) {
            return true;
        }
        return !level().isDay() || level().isRainingAt(livingEntity.blockPosition()) || !level().canSeeSky(livingEntity.blockPosition());
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
        Entity target = getTarget();
        if (target == null || !target.isAlive()) {
            return FungalZombieStates.IDLE;
        }
        return FungalZombieStates.RUN;

    }

    public boolean isDefaultState() {
        return getState().isDefault();
    }

    @Override
    public boolean canJoinPatrol() {
        return false;
    }

    @Override
    protected void tickDeath() {
        super.tickDeath();
        deathTime++;
        if (this.deathTime >= 30 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(Entity.RemovalReason.KILLED);
        }
        setPose(Pose.DYING);
    }

    @Override
    protected boolean isSunSensitive() {
        return CommonConfig.VOLATILE_BURN_IN_SUN.get() && super.isSunSensitive();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "animationController", 3, this::animationController)
                .triggerableAnim("attack", ATTACK)
                .triggerableAnim("attack2", ATTACK2)
                .triggerableAnim("sprint", SPRINT)
                .triggerableAnim("start_jump", START_JUMP)
                .triggerableAnim("ground", GROUND)
                .triggerableAnim("stagger", STAGGER)
                .triggerableAnim("start_dodge", START_DODGE)
                .triggerableAnim("execution_ground", EXECUTION_GROUND)
                .receiveTriggeredAnimations());
        controllerRegistrar.add(new AnimationController<>(this, "executionAnimationController", 3, this::executionAnimationController)
                .triggerableAnim("execution", EXECUTION)
                .receiveTriggeredAnimations());
        controllerRegistrar.add(new AnimationController<>(this, "deathAnimationController", 0, this::deathAnimationController)
                .receiveTriggeredAnimations());
        controllerRegistrar.add(new AnimationController<>(this, "guardAnimationController", 3, this::guardAnimationController)
                .triggerableAnim("parry", PARRY)
                .receiveTriggeredAnimations());
    }

    private PlayState guardAnimationController(AnimationState<VolatileEntity> event) {
        AnimationController<VolatileEntity> controller = event.getController();
        if (isAlive() && getPassengers().isEmpty() && getState().equals(FungalZombieStates.GUARD)) {
            if (event.isMoving() || !(event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F)) {
                if (!controller.isPlayingTriggeredAnimation()) {
                    if (event.isCurrentAnimation(GUARD) || controller.getCurrentRawAnimation() == null || event.getController().hasAnimationFinished()) {
                        event.setAndContinue(GUARD_WALK);
                    }
                }
            } else {
                if (!controller.isPlayingTriggeredAnimation()) {
                    if (event.isCurrentAnimation(GUARD_WALK) || controller.getCurrentRawAnimation() == null || event.getController().hasAnimationFinished()) {
                        event.setAndContinue(GUARD);
                    }
                }
            }
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private PlayState deathAnimationController(AnimationState<VolatileEntity> event) {
        if (!isAlive()) {
            if (!event.isCurrentAnimation(DEATH)) {
                event.setAndContinue(DEATH);
            }
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private PlayState executionAnimationController(AnimationState<VolatileEntity> event) {
        AnimationController<VolatileEntity> controller = event.getController();

        if (isAlive() && !getPassengers().isEmpty()) {
            if (!controller.isPlayingTriggeredAnimation()) {
                if (getState().equals(FungalZombieStates.RIDING)) {

                    if (event.isCurrentAnimation(EXECUTION) || controller.getCurrentRawAnimation() == null || event.getController().hasAnimationFinished()) {

                        event.setAndContinue(RIDING);
                    }
                } else if (getState().equals(FungalZombieStates.EXECUTION)) {
                    if (event.isCurrentAnimation(RIDING) || controller.getCurrentRawAnimation() == null || event.getController().hasAnimationFinished()) {
                        event.setAndContinue(EXECUTION);
                    }
                }
            }
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    public PlayState animationController(AnimationState<VolatileEntity> event) {
        AnimationController<VolatileEntity> controller = event.getController();
        if (isAlive() && getPassengers().isEmpty() && !getState().equals(FungalZombieStates.GUARD)) {
            if (isDefaultState()) {
                if (event.isMoving() || !(event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F)) {
                    if (!controller.isPlayingTriggeredAnimation()) {
                        if (event.isCurrentAnimation(IDLE) || event.isCurrentAnimation(WALK) || event.isCurrentAnimation(GUARD) || controller.getCurrentRawAnimation() == null || event.getController().hasAnimationFinished()) {
                            if (getState().equals(FungalZombieStates.IDLE)) {
                                event.setAndContinue(WALK);
                            } else {
                                event.setAndContinue(SPRINT);
                            }
                        }
                    }
                } else {
                    if (!controller.isPlayingTriggeredAnimation()) {
                        if (event.isCurrentAnimation(WALK) || event.isCurrentAnimation(SPRINT) || event.isCurrentAnimation(GUARD) || controller.getCurrentRawAnimation() == null || event.getController().hasAnimationFinished()) {
                            event.setAndContinue(IDLE);
                        }
                    }
                }
            }

            if (event.isCurrentAnimation(GROUND) || event.isCurrentAnimation(START_JUMP) || event.isCurrentAnimation(SPRINT)) {
                controller.setAnimationSpeed(2);
            } else {
                controller.setAnimationSpeed(1);
            }
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }


    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder.add(Attributes.MOVEMENT_SPEED, 0.45)
                .add(Attributes.MAX_HEALTH, 85)
                .add(Attributes.ATTACK_DAMAGE, 13)
                .add(Attributes.FOLLOW_RANGE, 35)
                .add(Attributes.ARMOR, 15)
                .add(Attributes.ARMOR_TOUGHNESS, 8)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        return builder;
    }

}
