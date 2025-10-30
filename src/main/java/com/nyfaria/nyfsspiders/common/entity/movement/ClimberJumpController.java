package com.nyfaria.nyfsspiders.common.entity.movement;

import com.nyfaria.nyfsspiders.common.entity.mob.IClimberEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.phys.Vec3;

/**
 * Basic jump controller that allows the Nyfaria movement helpers to request a
 * jump in a specific direction. The vanilla controller only toggles a boolean
 * each tick, so we extend it with a small velocity nudge while still delegating
 * to the base implementation for consistency with other behaviours.
 */
public class ClimberJumpController<T extends Mob & IClimberEntity> extends JumpControl {
    private final T mob;
    private Vec3 pendingJump = Vec3.ZERO;

    public ClimberJumpController(T mob) {
        super(mob);
        this.mob = mob;
    }

    public void setJumping(Vec3 direction) {
        if (direction.lengthSqr() > 1.0E-4D) {
            this.pendingJump = direction.normalize();
            this.jump();
        }
    }

    @Override
    public void tick() {
        if (this.pendingJump.lengthSqr() > 0.0D) {
            Vec3 velocity = this.mob.getDeltaMovement();
            this.mob.setDeltaMovement(velocity.add(this.pendingJump));
            this.pendingJump = Vec3.ZERO;
        }
        super.tick();
    }
}
