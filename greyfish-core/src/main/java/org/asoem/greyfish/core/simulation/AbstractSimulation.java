package org.asoem.greyfish.core.simulation;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.PrototypeGroup;
import org.asoem.greyfish.impl.agent.BasicAgent;
import org.asoem.greyfish.impl.simulation.SynchronizedAgentsSimulation;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractSimulation<A extends Agent<A, ?>> implements SynchronizedAgentsSimulation<A> {


    /**
     * A standard implementation for {@link #numberOfPopulations()} you can use to implement this method.
     *
     * @return the number of distinct populations of all agents
     */
    protected final int standardNumberOfPopulations() {
        return Sets.newHashSet(Iterables.transform(getActiveAgents(), new Function<A, PrototypeGroup>() {
            @Override
            public PrototypeGroup apply(final A input) {
                return input.getPrototypeGroup();
            }
        })).size();
    }

    /**
     * A standard implementation for {@link #filterAgents(com.google.common.base.Predicate)} you can use to implement
     * this method.
     *
     * @param prototypeGroup the population to filter agents for
     * @return a view of {@link #getActiveAgents()} where all agents have {@link org.asoem.greyfish.core.agent.Agent#getPrototypeGroup()}
     * equal to {@code population}
     */
    protected final Iterable<A> standardGetAgents(final PrototypeGroup prototypeGroup) {
        checkNotNull(prototypeGroup);

        return Iterables.filter(getActiveAgents(), new Predicate<A>() {
            @Override
            public boolean apply(final A agent) {
                return prototypeGroup.equals(agent.getPrototypeGroup());
            }
        });
    }

    @Override
    public final String toString() {
        return "Simulation['" + getName() + "']";
    }

    @Override
    public void shutdown() {
    }

    @Override
    public final Iterable<A> filterAgents(final Predicate<? super A> predicate) {
        return Iterables.filter(getActiveAgents(), predicate);
    }

    protected int standardCountAgents() {
        return Iterables.size(getActiveAgents());
    }

    protected int standardCountAgents(final PrototypeGroup prototypeGroup) {
        return Iterables.frequency(getActiveAgents(), new Predicate<BasicAgent>() {
            @Override
            public boolean apply(final BasicAgent input) {
                return prototypeGroup.equals(input.getPrototypeGroup());
            }
        });
    }
}
