package org.asoem.greyfish.impl.agent;

import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.impl.simulation.Basic2DSimulation;
import org.asoem.greyfish.utils.space.Point2D;

/**
 * A basic configuration of a spatial agent with an {@link Point2D} projection into space to get simulated in an {@link
 * Basic2DSimulation}.
 */
public interface Basic2DAgent extends SpatialAgent<Basic2DAgent, Point2D, BasicSimulationContext<Basic2DSimulation, Basic2DAgent>> {
}
