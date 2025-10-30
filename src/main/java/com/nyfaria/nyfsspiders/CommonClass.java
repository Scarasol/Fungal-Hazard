package com.nyfaria.nyfsspiders;

import net.minecraft.core.BlockPos;

/**
 * Utility helpers extracted from Nyfaria's spider implementation. Only the
 * handful of methods referenced by the imported navigation code are reproduced
 * here.
 */
public final class CommonClass {
    private CommonClass() {
    }

    public static BlockPos blockPos(double x, double y, double z) {
        return BlockPos.containing(x, y, z);
    }
}
