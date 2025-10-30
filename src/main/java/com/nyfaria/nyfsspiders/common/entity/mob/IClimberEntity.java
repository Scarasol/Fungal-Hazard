package com.nyfaria.nyfsspiders.common.entity.mob;

import com.nyfaria.nyfsspiders.common.entity.movement.IAdvancedPathFindingEntity;
import net.minecraft.core.Direction;

/**
 * Minimal contract required by the ported Nyfaria navigation helpers. The
 * actual spider implementation can provide the appropriate orientation and
 * ground attachment information while still delegating to the existing
 * Fungal Hazard behaviour.
 */
public interface IClimberEntity extends IAdvancedPathFindingEntity {

    /**
     * @return the current local orientation basis for the climber.
     */
    Orientation getOrientation();

    /**
     * @return the direction of the surface the entity treats as the ground.
     */
    Direction getGroundDirection();

    /**
     * @return the base movement speed used by helper controllers.
     */
    double getMovementSpeed();
}
