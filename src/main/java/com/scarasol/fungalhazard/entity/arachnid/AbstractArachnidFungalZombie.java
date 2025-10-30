package com.scarasol.fungalhazard.entity.arachnid;

import com.google.common.collect.Maps;
import com.scarasol.fungalhazard.api.IFungalZombie;
import com.scarasol.fungalhazard.api.IPatrolMob;
import com.scarasol.fungalhazard.configuration.CommonConfig;
import com.scarasol.fungalhazard.entity.ai.ArachnidSurfaceNavigation;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieState;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieStates;
import com.scarasol.fungalhazard.entity.ai.fsm.StateHandler;
import com.scarasol.fungalhazard.entity.humanoid.AbstractMutilatableZombie;
import com.nyfaria.nyfsspiders.common.CollisionSmoothingUtil;
import com.nyfaria.nyfsspiders.common.entity.mob.IClimberEntity;
import com.nyfaria.nyfsspiders.common.entity.mob.Orientation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.scarasol.fungalhazard.init.FungalHazardEntities.*;

/**
 * @author Scarasol
 */
public abstract class AbstractArachnidFungalZombie extends Spider implements IFungalZombie, IClimberEntity {


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
    @Nullable
    private Direction crawlingDirection;
    private int surfaceGraceTicks;

    private static final float COLLISION_INCLUSION_RANGE = 1.4F;
    private static final float COLLISION_SMOOTHING_RANGE = 1.25F;
    private static final float COLLISION_SAMPLE_STEP = 0.001F;
    private static final int COLLISION_ITERATIONS = 20;
    private static final float COLLISION_THRESHOLD = 0.05F;

    private static final double SURFACE_CHECK_STEP = 0.3D;
    private static final double SURFACE_SHRINK = 0.02D;
    private static final double SURFACE_GAP_TOLERANCE = 0.25D;
    private static final double SURFACE_OVERLAP_EPS = 5.0E-4D;
    private static final double SURFACE_QUERY_EPSILON = 1.0E-4D;
    private static final double SURFACE_CORNER_OFFSET = 0.12D;
    private static final int SURFACE_GRACE_TICKS = 3;
    private static final Direction[] SURFACE_SEARCH_ORDER = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP, Direction.DOWN};
    private static final Vec3[] SURFACE_PROBES = new Vec3[]{
            Vec3.ZERO,
            new Vec3(SURFACE_CORNER_OFFSET, 0.0D, 0.0D),
            new Vec3(-SURFACE_CORNER_OFFSET, 0.0D, 0.0D),
            new Vec3(0.0D, SURFACE_CORNER_OFFSET, 0.0D),
            new Vec3(0.0D, -SURFACE_CORNER_OFFSET, 0.0D),
            new Vec3(0.0D, 0.0D, SURFACE_CORNER_OFFSET),
            new Vec3(0.0D, 0.0D, -SURFACE_CORNER_OFFSET)
    };

    private record SurfaceHit(Direction direction, Vec3 anchor, Vec3 normal) {}

    private Vec3 attachmentNormal = Vec3.UP;
    private Vec3 prevAttachmentNormal = Vec3.UP;
    private Vec3 attachmentAnchor = Vec3.ZERO;
    private Vec3 prevAttachmentAnchor = Vec3.ZERO;
    private Orientation orientation = defaultOrientation();
    private final List<AABB> collisionCache = new ArrayList<>();

    public AbstractArachnidFungalZombie(EntityType<? extends Spider> entityType, Level level) {
        super(entityType, level);
        registerStateRunner();
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new ArachnidSurfaceNavigation<>(this, level);
    }

    @Override
    public void tick() {
        super.tick();
        updateSurfaceAttachment();
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
        if (crawlingDirection != null) {
            clampSurfaceMovement();
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

    private void updateSurfaceAttachment() {
        if (!isAlive()) {
            detachFromSurface();
            return;
        }

        SurfaceHit hit = findSurfaceContact();
        if (hit != null) {
            crawlingDirection = hit.direction();
            Vec3 anchor = hit.anchor();
            prevAttachmentNormal = attachmentNormal;
            prevAttachmentAnchor = attachmentAnchor;
            attachmentAnchor = anchor;
            attachmentNormal = resolveNormal(hit);
            orientation = computeOrientationFromNormal(attachmentNormal);
            setNoGravity(true);
            fallDistance = 0.0F;
            surfaceGraceTicks = SURFACE_GRACE_TICKS;
        } else if (surfaceGraceTicks > 0) {
            surfaceGraceTicks--;
        } else {
            detachFromSurface();
        }

        boolean horizontalAttachment = crawlingDirection != null && crawlingDirection.getAxis().isHorizontal();
        this.setClimbing(this.horizontalCollision || horizontalAttachment);
    }

    private void detachFromSurface() {
        if (crawlingDirection != null) {
            setNoGravity(false);
        }
        crawlingDirection = null;
        surfaceGraceTicks = 0;
        attachmentAnchor = Vec3.ZERO;
        prevAttachmentAnchor = Vec3.ZERO;
        attachmentNormal = Vec3.UP;
        prevAttachmentNormal = Vec3.UP;
        orientation = defaultOrientation();
    }

    @Nullable
    public Direction getCrawlingDirection() {
        return crawlingDirection;
    }

    @Override
    public Orientation getOrientation() {
        return orientation;
    }

    @Override
    public Direction getGroundDirection() {
        return crawlingDirection != null ? crawlingDirection : Direction.DOWN;
    }

    @Override
    public double getMovementSpeed() {
        AttributeInstance attribute = getAttribute(Attributes.MOVEMENT_SPEED);
        return attribute != null ? attribute.getValue() : this.getSpeed();
    }

    @Override
    public Direction getGroundSide() {
        return getGroundDirection();
    }

    @Override
    public void onPathingObstructed(Direction facing) {
        surfaceGraceTicks = 0;
    }

    @Override
    public int getMaxStuckCheckTicks() {
        return 40;
    }

    @Override
    public float getBridgePathingMalus(Mob entity, BlockPos pos, Node fallPathPoint) {
        return -1.0F;
    }

    @Override
    public float getPathingMalus(BlockGetter cache, Mob entity, BlockPathTypes nodeType, BlockPos pos, Vec3i direction, Predicate<Direction> sides) {
        return entity.getPathfindingMalus(nodeType);
    }

    @Override
    public void pathFinderCleanup() {
    }

    @Nullable
    private SurfaceHit findSurfaceContact() {
        Pair<Vec3, Vec3> contact = sampleSurfaceContact();
        if (contact != null) {
            Vec3 normal = contact.getRight();
            if (normal != null && normal.lengthSqr() > 1.0E-6D) {
                Direction direction = resolveSurfaceDirection(normal);
                return new SurfaceHit(direction, contact.getLeft(), normal);
            }
        }
        SurfaceHit fallback = findAdjacentSurface();
        if (fallback != null) {
            return fallback;
        }
        return null;
    }

    private void clampSurfaceMovement() {
        Direction direction = this.crawlingDirection;
        if (direction == null) {
            return;
        }

        if (direction == Direction.UP) {
            clampToCeiling();
        } else if (direction.getAxis().isHorizontal()) {
            clampToWall(direction);
        }
    }

    private Vec3 resolveNormal(SurfaceHit hit) {
        Vec3 normal = hit.normal();
        if (normal == null || normal.lengthSqr() < 1.0E-6D) {
            return normalVector(hit.direction());
        }
        double length = normal.length();
        return length > 1.0E-6D ? normal.scale(1.0D / length) : normalVector(hit.direction());
    }

    private Direction resolveSurfaceDirection(Vec3 normal) {
        Vec3 inverted = normal.scale(-1.0D);
        return Direction.getNearest(inverted.x, inverted.y, inverted.z);
    }

    private Orientation computeOrientationFromNormal(Vec3 normal) {
        Vec3 up = normal.lengthSqr() > 1.0E-6D ? normal.normalize() : Vec3.UP;
        Vec3 reference = Math.abs(up.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : Vec3.UP;
        Vec3 localX = reference.cross(up).normalize();
        if (localX.lengthSqr() < 1.0E-6D) {
            localX = new Vec3(1.0D, 0.0D, 0.0D);
        }
        Vec3 localZ = localX.cross(up).normalize();
        float componentZ = (float) localZ.dot(up);
        float componentY = (float) up.dot(up);
        float componentX = (float) localX.dot(up);
        float yaw = (float) Math.toDegrees(Math.atan2(componentX, componentZ));
        float pitch = (float) Math.toDegrees(Math.atan2(Math.sqrt(componentX * componentX + componentZ * componentZ), componentY));
        return new Orientation(up, localZ, up, localX, componentZ, componentY, componentX, yaw, pitch);
    }

    private Orientation defaultOrientation() {
        return computeOrientationFromNormal(Vec3.UP);
    }

    @Nullable
    private Pair<Vec3, Vec3> sampleSurfaceContact() {
        Vec3 center = getBoundingBox().getCenter();
        Vec3 probe = center.add(0.0D, getBbHeight() * 0.25D, 0.0D);
        return findContact(probe, attachmentNormal.scale(-1.0D));
    }

    @Nullable
    private Pair<Vec3, Vec3> findContact(Vec3 probePoint, Vec3 preferredNormal) {
        collisionCache.clear();
        AABB inclusion = new AABB(probePoint, probePoint).inflate(COLLISION_INCLUSION_RANGE);
        for (VoxelShape shape : level().getBlockCollisions(this, inclusion)) {
            if (!shape.isEmpty()) {
                collisionCache.addAll(shape.toAabbs());
            }
        }
        if (collisionCache.isEmpty()) {
            return null;
        }
        Vec3 normal = preferredNormal.lengthSqr() > 1.0E-6D ? preferredNormal.normalize() : Vec3.ZERO;
        Pair<Vec3, Vec3> contact = CollisionSmoothingUtil.findClosestPoint(
                collisionCache,
                probePoint,
                normal,
                COLLISION_SMOOTHING_RANGE,
                1.0F,
                COLLISION_SAMPLE_STEP,
                COLLISION_ITERATIONS,
                COLLISION_THRESHOLD,
                probePoint
        );
        if (contact == null) {
            return null;
        }
        Vec3 anchor = contact.getLeft();
        if (anchor == null) {
            return null;
        }
        double maxDistance = (getBbWidth() + 1.0D) * (getBbWidth() + 1.0D);
        if (anchor.distanceToSqr(probePoint) > maxDistance) {
            return null;
        }
        Vec3 resultNormal = contact.getRight();
        if (resultNormal == null || resultNormal.lengthSqr() < 1.0E-6D) {
            return null;
        }
        return contact;
    }

    private void clampToCeiling() {
        Vec3 movement = getDeltaMovement();
        if (movement.y < 0.0D) {
            setDeltaMovement(movement.x, 0.0D, movement.z);
        }

        double maxY = getBoundingBox().maxY;
        BlockPos blockPos = BlockPos.containing(getX(), maxY + 0.2D, getZ());
        BlockState stateAbove = level().getBlockState(blockPos);
        if (!stateAbove.isAir()) {
            VoxelShape shape = stateAbove.getCollisionShape(level(), blockPos);
            if (!shape.isEmpty()) {
                double ceilingY = blockPos.getY() + shape.min(Direction.Axis.Y);
                double desiredMaxY = ceilingY - 0.01D;
                double desiredMinY = desiredMaxY - getBbHeight();
                if (getBoundingBox().maxY > desiredMaxY) {
                    setPos(getX(), desiredMinY, getZ());
                }
            } else {
                double desiredMaxY = blockPos.getY() - 0.01D;
                double desiredMinY = desiredMaxY - getBbHeight();
                if (getBoundingBox().maxY > desiredMaxY) {
                    setPos(getX(), desiredMinY, getZ());
                }
            }
        }
    }

    private void clampToWall(Direction direction) {
        Vec3 movement = getDeltaMovement();
        Vec3 normal = Vec3.atLowerCornerOf(direction.getNormal()).normalize();
        double push = movement.dot(normal);
        if (push > 0.0D) {
            movement = movement.subtract(normal.scale(push));
        }
        if (movement.y < 0.0D) {
            movement = new Vec3(movement.x, 0.0D, movement.z);
        }
        setDeltaMovement(movement);
    }

    @Nullable
    private SurfaceHit findAdjacentSurface() {
        Direction preferred = this.crawlingDirection;
        AABB box = getBoundingBox();
        if (preferred != null) {
            SurfaceHit hit = findSurface(preferred, box);
            if (hit != null) {
                return hit;
            }
        }

        for (Direction direction : SURFACE_SEARCH_ORDER) {
            if (direction != preferred) {
                SurfaceHit hit = findSurface(direction, box);
                if (hit != null) {
                    return hit;
                }
            }
        }

        return null;
    }

    public boolean canAttachToSurface(Direction direction, AABB referenceBox) {
        return findSurface(direction, referenceBox) != null;
    }

    @Nullable
    public Direction findAttachment(Direction preferred, AABB referenceBox, Direction... fallbacks) {
        SurfaceHit hit = findSurface(preferred, referenceBox);
        if (hit != null) {
            return hit.direction();
        }
        for (Direction fallback : fallbacks) {
            hit = findSurface(fallback, referenceBox);
            if (hit != null) {
                return hit.direction();
            }
        }
        return null;
    }

    @Nullable
    private SurfaceHit findSurface(Direction direction, AABB referenceBox) {
        for (Vec3 probe : SURFACE_PROBES) {
            SurfaceHit hit = scanSurface(direction, referenceBox.move(probe.x, probe.y, probe.z));
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    @Nullable
    private SurfaceHit scanSurface(Direction direction, AABB referenceBox) {
        AABB shrunken = referenceBox.inflate(-SURFACE_SHRINK);
        AABB query = shrunken.expandTowards(direction.getStepX() * SURFACE_CHECK_STEP,
                direction.getStepY() * SURFACE_CHECK_STEP,
                direction.getStepZ() * SURFACE_CHECK_STEP).inflate(SURFACE_QUERY_EPSILON);

        if (direction.getAxis().isHorizontal()) {
            query = query.inflate(0.0D, SURFACE_CHECK_STEP, 0.0D);
        }

        for (VoxelShape shape : level().getBlockCollisions(this, query)) {
            if (shape.isEmpty()) {
                continue;
            }
            for (AABB blockBox : shape.toAabbs()) {
                if (!overlapsOnOtherAxes(shrunken, blockBox, direction)) {
                    continue;
                }
                double gap = surfaceGap(shrunken, blockBox, direction);
                if (gap <= SURFACE_GAP_TOLERANCE) {
                    if (direction != Direction.UP || blockBox.minY <= shrunken.maxY + SURFACE_CHECK_STEP + SURFACE_GAP_TOLERANCE) {
                        Vec3 anchor = new Vec3(blockBox.getCenter().x, blockBox.getCenter().y, blockBox.getCenter().z);
                        return new SurfaceHit(direction, anchor, normalVector(direction));
                    }
                }
            }
        }

        return null;
    }

    private boolean overlapsOnOtherAxes(AABB entityBox, AABB blockBox, Direction direction) {
        return switch (direction.getAxis()) {
            case X -> overlap(entityBox.minY, entityBox.maxY, blockBox.minY, blockBox.maxY) > SURFACE_OVERLAP_EPS
                    && overlap(entityBox.minZ, entityBox.maxZ, blockBox.minZ, blockBox.maxZ) > SURFACE_OVERLAP_EPS;
            case Y -> overlap(entityBox.minX, entityBox.maxX, blockBox.minX, blockBox.maxX) > SURFACE_OVERLAP_EPS
                    && overlap(entityBox.minZ, entityBox.maxZ, blockBox.minZ, blockBox.maxZ) > SURFACE_OVERLAP_EPS;
            case Z -> overlap(entityBox.minX, entityBox.maxX, blockBox.minX, blockBox.maxX) > SURFACE_OVERLAP_EPS
                    && overlap(entityBox.minY, entityBox.maxY, blockBox.minY, blockBox.maxY) > SURFACE_OVERLAP_EPS;
        };
    }

    private double overlap(double minA, double maxA, double minB, double maxB) {
        return Math.min(maxA, maxB) - Math.max(minA, minB);
    }

    private double surfaceGap(AABB entityBox, AABB blockBox, Direction direction) {
        return switch (direction) {
            case EAST -> blockBox.minX - entityBox.maxX;
            case WEST -> entityBox.minX - blockBox.maxX;
            case UP -> blockBox.minY - entityBox.maxY;
            case DOWN -> entityBox.minY - blockBox.maxY;
            case SOUTH -> blockBox.minZ - entityBox.maxZ;
            case NORTH -> entityBox.minZ - blockBox.maxZ;
        };
    }

    private Vec3 normalVector(Direction direction) {
        return new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

}
