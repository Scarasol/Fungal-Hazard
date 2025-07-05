package com.scarasol.fungalhazard.entity.goal;

import com.scarasol.fungalhazard.entity.AbstractFungalZombie;
import com.scarasol.sona.util.SonaMath;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * @author Scarasol
 */
public class FungalZombiePatrolGoal<T extends AbstractFungalZombie> extends Goal {
    private static final int NAVIGATION_FAILED_COOLDOWN = 200;
    private final T mob;
    private final double speedModifier;
    private final double leaderSpeedModifier;
    private long cooldownUntil;

    public FungalZombiePatrolGoal(T mob, double speedModifier, double leaderSpeedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.leaderSpeedModifier = leaderSpeedModifier;
        this.cooldownUntil = -1L;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        boolean flag = this.mob.level().getGameTime() >= this.cooldownUntil;
        return this.mob.getTarget() == null && !this.mob.isVehicle() && flag && this.mob.canJoinPatrol();
    }

    @Override
    public void stop() {
        if (!this.mob.canJoinPatrol()) {
            this.mob.setPatrolLeader(false);
            this.mob.setPatrolTarget(null);
            this.mob.setPatrolling(false);
        }
    }

    @Override
    public void tick() {
        boolean flag = this.mob.isPatrolLeader();
        PathNavigation pathNavigation = this.mob.getNavigation();
        if (pathNavigation.isDone()) {
            List<AbstractFungalZombie> list = this.findPatrolCompanions();
            if (list.isEmpty()) {
                this.mob.setPatrolLeader(false);
                this.mob.setPatrolling(false);
                this.cooldownUntil = this.mob.level().getGameTime() + NAVIGATION_FAILED_COOLDOWN;
                return;
            }
            if (!this.mob.isHasLeader() && this.mob.canBeLeader()) {
                flag = true;
                this.mob.setPatrolLeader(true);
            }
            if (!this.mob.hasPatrolTarget() || this.mob.getPatrolTarget().closerToCenterThan(this.mob.position(), 10.0D)) {
                if (flag) {
                    this.mob.findPatrolTarget();
                } else {
                    this.mob.setPatrolTarget(null);
                    this.mob.setPatrolling(false);
                    return;
                }
            }
            if (this.mob.hasPatrolTarget() && flag) {
                Vec3 vec3 = Vec3.atBottomCenterOf(this.mob.getPatrolTarget());
                Vec3 vec31 = this.mob.position();
                Vec3 vec32 = vec31.subtract(vec3);
                vec3 = vec32.yRot(90.0F * (-0.5F + this.mob.level().random.nextFloat())).scale(0.4D).add(vec3);
                Vec3 vec33 = vec3.subtract(vec31).normalize().scale(10.0D).add(vec31);
                BlockPos blockpos = BlockPos.containing(vec33);
                blockpos = this.mob.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockpos);
                if (!pathNavigation.moveTo(blockpos.getX(), blockpos.getY(), blockpos.getZ(), this.mob.isInWaterOrBubble() ? leaderSpeedModifier * 1.5 : this.leaderSpeedModifier) && flag) {
                    this.moveRandomly();
                }
                for (AbstractFungalZombie patrollingZombie : list) {
                    patrollingZombie.setPatrolTarget(blockpos);
                    patrollingZombie.getNavigation().stop();
                    patrollingZombie.getNavigation().moveTo(blockpos.getX(), blockpos.getY(), blockpos.getZ(), this.mob.isInWaterOrBubble() ? speedModifier * 1.5 : this.speedModifier);
                }
            }
        } else if (!flag && this.mob.isHasLeader()) {
            AbstractFungalZombie leader = this.mob.getLeader();
            double distanceToSqr = leader.distanceToSqr(this.mob);
            if (distanceToSqr > 256) {
                this.mob.setHasLeader(false);
                this.mob.setLeader(null);
            } else if (distanceToSqr > 64 && leader.hasPatrolTarget()) {
                Vec3 distance = leader.getPatrolTarget().getCenter().subtract(leader.position());
                Vec3 angle = this.mob.position().subtract(leader.position());
                if (SonaMath.vectorDegreeCalculate(distance, angle) > 45) {
                    this.mob.getNavigation().setSpeedModifier(this.mob.isInWaterOrBubble() ? speedModifier * 1.3 * 1.5 : speedModifier * 1.3);
                }
            }
        }
    }

    private List<AbstractFungalZombie> findPatrolCompanions() {
        boolean isLeader = this.mob.isPatrolLeader();
        this.mob.setHasLeader(isLeader);
        AbstractFungalZombie leader = isLeader ? this.mob : null;
        this.mob.setLeader(leader);
        return this.mob.level().getEntitiesOfClass(AbstractFungalZombie.class,
                this.mob.getBoundingBox().inflate(16.0D),
                (abstractFungalZombie) -> {
                    if (abstractFungalZombie.is(this.mob) || !abstractFungalZombie.canJoinPatrol()) {
                        return false;
                    }
                    if (abstractFungalZombie.isPatrolLeader()) {
                        this.mob.setHasLeader(true);
                        this.mob.setLeader(abstractFungalZombie);
                        this.mob.setPatrolLeader(false);
                    }
                    return true;
                });
    }

    private boolean moveRandomly() {
        RandomSource randomsource = this.mob.getRandom();
        BlockPos blockpos = this.mob.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                this.mob.blockPosition().offset(-8 + randomsource.nextInt(16),
                        0, -8 + randomsource.nextInt(16)));
        return this.mob.getNavigation().moveTo(blockpos.getX(), blockpos.getY(), blockpos.getZ(), this.speedModifier);
    }
}
