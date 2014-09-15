package org.asoem.greyfish.core.simulation;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.impl.simulation.SynchronizedAgentsSimulation;

public abstract class AbstractSimulation<A extends Agent<?>> implements SynchronizedAgentsSimulation<A> {


    @Override
    public final String toString() {
        return "Simulation['" + getName() + "']";
    }

    @Override
    public final Iterable<A> filterAgents(final Predicate<? super A> predicate) {
        return Iterables.filter(getActiveAgents(), predicate);
    }

    protected int standardCountAgents() {
        return Iterables.size(getActiveAgents());
    }

}
