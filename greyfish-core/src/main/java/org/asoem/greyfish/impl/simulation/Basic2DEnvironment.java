package org.asoem.greyfish.impl.simulation;

import org.asoem.greyfish.core.simulation.SpatialEnvironment2D;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.space.BasicTiled2DSpace;
import org.asoem.greyfish.utils.space.Point2D;

public interface Basic2DEnvironment extends SpatialEnvironment2D<Basic2DAgent, BasicTiled2DSpace>, SynchronizedAgentsEnvironment<Basic2DAgent> {

    void enqueueAddition(Basic2DAgent agent, Point2D point2D);
}
