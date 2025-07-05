package com.scarasol.fungalhazard.api;

/**
 * @author Scarasol
 */
public interface IDodgeableZombie {
    boolean canDodge();
    int getDodgeCount();
    boolean dodge();
    long getLastDodgeTime();
}
