package com.scarasol.fungalhazard.entity;

import com.google.common.collect.Maps;
import com.scarasol.fungalhazard.FungalHazardMod;
import com.scarasol.fungalhazard.api.IFungalHazardGeoEntity;
import com.scarasol.fungalhazard.configuration.CommonConfig;
import com.scarasol.fungalhazard.entity.ai.FungalZombieGroundPathNavigation;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieState;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieStates;
import com.scarasol.fungalhazard.entity.ai.fsm.StateHandler;
import com.scarasol.fungalhazard.entity.goal.*;
import com.scarasol.fungalhazard.init.FungalHazardTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

/**
 * @author Scarasol
 */
public abstract class AbstractFungalZombie extends Zombie implements IFungalHazardGeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final EntityDimensions DEATH_DIMENSIONS = EntityDimensions.scalable(0.7f, 0.1f);

    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(AbstractFungalZombie.class, EntityDataSerializers.INT);

    public static final UUID SPAWN_MODIFIER = UUID.fromString("3606EAA0-0711-7B20-6FEA-1B6E1EFB535C");
//    public static final UUID HEALTH_MODIFIER = UUID.fromString("6C53F861-5F06-B37C-3BBA-CE9A64139214");
//    public static final UUID MOVEMENT_MODIFIER = UUID.fromString("9576CFEC-AE8C-7FF8-0612-52DA09D7632B");
//    public static final UUID ARMOR_MODIFIER = UUID.fromString("5CDAC255-47E4-3E04-B51E-28F64AB10F0C");
//    public static final UUID ARMOR_TOUGHNESS_MODIFIER = UUID.fromString("9DE7C1C5-1772-C598-CFA4-230C16C22781");

    @Nullable
    private BlockPos patrolTarget;
    private boolean patrolLeader;
    private boolean patrolling;
    private boolean hasLeader;
    private AbstractFungalZombie leader;
    private int animationTick;
    private final Map<FungalZombieState, StateHandler> stateHandlers = Maps.newHashMap();

    private FungalZombieState oldState;


    public AbstractFungalZombie(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
        registerStateRunner();
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public int getAnimationTick() {
        return animationTick;
    }

    public void setAnimationTick(int animationTick) {
        this.animationTick = animationTick;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FungalZombieGroundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new FungalZombiePatrolGoal<>(this, 0.7D, 0.595D));
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
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        setState(defaultStates());
        if (spawnGroupData == null) {
            spawnGroupData = new Zombie.ZombieGroupData(false, false);
        }
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
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(3, new FungalZombieAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(6, new FungalZombieFloatGoal(this));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new FungalZombieWaterAvoidingRandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new FungalZombieNearestAttackableTargetGoal<>(this, Player.class, true, this::testAttackable));
        this.targetSelector.addGoal(3, new FungalZombieNearestAttackableTargetGoal<>(this, AbstractVillager.class, false, this::testAttackable));
        this.targetSelector.addGoal(3, new FungalZombieNearestAttackableTargetGoal<>(this, IronGolem.class, true, this::testAttackable));
        this.targetSelector.addGoal(5, new FungalZombieNearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, livingEntity -> testAttackable(this) && Turtle.BABY_ON_LAND_SELECTOR.test(this)));
    }

    public abstract double getAttackDamageModifier();

    public abstract double getHealthModifier();

    public abstract double getMovementModifier();

    public abstract double getArmorModifier();

    public abstract double getArmorToughnessModifier();

    public abstract boolean testAttackable(LivingEntity livingEntity);

    public abstract void registerStateRunner();

    public abstract double getAttackRangeModifier();

    public abstract int getAttackCoolDown();

    public double getAttackReachSqr(LivingEntity target) {
        return this.getBbWidth() * 2.0F * this.getBbWidth() * 2.0F * getAttackRangeModifier() + target.getBbWidth();
    }

    public void putStateRunner(FungalZombieState state, StateHandler handler) {
        stateHandlers.put(state, handler);
    }

    public void runState() {
        FungalZombieState state = getState();
        StateHandler.StateRunner runner = stateHandlers.get(state).loopRunner();
        if (runner != null) {
            runner.run(state);
        }
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

    public void setState(FungalZombieState stateNew) {
//        FungalZombieState stateOld = getState();
//        switchState(stateOld, stateNew);
        this.entityData.set(STATE, stateNew.index());
    }

    public void setState(int stateIndex) {
        FungalZombieState stateNew = FungalZombieStates.FUNGAL_ZOMBIE_STATES.get(stateIndex);
        if (stateNew != null) {
            setState(stateNew);
        } else {
            setState(defaultStates());
        }
    }

    public void switchState(@Nullable FungalZombieState stateOld, @Nonnull FungalZombieState stateNew) {
        if (!stateNew.equals(stateOld)) {
            StateHandler handler = stateHandlers.get(stateOld);
            if (handler != null) {
                handler.endRunner().run(stateOld);
            }
            handler = stateHandlers.get(stateNew);
            if (handler != null) {
                handler.startRunner().run(stateNew);
            }
        }
    }

    public FungalZombieState getState() {
        return FungalZombieStates.FUNGAL_ZOMBIE_STATES.get(this.entityData.get(STATE));
    }

    @Override
    public boolean canBeCollidedWith() {
        return !isAlive();
    }

    @Override
    public void push(Entity entity) {
        if (isPatrolling()) {
            if (isPatrolLeader() || (entity instanceof AbstractFungalZombie zombie && zombie.isPatrolLeader())) {
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
    public void setBaby(boolean isBaby) {
        super.setBaby(false);
    }

    public abstract void attackAnimation();

    public abstract int getAttackAnimeTick();

    public abstract FungalZombieState defaultStates();

    public static boolean checkFungalZombieSpawnRules(EntityType<? extends AbstractFungalZombie> entityType, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {

        ServerLevel serverLevel = level.getLevel();
        if (serverLevel.getBiome(pos).is(FungalHazardTags.INFECTED_BLACKLIST)) {
            return false;
        }
//        if (serverLevel.isRainingAt(pos)) {
//            return true;
//        }
        return Monster.checkMonsterSpawnRules(entityType, level, reason, pos, random);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public float getStepHeight() {
        if (isPatrolling()) {
            return Math.max(super.getStepHeight(), 1);
        }
        return super.getStepHeight();
    }

    public boolean isHasLeader() {
        return hasLeader;
    }

    public void setHasLeader(boolean hasLeader) {
        this.hasLeader = hasLeader;
    }

    public AbstractFungalZombie getLeader() {
        return leader;
    }

    public void setLeader(AbstractFungalZombie leader) {
        this.leader = leader;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.patrolTarget != null) {
            compoundTag.put("PatrolTarget", NbtUtils.writeBlockPos(this.patrolTarget));
        }
        compoundTag.putInt("PoseState", getState().index());
        compoundTag.putBoolean("PatrolLeader", this.patrolLeader);
        compoundTag.putBoolean("Patrolling", this.patrolling);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("PatrolTarget")) {
            this.patrolTarget = NbtUtils.readBlockPos(compoundTag.getCompound("PatrolTarget"));
        }
        if (compoundTag.contains("PoseState")) {
            setState(compoundTag.getInt("PoseState"));
        }
        this.patrolLeader = compoundTag.getBoolean("PatrolLeader");
        this.patrolling = compoundTag.getBoolean("Patrolling");
    }

    public boolean canBeLeader() {
        return getState().canPatrol();
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return !this.patrolling || distanceSqr > 16384.0D;
    }

    public void setPatrolTarget(BlockPos patrolTarget) {
        this.patrolTarget = patrolTarget;
        this.patrolling = true;
    }

    public BlockPos getPatrolTarget() {
        return this.patrolTarget;
    }

    public boolean hasPatrolTarget() {
        return this.patrolTarget != null;
    }

    public void setPatrolLeader(boolean patrolLeader) {
        this.patrolLeader = patrolLeader;
        this.patrolling = true;
    }

    public boolean isPatrolLeader() {
        return this.patrolLeader;
    }

    public boolean canJoinPatrol() {
        FungalZombieState state = getState();
        return CommonConfig.INFECTED_PATROLLING.get() && state.canPatrol() && level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPosition()).getY() <= getEyeY() && (!level().isDay() || level().isRaining());
    }

    public void findPatrolTarget() {
        this.patrolTarget = this.blockPosition().offset(-500 + this.random.nextInt(1000), 0, -500 + this.random.nextInt(1000));
        this.patrolling = true;
    }

    public boolean isPatrolling() {
        return this.patrolling;
    }

    public void setPatrolling(boolean patrolling) {
        this.patrolling = patrolling;
    }


}
