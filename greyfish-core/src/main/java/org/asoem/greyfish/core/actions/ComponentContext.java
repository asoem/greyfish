package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.SimulationContext;

public interface ComponentContext<A extends Agent<A, C>, C extends SimulationContext<?>> {
    A agent();

    C simulationContext();
}
