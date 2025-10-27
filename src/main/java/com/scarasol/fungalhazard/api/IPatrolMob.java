package com.scarasol.fungalhazard.api;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;

/**
 * @author Scarasol
 */
public interface IPatrolMob {

    boolean isHasLeader();

    void setHasLeader(boolean hasLeader);

    LivingEntity getLeader();

    void setLeader(LivingEntity leader);

    boolean canBeLeader();

    boolean isPatrolLeader();

    BlockPos getPatrolTarget();

    boolean hasPatrolTarget();

    void setPatrolTarget(BlockPos patrolTarget);

    void setPatrolLeader(boolean patrolLeader);

    boolean canJoinPatrolNow();

    boolean canJoinPatrol();

    void findPatrolTarget();

    boolean isPatrolling();

    void setPatrolling(boolean patrolling);

    default void readPatrolInfo(CompoundTag compoundTag) {
        if (compoundTag.contains("PatrolTarget")) {
            setPatrolTarget(NbtUtils.readBlockPos(compoundTag.getCompound("PatrolTarget")));
        }
        setPatrolLeader(compoundTag.getBoolean("PatrolLeader"));
        setPatrolling(compoundTag.getBoolean("Patrolling"));
    }

    default void savePatrolInfo(CompoundTag compoundTag) {
        BlockPos patrolTarget = getPatrolTarget();
        if (patrolTarget != null) {
            compoundTag.put("PatrolTarget", NbtUtils.writeBlockPos(patrolTarget));
        }

        compoundTag.putBoolean("PatrolLeader", isPatrolLeader());
        compoundTag.putBoolean("Patrolling", isPatrolling());
    }
}
