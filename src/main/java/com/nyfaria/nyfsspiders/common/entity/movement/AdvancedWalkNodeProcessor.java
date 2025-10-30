package com.nyfaria.nyfsspiders.common.entity.movement;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * Simplified stand-in for the Nyfaria walk node processor. The original mod
 * exposes a large collection of hooks to support wall and ceiling traversal.
 * For the purposes of this port we keep the interface surface compatible while
 * delegating all heavy lifting back to {@link WalkNodeEvaluator}. This keeps the
 * imported navigation helpers compiling without altering the existing fungal
 * hazard path finding.
 */
public class AdvancedWalkNodeProcessor extends WalkNodeEvaluator {
    private boolean startPathOnGround = true;
    private boolean canPathWalls;
    private boolean canPathCeiling;
    private boolean checkObstructions = true;

    public void setStartPathOnGround(boolean startPathOnGround) {
        this.startPathOnGround = startPathOnGround;
    }

    public void setCanPathWalls(boolean canPathWalls) {
        this.canPathWalls = canPathWalls;
    }

    public void setCanPathCeiling(boolean canPathCeiling) {
        this.canPathCeiling = canPathCeiling;
    }

    public void setCheckObstructions(boolean checkObstructions) {
        this.checkObstructions = checkObstructions;
    }

    public boolean isStartPathOnGround() {
        return startPathOnGround;
    }

    public boolean canPathWalls() {
        return canPathWalls;
    }

    public boolean canPathCeiling() {
        return canPathCeiling;
    }

    public boolean shouldCheckObstructions() {
        return checkObstructions;
    }

    @Override
    public void prepare(PathNavigationRegion region, Mob mob) {
        super.prepare(region, mob);
    }

    @Override
    public void done() {
        super.done();
    }

    public static long packDirection(Direction direction, long bits) {
        return bits | (1L << direction.ordinal());
    }

    public static boolean unpackDirection(Direction direction, long bits) {
        return (bits & (1L << direction.ordinal())) != 0L;
    }

    @Override
    protected BlockPathTypes evaluateBlockPathType(Mob mob, int x, int y, int z) {
        return super.evaluateBlockPathType(mob, x, y, z);
    }

    @Override
    public BlockPathTypes getBlockPathType(PathNavigationRegion region, int x, int y, int z, Mob mob) {
        return super.getBlockPathType(region, x, y, z, mob);
    }

    @Override
    protected Node getNode(int x, int y, int z) {
        return super.getNode(x, y, z);
    }
}
