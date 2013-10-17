package org.asoem.greyfish.impl.simulation;

import org.asoem.greyfish.core.simulation.SpatialSimulation2D;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.space.BasicTiled2DSpace;
import org.asoem.greyfish.utils.space.Point2D;

public interface Basic2DSimulation extends SpatialSimulation2D<Basic2DAgent, BasicTiled2DSpace> {

    void enqueueAddition(Basic2DAgent agent, Point2D point2D);
}
