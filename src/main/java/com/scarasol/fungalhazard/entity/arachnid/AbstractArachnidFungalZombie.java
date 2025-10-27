package com.scarasol.fungalhazard.entity.arachnid;

import com.google.common.collect.Maps;
import com.scarasol.fungalhazard.api.IFungalZombie;
import com.scarasol.fungalhazard.api.IPatrolMob;
import com.scarasol.fungalhazard.configuration.CommonConfig;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieState;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieStates;
import com.scarasol.fungalhazard.entity.ai.fsm.StateHandler;
import com.scarasol.fungalhazard.entity.humanoid.AbstractMutilatableZombie;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Map;

import static com.scarasol.fungalhazard.init.FungalHazardEntities.*;

/**
 * @author Scarasol
 */
public abstract class AbstractArachnidFungalZombie extends Spider implements IFungalZombie {


    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(AbstractArachnidFungalZombie.class, EntityDataSerializers.INT);

    private final Map<FungalZombieState, StateHandler> stateHandlers = Maps.newHashMap();
    @Nullable
    private BlockPos patrolTarget;
    private boolean patrolLeader;
    private boolean patrolling;
    private boolean hasLeader;
    private LivingEntity leader;
    private int animationTick;
    private FungalZombieState oldState;

    public AbstractArachnidFungalZombie(EntityType<? extends Spider> entityType, Level level) {
        super(entityType, level);
        registerStateRunner();
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public @NotNull MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        setState(defaultStates());
        getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(SPAWN_MODIFIER, "FungalHazardSpawnModifier", getAttackDamageModifier(), AttributeModifier.Operation.ADDITION));
        float healthRate = getHealth() / getMaxHealth();
        getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(SPAWN_MODIFIER, "FungalHazardSpawnModifier", getHealthModifier(), AttributeModifier.Operation.ADDITION));
        setHealth(getMaxHealth() * healthRate);
        getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier(SPAWN_MODIFIER, "FungalHazardSpawnModifier", getMovementModifier(), AttributeModifier.Operation.ADDITION));
        getAttribute(Attributes.ARMOR).addPermanentModifier(new AttributeModifier(SPAWN_MODIFIER, "FungalHazardSpawnModifier", getArmorModifier(), AttributeModifier.Operation.ADDITION));
        getAttribute(Attributes.ARMOR_TOUGHNESS).addPermanentModifier(new AttributeModifier(SPAWN_MODIFIER, "FungalHazardSpawnModifier", getArmorToughnessModifier(), AttributeModifier.Operation.ADDITION));

        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return entityDimensions.height * 0.85F;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (entityDataAccessor.equals(STATE)) {
            FungalZombieState stateNew = getState();
            switchState(oldState, stateNew);
            oldState = stateNew;
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override
    public int getMaxHeadYRot() {
        return 0;
    }

    @Override
    public void setXRot(float xRot) {
        if (getState().canRot() && isAlive()) {
            super.setXRot(xRot);
        }
    }

    @Override
    public void setYRot(float yRot) {
        if (getState().canRot() && isAlive()) {
            super.setYRot(yRot);
        } else {
            super.setYRot(yBodyRot);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, 0);
    }

    @Override
    public void push(Entity entity) {
        if (isPatrolling()) {
            if (isPatrolLeader() || (entity instanceof IPatrolMob patrolMob && patrolMob.isPatrolLeader())) {
                return;
            }
        }
        super.push(entity);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (isAlive()) {
            return super.getDimensions(pose);
        }
        return DEATH_DIMENSIONS.scale(getScale());
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
    }

    @Override
    protected void tickDeath() {
        yBodyRot = yBodyRotO;
        yHeadRot = yBodyRot;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        setAnimationTick(Math.max(getAnimationTick() - 1, 0));
        runState();
        if (!getState().canRot()) {
            yBodyRot = yBodyRotO;
            yHeadRot = yBodyRot;
        }
    }

    @Override
    public float getStepHeight() {
        if (isPatrolling()) {
            return Math.max(super.getStepHeight(), 1);
        }
        return super.getStepHeight();
    }


    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("PoseState", getState().index());
        savePatrolInfo(compoundTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("PoseState")) {
            setState(compoundTag.getInt("PoseState"));
        }
        readPatrolInfo(compoundTag);
    }

    @NotNull
    @Override
    public Map<FungalZombieState, StateHandler> getStateHandlers() {
        return stateHandlers;
    }

    @Override
    public void setState(FungalZombieState stateNew) {
        this.entityData.set(STATE, stateNew.index());
    }

    @Override
    public void setState(int stateIndex) {
        FungalZombieState stateNew = FungalZombieStates.FUNGAL_ZOMBIE_STATES.get(stateIndex);
        if (stateNew != null) {
            setState(stateNew);
        } else {
            setState(defaultStates());
        }
    }

    @Override
    public FungalZombieState getState() {
        return FungalZombieStates.FUNGAL_ZOMBIE_STATES.get(this.entityData.get(STATE));
    }

    @Override
    public double getAttackReachSqr(LivingEntity target) {
        return 1;
    }

    @Override
    public void setAnimationTick(int animationTick) {
        this.animationTick = animationTick;
    }

    @Override
    public int getAnimationTick() {
        return this.animationTick;
    }

    @Override
    public boolean isHasLeader() {
        return this.hasLeader;
    }

    @Override
    public void setHasLeader(boolean hasLeader) {
        this.hasLeader = hasLeader;
    }

    @Override
    public LivingEntity getLeader() {
        return this.leader;
    }

    @Override
    public void setLeader(LivingEntity leader) {
        this.leader = leader;
    }

    @Override
    public boolean canBeLeader() {
        return true;
    }

    @Override
    public boolean isPatrolLeader() {
        return this.patrolLeader;
    }

    @Nullable
    @Override
    public BlockPos getPatrolTarget() {
        return this.patrolTarget;
    }

    @Override
    public boolean hasPatrolTarget() {
        return this.patrolTarget != null;
    }

    @Override
    public void setPatrolTarget(@Nullable BlockPos patrolTarget) {
        this.patrolTarget = patrolTarget;
    }

    @Override
    public void setPatrolLeader(boolean patrolLeader) {
        this.patrolLeader = patrolLeader;
    }

    @Override
    public boolean canJoinPatrolNow() {
        FungalZombieState state = getState();
        Level level = level();
        boolean flag = level.getBrightness(LightLayer.SKY, blockPosition()) > 10 || level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPosition()).getY() <= getEyeY();
        boolean flag2 = !level.isDay() || !isSunSensitive() || level.isRaining();
        return CommonConfig.INFECTED_PATROLLING.get() && state.canPatrol() && flag2 && flag;
    }

    private boolean isSunSensitive() {
        return true;
    }

    @Override
    public boolean canJoinPatrol() {
        return true;
    }

    @Override
    public void findPatrolTarget() {
        this.patrolTarget = this.blockPosition().offset(-500 + this.random.nextInt(1000), 0, -500 + this.random.nextInt(1000));
        this.patrolling = true;
    }

    @Override
    public boolean isPatrolling() {
        return this.patrolling;
    }

    @Override
    public void setPatrolling(boolean patrolling) {
        this.patrolling = patrolling;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

}
