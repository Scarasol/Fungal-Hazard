package com.scarasol.fungalhazard.entity.ai;

import com.scarasol.fungalhazard.entity.arachnid.AbstractArachnidFungalZombie;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Navigation that keeps arachnid zombies glued to nearby surfaces while performing
 * lightweight detours inspired by Nyfaria's climber implementation. The navigator
 * avoids heavy 3D path-finding and instead derives short, validated offsets when a
 * forward sweep detects an overhang, keeping the runtime cost low enough for large
 * groups (≈70) of entities.
 */
public class ArachnidSurfaceNavigation<T extends AbstractArachnidFungalZombie> extends FungalZombieGroundPathNavigation<T> {

    private static final double DETOUR_STEP = 0.5D;
    private static final double DETOUR_VERTICAL = 0.7D;
    private static final int MAX_DETOUR_STEPS = 6;
    private static final double CLEARANCE_SHRINK = 0.05D;
    private static final double SWEEP_STEP = 0.35D;
    private static final int SWEEP_SAMPLES = 3;
    private static final double TARGET_REACHED_SQR = 0.25D;
    private static final int PLAN_LIFETIME = 20;
    private static final int PLAN_COOLDOWN_TICKS = 6;

    private static final Vec3 DOWN_OFFSET = new Vec3(0.0D, -DETOUR_VERTICAL, 0.0D);
    private static final Vec3 UP_OFFSET = new Vec3(0.0D, DETOUR_VERTICAL, 0.0D);
    private static final Direction[] HORIZONTAL_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    @Nullable
    private Plan activePlan;
    private int planCooldown;
    @Nullable
    private BlockPos fallbackGoal;

    public ArachnidSurfaceNavigation(T mob, Level level) {
        super(mob, level);
    }

    @Override
    public Path createPath(BlockPos pos, int depth) {
        this.fallbackGoal = pos;
        return super.createPath(pos, depth);
    }

    @Override
    public Path createPath(Entity entity, int depth) {
        this.fallbackGoal = entity.blockPosition();
        return super.createPath(entity, depth);
    }

    @Override
    public boolean moveTo(Entity entity, double speed) {
        Path path = this.createPath(entity, 0);
        if (path != null) {
            return this.moveTo(path, speed);
        }
        this.fallbackGoal = entity.blockPosition();
        this.speedModifier = speed;
        return true;
    }

    @Override
    public boolean moveTo(double x, double y, double z, double speed) {
        BlockPos target = BlockPos.containing(x, y, z);
        Path path = this.createPath(target, 0);
        if (path != null) {
            return this.moveTo(path, speed);
        }
        this.fallbackGoal = target;
        this.speedModifier = speed;
        return true;
    }

    @Override
    public void tick() {
        setSpeedModifier(zombie().getState().speedModifier());
        if (handleSurfaceTraversal()) {
            return;
        }

        if (!this.isDone()) {
            super.tick();
            return;
        }

        if (this.fallbackGoal != null) {
            if (!isWithinReach(this.fallbackGoal)) {
                Vec3 target = Vec3.atCenterOf(this.fallbackGoal);
                this.mob.getMoveControl().setWantedPosition(target.x, target.y, target.z, this.speedModifier);
            } else {
                this.fallbackGoal = null;
            }
        }
    }

    private boolean handleSurfaceTraversal() {
        Direction surface = zombie().getCrawlingDirection();
        if (surface == null) {
            clearPlan();
            return false;
        }

        Vec3 desired = computeDesiredVector();
        Vec3 forward = projectOntoSurface(surface, desired);
        if (forward == null || forward.lengthSqr() < 1.0E-4D) {
            forward = fallbackForward(surface);
        }
        if (forward == null || forward.lengthSqr() < 1.0E-4D) {
            clearPlan();
            return false;
        }

        boolean blocked = isBlockedAhead(surface, forward);
        updatePlan(blocked, surface, forward, desired);

        if (activePlan != null) {
            if (activePlan.isReached(this.mob)) {
                activePlan = null;
                return false;
            }
            drivePlan();
            return true;
        }

        return false;
    }

    private void clearPlan() {
        activePlan = null;
        planCooldown = Math.max(planCooldown - 1, 0);
    }

    private void updatePlan(boolean blocked, Direction surface, Vec3 forward, @Nullable Vec3 desired) {
        if (activePlan != null) {
            if (activePlan.isReached(this.mob) || !activePlan.isValid(this.mob, this.level, zombie(), forward)) {
                activePlan = null;
            } else {
                activePlan.ticksRemaining--;
                if (activePlan.ticksRemaining <= 0) {
                    activePlan = null;
                }
            }
        }

        if (!blocked) {
            planCooldown = Math.max(planCooldown - 1, 0);
            return;
        }

        if (activePlan != null) {
            planCooldown = PLAN_COOLDOWN_TICKS;
            return;
        }

        if (planCooldown > 0) {
            planCooldown--;
            return;
        }

        Plan plan = surface.getAxis().isHorizontal()
                ? createWallPlan(surface, forward, desired)
                : createCeilingPlan(surface, forward);

        if (plan != null) {
            activePlan = plan;
            planCooldown = PLAN_COOLDOWN_TICKS;
        } else {
            planCooldown = Math.max(planCooldown - 1, 0);
        }
    }

    @Nullable
    private Plan createWallPlan(Direction surface, Vec3 forward, @Nullable Vec3 desired) {
        Direction[] lateralOrder = orderLaterals(surface, desired);
        for (Direction lateral : lateralOrder) {
            Plan result = tryWallDetour(surface, forward, lateral);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Nullable
    private Plan tryWallDetour(Direction surface, Vec3 forward, Direction lateral) {
        Vec3 lateralVector = horizontalVector(lateral);
        if (lateralVector.lengthSqr() < 1.0E-6D) {
            return null;
        }

        Vec3 unit = lateralVector.normalize();
        for (int step = 1; step <= MAX_DETOUR_STEPS; step++) {
            Vec3 sideways = unit.scale(DETOUR_STEP * step);

            Plan plan = evaluateOffset(sideways, surface, forward, lateral, true);
            if (plan != null) {
                return plan;
            }

            plan = evaluateOffset(sideways.add(UP_OFFSET), surface, forward, lateral, true);
            if (plan != null) {
                return plan;
            }

            plan = evaluateOffset(sideways.add(DOWN_OFFSET), surface, forward, lateral, false);
            if (plan != null) {
                return plan;
            }
        }

        return null;
    }

    @Nullable
    private Plan createCeilingPlan(Direction surface, Vec3 forward) {
        Vec3 perpendicular = new Vec3(-forward.z, 0.0D, forward.x);
        if (perpendicular.lengthSqr() < 1.0E-6D) {
            Direction facing = this.mob.getDirection();
            perpendicular = new Vec3(-facing.getStepZ(), 0.0D, facing.getStepX());
        }
        if (perpendicular.lengthSqr() < 1.0E-6D) {
            return null;
        }

        Vec3 unit = perpendicular.normalize();
        Plan plan = evaluateCeilingOffset(unit, surface, forward);
        if (plan != null) {
            return plan;
        }
        return evaluateCeilingOffset(unit.scale(-1.0D), surface, forward);
    }

    @Nullable
    private Plan evaluateCeilingOffset(Vec3 lateral, Direction surface, Vec3 forward) {
        if (lateral.lengthSqr() < 1.0E-6D) {
            return null;
        }
        Vec3 unit = lateral.normalize();

        for (int step = 1; step <= MAX_DETOUR_STEPS; step++) {
            Vec3 sideways = unit.scale(DETOUR_STEP * step);

            Plan plan = evaluateOffset(sideways, surface, forward, null, true);
            if (plan != null) {
                return plan;
            }

            plan = evaluateOffset(sideways.add(DOWN_OFFSET), surface, forward, null, false);
            if (plan != null) {
                return plan;
            }
        }

        return null;
    }

    @Nullable
    private Plan evaluateOffset(Vec3 offset, Direction surface, Vec3 forward, @Nullable Direction lateral, boolean preferSurface) {
        if (offset.lengthSqr() < 1.0E-6D) {
            return null;
        }
        if (!isSweepClear(offset)) {
            return null;
        }

        AABB moved = this.mob.getBoundingBox().move(offset.x, offset.y, offset.z);
        if (!this.level.noCollision(this.mob, moved)) {
            return null;
        }

        Direction[] attachmentOrder = buildAttachmentOrder(surface, lateral, preferSurface);
        Direction attachment = zombie().findAttachment(surface, moved, attachmentOrder);
        if (attachment == null) {
            return null;
        }

        AABB shrunken = moved.inflate(-CLEARANCE_SHRINK);
        if (!hasHeadClearance(shrunken, attachment, forward)) {
            return null;
        }

        Vec3 center = moved.getCenter();
        double targetY = attachment == Direction.UP ? this.mob.getY() : center.y;
        return new Plan(new Vec3(center.x, targetY, center.z), attachment, PLAN_LIFETIME);
    }

    private boolean isSweepClear(Vec3 offset) {
        int samples = Math.max(1, (int) Math.ceil(offset.length() / SWEEP_STEP));
        Vec3 step = offset.scale(1.0D / samples);
        AABB base = this.mob.getBoundingBox().inflate(-CLEARANCE_SHRINK);
        for (int i = 1; i <= samples; i++) {
            AABB moved = base.move(step.x * i, step.y * i, step.z * i);
            if (!this.level.noCollision(this.mob, moved)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasHeadClearance(AABB baseBox, Direction attachment, Vec3 forward) {
        Vec3 normal = normalVector(attachment).scale(0.05D);
        Vec3 step = forward.lengthSqr() > 1.0E-4D ? forward.normalize().scale(SWEEP_STEP) : Vec3.ZERO;
        for (int i = 1; i <= SWEEP_SAMPLES; i++) {
            AABB moved = baseBox.move(normal.x + step.x * i, normal.y + step.y * i, normal.z + step.z * i);
            if (!this.level.noCollision(this.mob, moved)) {
                return false;
            }
        }
        return true;
    }

    private boolean isBlockedAhead(Direction surface, Vec3 forward) {
        if (forward.lengthSqr() < 1.0E-4D) {
            return false;
        }
        Vec3 normal = normalVector(surface).scale(0.05D);
        Vec3 step = forward.normalize().scale(SWEEP_STEP);
        AABB base = this.mob.getBoundingBox().inflate(-CLEARANCE_SHRINK);
        for (int i = 1; i <= SWEEP_SAMPLES; i++) {
            AABB moved = base.move(normal.x + step.x * i, normal.y + step.y * i, normal.z + step.z * i);
            if (!this.level.noCollision(this.mob, moved)) {
                return true;
            }
        }
        return false;
    }

    private Vec3 projectOntoSurface(Direction surface, @Nullable Vec3 desired) {
        if (desired == null) {
            return null;
        }
        Vec3 normal = normalVector(surface);
        Vec3 projected = desired.subtract(normal.scale(desired.dot(normal)));
        return projected.lengthSqr() > 1.0E-4D ? projected.normalize() : null;
    }

    private Vec3 fallbackForward(Direction surface) {
        if (surface.getAxis().isHorizontal()) {
            return new Vec3(0.0D, 1.0D, 0.0D);
        }
        Direction facing = this.mob.getDirection();
        Vec3 forward = new Vec3(facing.getStepX(), 0.0D, facing.getStepZ());
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = new Vec3(-surface.getStepZ(), 0.0D, surface.getStepX());
        }
        return forward.lengthSqr() > 1.0E-4D ? forward.normalize() : null;
    }

    @Nullable
    private Vec3 computeDesiredVector() {
        if (activePlan != null) {
            return activePlan.target.subtract(this.mob.position());
        }
        if (this.path != null && !this.path.isDone()) {
            Vec3 next = Vec3.atCenterOf(this.path.getNextNodePos());
            return next.subtract(this.mob.position());
        }
        if (this.fallbackGoal != null) {
            Vec3 goal = Vec3.atCenterOf(this.fallbackGoal);
            return goal.subtract(this.mob.position());
        }
        Vec3 motion = this.mob.getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-4D) {
            return motion;
        }
        Vec3 look = this.mob.getLookAngle();
        return look.lengthSqr() > 1.0E-4D ? look : null;
    }

    private void drivePlan() {
        if (activePlan == null) {
            return;
        }
        double targetY = activePlan.attachment == Direction.UP ? this.mob.getY() : activePlan.target.y;
        this.mob.getMoveControl().setWantedPosition(activePlan.target.x, targetY, activePlan.target.z, this.speedModifier);
    }

    private boolean isWithinReach(BlockPos pos) {
        double radius = Math.max(this.mob.getBbWidth(), 1.0D);
        return Vec3.atCenterOf(pos).distanceToSqr(this.mob.position()) <= radius * radius;
    }

    private Direction[] orderLaterals(Direction surface, @Nullable Vec3 desired) {
        Direction cw = surface.getClockWise();
        Direction ccw = surface.getCounterClockWise();
        if (desired == null) {
            return new Direction[]{cw, ccw};
        }

        Vec3 horizontal = new Vec3(desired.x, 0.0D, desired.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            return new Direction[]{cw, ccw};
        }

        Vec3 normalized = horizontal.normalize();
        double dotCw = normalized.dot(horizontalVector(cw));
        double dotCcw = normalized.dot(horizontalVector(ccw));
        return dotCw >= dotCcw ? new Direction[]{cw, ccw} : new Direction[]{ccw, cw};
    }

    private Vec3 horizontalVector(Direction direction) {
        return switch (direction) {
            case NORTH -> new Vec3(0.0D, 0.0D, -1.0D);
            case SOUTH -> new Vec3(0.0D, 0.0D, 1.0D);
            case WEST -> new Vec3(-1.0D, 0.0D, 0.0D);
            case EAST -> new Vec3(1.0D, 0.0D, 0.0D);
            default -> Vec3.ZERO;
        };
    }

    private Direction[] buildAttachmentOrder(Direction surface, @Nullable Direction lateral, boolean preferSurface) {
        Direction forbidden = surface.getOpposite();
        Direction[] buffer = new Direction[8];
        int size = 0;

        if (preferSurface) {
            size = appendDirection(buffer, size, surface, forbidden);
        }
        if (lateral != null) {
            size = appendDirection(buffer, size, lateral, forbidden);
            size = appendDirection(buffer, size, lateral.getOpposite(), forbidden);
        }
        if (!preferSurface) {
            size = appendDirection(buffer, size, surface, forbidden);
        }
        size = appendDirection(buffer, size, Direction.UP, forbidden);
        for (Direction dir : HORIZONTAL_DIRECTIONS) {
            size = appendDirection(buffer, size, dir, forbidden);
        }
        size = appendDirection(buffer, size, Direction.DOWN, forbidden);

        Direction[] attachments = new Direction[size];
        System.arraycopy(buffer, 0, attachments, 0, size);
        return attachments;
    }

    private int appendDirection(Direction[] buffer, int size, Direction candidate, Direction forbidden) {
        if (candidate == null || candidate == forbidden) {
            return size;
        }
        for (int i = 0; i < size; i++) {
            if (buffer[i] == candidate) {
                return size;
            }
        }
        buffer[size] = candidate;
        return size + 1;
    }

    private Vec3 normalVector(Direction direction) {
        Vec3 normal = Vec3.atLowerCornerOf(direction.getOpposite().getNormal());
        return normal.lengthSqr() > 0.0D ? normal.normalize() : Vec3.ZERO;
    }

    @SuppressWarnings("unchecked")
    private T zombie() {
        return (T) this.mob;
    }

    private static final class Plan {
        final Vec3 target;
        final Direction attachment;
        int ticksRemaining;

        private Plan(Vec3 target, Direction attachment, int ticksRemaining) {
            this.target = target;
            this.attachment = attachment;
            this.ticksRemaining = ticksRemaining;
        }

        boolean isReached(Entity entity) {
            return entity.position().distanceToSqr(this.target) <= TARGET_REACHED_SQR;
        }

        boolean isValid(Entity entity, Level level, AbstractArachnidFungalZombie zombie, Vec3 forward) {
            Vec3 delta = this.target.subtract(entity.position());
            AABB moved = entity.getBoundingBox().move(delta.x, delta.y, delta.z);
            if (!level.noCollision(entity, moved)) {
                return false;
            }
            Direction resolved = zombie.findAttachment(this.attachment, moved);
            if (resolved == null) {
                return false;
            }
            Vec3 normal = normalVector(resolved).scale(0.05D);
            Vec3 step = forward.lengthSqr() > 1.0E-4D ? forward.normalize().scale(SWEEP_STEP) : Vec3.ZERO;
            AABB shrunken = moved.inflate(-CLEARANCE_SHRINK);
            for (int i = 1; i <= SWEEP_SAMPLES; i++) {
                AABB box = shrunken.move(normal.x + step.x * i, normal.y + step.y * i, normal.z + step.z * i);
                if (!level.noCollision(entity, box)) {
                    return false;
                }
            }
            return true;
        }
    }
}

