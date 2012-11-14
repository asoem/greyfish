package org.asoem.greyfish.core.actions.utils;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.SpatialObject;

/**
 * User: christoph
 * Date: 14.02.11
 * Time: 09:59
 */
public interface MovementPattern {

    Motion2D createMotion(Agent agent, Simulation<SpatialObject> simulation);
}
