package org.asoem.greyfish.core.simulation;

/**
 * User: christoph
 * Date: 08.10.12
 * Time: 14:56
 */
public interface Model<S extends SpatialSimulation<?, ?>> {
    S createSimulation();
}
