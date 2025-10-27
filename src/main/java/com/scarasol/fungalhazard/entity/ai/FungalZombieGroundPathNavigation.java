package com.scarasol.fungalhazard.entity.ai;

import com.google.common.collect.Lists;
import com.scarasol.fungalhazard.api.IFungalZombie;
import com.scarasol.fungalhazard.api.IPatrolMob;
import com.scarasol.sona.util.SonaMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FungalZombieGroundPathNavigation<T extends Mob & IPatrolMob & IFungalZombie> extends GroundPathNavigation {

    private final T zombie;
    private long lastJumpTime;
    private boolean isStuck;
    private boolean stuckJumpTried;
    private boolean timeoutJumpTried;

    public FungalZombieGroundPathNavigation(T mob, Level level) {
        super(mob, level);
        this.zombie = mob;
    }

    @Override
    public Path createPath(BlockPos blockPos, int i1) {
        if (!zombie.getState().canMove()) {
            return null;
        }
        LivingEntity leader = zombie.getLeader();
        if (!zombie.isPatrolLeader() && zombie.isPatrolling() && leader instanceof Mob mob && mob instanceof IPatrolMob patrolMob) {
            Path path = mob.getNavigation().getPath();
            if (path != null) {
                if (leader.distanceToSqr(zombie) < 64 && patrolMob.hasPatrolTarget()) {
                    Vec3 distance = patrolMob.getPatrolTarget().getCenter().subtract(leader.position());
                    Vec3 angle = this.zombie.position().subtract(leader.position());
                    if (SonaMath.vectorDegreeCalculate(distance, angle) > 45) {
                        return getLeadersPath();
                    }
                }
                if (path.getEndNode() != null) {
                    return super.createPath(path.getEndNode().asBlockPos(), i1);
                }
            }
        }
        return super.createPath(blockPos, i1);
    }

    @Override
    public void tick() {
        setSpeedModifier(zombie.getState().speedModifier());
        super.tick();
//        if (zombie.isPatrolling() && !zombie.isPatrolLeader() && (level.getGameTime() + zombie.getId()) % 20 == 0) {
//            applySeparationForce();
//        }
    }

//    /**
//     * 简化版鸟群分离规则
//     * 避免多个僵尸走在完全相同的路径点上
//     */
//    private void applySeparationForce() {
//        double separationRadius = zombie.getBbWidth() / 1.414;  // 分离半径（方块）
//        double separationStrength = 0.05; // 分离强度，越大避让越快
//
//        Vec3 myPos = zombie.position();
//        Vec3 offset = Vec3.ZERO;
//
//        // 搜索半径内的实体
//        List<AbstractHumanoidFungalZombie> nearby = zombie.level().getEntitiesOfClass(
//                AbstractHumanoidFungalZombie.class,
//                zombie.getBoundingBox().inflate(separationRadius),
//                e -> e != zombie && e.isAlive() && e.isPatrolling()
//        );
//
//        for (AbstractHumanoidFungalZombie other : nearby) {
//            Vec3 diff = myPos.subtract(other.position());
//            double distSqr = diff.lengthSqr();
//            if (distSqr < 0.0001) {
//                continue; // 避免除零
//            }
//            double weight = 1.0 / distSqr;  // 距离越近，推力越大
//            offset = offset.add(diff.normalize().scale(weight));
//        }
//
//        if (!offset.equals(Vec3.ZERO)) {
//            Vec3 oldDelta = zombie.getDeltaMovement();
//            Vec3 newDelta = oldDelta.add(offset.normalize().scale(separationStrength));
//            zombie.setDeltaMovement(newDelta);
//        }
//    }

    @Override
    protected void followThePath() {
        Vec3 vec3 = this.getTempMobPos();
        if (zombie.isPatrolling()) {
            this.maxDistanceToWaypoint = Math.max(4, this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F);
        } else {
            this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
        }
        Vec3i vec3i = this.path.getNextNodePos();
        double d0 = Math.abs(this.mob.getX() - ((double) vec3i.getX() + (this.mob.getBbWidth() + 1) / 2D)); //Forge: Fix MC-94054
        double d1 = Math.abs(this.mob.getY() - (double) vec3i.getY());
        double d2 = Math.abs(this.mob.getZ() - ((double) vec3i.getZ() + (this.mob.getBbWidth() + 1) / 2D)); //Forge: Fix MC-94054
        boolean flag = d0 <= (double) this.maxDistanceToWaypoint && d2 <= (double) this.maxDistanceToWaypoint && d1 < 1D; //Forge: Fix MC-94054
        if (flag || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(vec3)) {
            this.path.advance();
        }

        this.doStuckDetection(vec3);
    }

//    @Override
//    protected void doStuckDetection(Vec3 pos) {
//        int difference = this.tick - this.lastStuckCheck;
//        float speed = this.mob.getSpeed();
//        long gameTime = level.getGameTime();
//        if (difference > 100) {
//            float f = speed >= 1.0F ? speed : speed * speed;
//            float f1 = f * difference * 0.25F;
//            if (pos.distanceToSqr(this.lastStuckCheckPos) < (double) (f1 * f1)) {
//                if (this.zombie.isPatrolling() && !stuckJumpTried) {
//                    this.mob.getJumpControl().jump();
//                    lastJumpTime = gameTime;
//                } else {
//                    this.isStuck = true;
//                    this.stop();
//                }
//                stuckJumpTried = !stuckJumpTried;
//            } else {
//                this.isStuck = false;
//            }
//            this.lastStuckCheck = this.tick;
//            this.lastStuckCheckPos = pos;
//        }
//
//        if (this.path != null && !this.path.isDone()) {
//            Vec3i vec3i = this.path.getNextNodePos();
//            if (vec3i.equals(this.timeoutCachedNode)) {
//                this.timeoutTimer += gameTime - this.lastTimeoutCheck;
//            } else {
//                timeoutJumpTried = false;
//                this.timeoutCachedNode = vec3i;
//                timeoutTimer = 0;
//                double d0 = pos.distanceTo(Vec3.atBottomCenterOf(this.timeoutCachedNode));
//                this.timeoutLimit = speed > 0.0F ? d0 / speed * 20.0D : 0.0D;
//            }
//            if (this.timeoutLimit > 0.0D) {
//                if ((double) this.timeoutTimer > this.timeoutLimit * 3.0D) {
//                    if (this.zombie.isPatrolling() && !timeoutJumpTried) {
//                        this.mob.getJumpControl().jump();
//                        this.timeoutTimer = 0;
//                    } else
//                        this.timeoutPath();
//                    timeoutJumpTried = !timeoutJumpTried;
//                }
//
//            }
//            this.lastTimeoutCheck = gameTime;
//        }
//    }
//
//    private void timeoutPath() {
//        this.resetStuckTimeout();
//        this.stop();
//    }
//
//    private void resetStuckTimeout() {
//        this.timeoutCachedNode = Vec3i.ZERO;
//        this.timeoutTimer = 0L;
//        this.timeoutLimit = 0.0D;
//        this.isStuck = false;
//    }
//
//    @Override
//    public boolean isStuck() {
//        return this.isStuck;
//    }
//
//    public boolean canJumpForward() {
//        BlockPos currentPos = mob.blockPosition();
//        Direction facing = mob.getDirection(); // 或用自定义方向
//        BlockPos front = currentPos.relative(facing);
//        BlockPos frontAbove = front.above();
//        BlockPos frontTwoAbove = frontAbove.above();
//
//        BlockState ground = level.getBlockState(front);
//        BlockState head = level.getBlockState(frontAbove);
//        BlockState aboveHead = level.getBlockState(frontTwoAbove);
//
//        boolean isObstacleLowEnough = !ground.is(BlockTags.DOORS) && !ground.is(BlockTags.FENCES); // 1格高
//        boolean spaceAboveClear = head.getCollisionShape(level, frontAbove).isEmpty() && aboveHead.getCollisionShape(level, frontTwoAbove).isEmpty(); // 足够空间跳进去
//        boolean destinationIsSolid = !level.getBlockState(front.below()).isAir();
//
//        return isObstacleLowEnough && spaceAboveClear && mob.onGround() && destinationIsSolid;
//    }

    private boolean shouldTargetNextNodeInDirection(Vec3 pos) {
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            return false;
        } else {
            Vec3 vec3 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
            if (!pos.closerThan(vec3, 2.0D)) {
                return false;
            } else if (this.canMoveDirectly(pos, this.path.getNextEntityPos(this.mob))) {
                return true;
            } else {
                Vec3 vec31 = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
                Vec3 vec32 = vec3.subtract(pos);
                Vec3 vec33 = vec31.subtract(pos);
                double d0 = vec32.lengthSqr();
                double d1 = vec33.lengthSqr();
                boolean flag = d1 < d0;
                boolean flag1 = d0 < 0.5D;
                if (!flag && !flag1) {
                    return false;
                } else {
                    Vec3 vec34 = vec32.normalize();
                    Vec3 vec35 = vec33.normalize();
                    return vec35.dot(vec34) < 0.0D;
                }
            }
        }
    }

    @Nullable
    public Path getLeadersPath() {
        LivingEntity leader = this.zombie.getLeader();
        if (leader != null && !this.zombie.isPatrolLeader() && leader instanceof Mob mob) {
            return pathCopy(mob.getNavigation().getPath());
        }
        return null;
    }

    @Nullable
    public Path pathCopy(Path path) {
        if (path == null) {
            return null;
        }
        List<Node> nodeList = Lists.newArrayList();
        for (int i = path.getNextNodeIndex(); i < path.getNodeCount(); i++) {
            Node node = path.getNode(i);
            nodeList.add(node);
        }
        return new Path(nodeList, path.getTarget(), path.canReach());
    }

//    public Node offsetNode(Node node, Random random) {
//        int x = node.x - 2 + random.nextInt(5);
//        int z = node.z - 2 + random.nextInt(5);
//        return node.cloneAndMove(x, node.y, z);
//    }
}
