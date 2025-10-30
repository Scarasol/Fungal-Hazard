package com.nyfaria.nyfsspiders.common.entity.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;

import java.util.function.Predicate;

/**
 * 兼容 Nyfaria Spiders 导航工具的最小接口集合。大部分方法提供默认实现，
 * 方便实体在未覆写时仍保持与原版逻辑一致的表现。
 */
public interface IAdvancedPathFindingEntity {

    default Direction getGroundSide() {
        return Direction.DOWN;
    }

    void onPathingObstructed(Direction facing);

    default int getMaxStuckCheckTicks() {
        return 40;
    }

    default float getBridgePathingMalus(Mob entity, BlockPos pos, Node fallPathPoint) {
        return -1.0F;
    }

    default float getPathingMalus(BlockGetter cache, Mob entity, BlockPathTypes nodeType,
                                   BlockPos pos, Vec3i direction, Predicate<Direction> sides) {
        return entity.getPathfindingMalus(nodeType);
    }

    default void pathFinderCleanup() {
    }
}
