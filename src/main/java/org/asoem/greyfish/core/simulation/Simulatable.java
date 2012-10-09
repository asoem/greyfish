package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.agent.ActiveSimulationContext;
import org.asoem.greyfish.core.agent.PassiveSimulationContext;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 12:54
 */
public interface Simulatable {
    void activate(ActiveSimulationContext context);
    void execute();
    void shutDown(PassiveSimulationContext context);
}
