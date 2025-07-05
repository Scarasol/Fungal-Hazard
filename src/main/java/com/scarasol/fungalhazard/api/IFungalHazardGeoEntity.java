package com.scarasol.fungalhazard.api;

import software.bernie.geckolib.animatable.GeoEntity;

/**
 * @author Scarasol
 */
public interface IFungalHazardGeoEntity extends GeoEntity {
    String getModel();
    String getTexture();
    String getAnimation();
}