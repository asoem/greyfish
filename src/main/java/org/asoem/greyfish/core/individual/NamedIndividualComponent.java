package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.lang.HasName;
import org.asoem.greyfish.utils.Preparable;

public interface NamedIndividualComponent extends AgentComponent, Preparable<ParallelizedSimulation>, HasName {
}
