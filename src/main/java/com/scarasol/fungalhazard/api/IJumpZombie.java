package com.scarasol.fungalhazard.api;

/**
 * @author Scarasol
 */
public interface IJumpZombie {
    boolean canJump();
    void setLastJumpTime(long lastJumpTime);
}
