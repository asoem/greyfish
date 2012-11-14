package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.ActiveSimulationContext;
import org.asoem.greyfish.core.agent.PassiveSimulationContext;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 12:54
 */
public interface Simulatable<P extends Object2D> {
    Simulation<P> simulation();
    void activate(ActiveSimulationContext context);
    void execute();
    void shutDown(PassiveSimulationContext context);
}
