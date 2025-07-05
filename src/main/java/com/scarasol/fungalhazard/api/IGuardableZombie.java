package com.scarasol.fungalhazard.api;

/**
 * @author Scarasol
 */
public interface IGuardableZombie {
    boolean canGuard();
    long getLastGuardTime();
}
