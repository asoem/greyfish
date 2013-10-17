package org.asoem.greyfish.impl.simulation;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.DefaultSimulationContextFactory;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.agent.SimulationContextFactory;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.io.SimulationLoggers;
import org.asoem.greyfish.core.simulation.AbstractSimulation;
import org.asoem.greyfish.core.utils.DiscreteTimeListener;
import org.asoem.greyfish.impl.agent.BasicAgent;
import org.asoem.greyfish.utils.concurrent.Runnables;

import javax.annotation.concurrent.GuardedBy;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.*;

/**
 * The default thread safe implementation of BasicSimulation.
 */
public final class DefaultBasicSimulation
        extends AbstractSimulation<BasicAgent>
        implements BasicSimulation {

    @GuardedBy("this")
    private final List<BasicAgent> agents = Lists.newArrayList();
    private final Queue<ModificationEvent> modificationEvents = Queues.newConcurrentLinkedQueue();
    private final transient Collection<BasicAgent> unmodifiableAgents = Collections.unmodifiableCollection(agents);
    private final SimulationLogger<? super BasicAgent> simulationLogger;
    private final ExecutorService executorService;
    private final String name;
    private final SimulationContextFactory<BasicSimulation, BasicAgent> contextFactory;
    private final AtomicLong steps = new AtomicLong(0);
    private final AtomicInteger agentIdSequence = new AtomicInteger();
    private final AtomicReference<Phase> phase = new AtomicReference<Phase>(Phase.IDLE);
    private final List<DiscreteTimeListener> timeListeners = Lists.newCopyOnWriteArrayList();

    public DefaultBasicSimulation(final String name, final ExecutorService executorService, final SimulationLogger<? super BasicAgent> logger, final DefaultSimulationContextFactory<BasicSimulation, BasicAgent> simulationContextFactory) {
        this.executorService = executorService;
        this.simulationLogger = logger;
        this.name = name;
        this.contextFactory = simulationContextFactory;
    }

    @Override
    protected SimulationLogger<? super BasicAgent> getSimulationLogger() {
        return simulationLogger;
    }

    @Override
    public void addTimeChangeListener(final DiscreteTimeListener timeListener) {
        timeListeners.add(timeListener);
    }

    @Override
    public long getTime() {
        return steps.get();
    }

    @Override
    public void nextStep() {
        synchronized (this) {
            checkState(this.phase.compareAndSet(Phase.IDLE, Phase.MODIFICATION));
            for (ModificationEvent modificationEvent : modificationEvents) {
                modificationEvent.apply();
            }
            modificationEvents.clear();

            checkState(this.phase.compareAndSet(Phase.MODIFICATION, Phase.EXECUTION));
            final List<Future<?>> agentExecutions = Lists.newArrayList();
            for (ListIterator<BasicAgent> iterator = agents.listIterator(); iterator.hasNext();) {
                final BasicAgent next = iterator.next();
                if (next.isActive()) {
                    final Future<?> future = executorService.submit(next);
                    agentExecutions.add(future);
                } else {
                    iterator.remove();
                }
            }

            for (Future<?> agentExecution : agentExecutions) {
                Futures.getUnchecked(agentExecution);
            }

            final long previousStep = steps.getAndIncrement();
            for (DiscreteTimeListener timeListener : timeListeners) {
                timeListener.timeChanged(this, previousStep, steps.get());
            }
            checkState(this.phase.compareAndSet(Phase.EXECUTION, Phase.IDLE));
        }
    }

    @Override
    public Iterable<BasicAgent> getAgents(final Population population) {
        return standardGetAgents(population);
    }

    @Override
    public Collection<BasicAgent> getAgents() {
        return unmodifiableAgents;
    }

    @Override
    public void enqueueRemoval(final BasicAgent agent) {
        enqueueRemoval(agent, Runnables.emptyRunnable(), MoreExecutors.sameThreadExecutor());
    }

    @Override
    public void enqueueRemoval(final BasicAgent agent, final Runnable listener, final Executor executor) {
        checkNotNull(agent);
        checkArgument(agent.isActive(), "Agent is not active");
        checkArgument(this.equals(agent.simulation()), "Agent not is active in another simulation");
        checkState(!Phase.MODIFICATION.equals(phase.get()));
        this.modificationEvents.add(new ModificationEvent() {
            @Override
            public void apply() {
                agent.deactivate();
                executor.execute(listener); // TODO: Agent is not removed yet, only marked for removal
            }
        });
    }

    @Override
    public void enqueueAddition(final BasicAgent agent) {
        checkNotNull(agent);
        checkArgument(!agent.isActive(), "Agent is active");
        checkState(!Phase.MODIFICATION.equals(phase.get()));
        this.modificationEvents.add(new ModificationEvent() {
            @Override
            public void apply() {
                agent.activate(contextFactory.createActiveContext(
                        DefaultBasicSimulation.this, agentIdSequence.incrementAndGet(), getTime()));
                agents.add(agent);
            }
        });
    }

    @Override
    public int numberOfPopulations() {
        return standardNumberOfPopulations();
    }

    @Override
    public int countAgents() {
        return standardCountAgents();
    }

    @Override
    public int countAgents(final Population population) {
        return standardCountAgents(population);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void deliverMessage(final ACLMessage<BasicAgent> message) {
        checkNotNull(message);
        checkState(!Phase.MODIFICATION.equals(phase.get()));
        modificationEvents.add(new ModificationEvent() {
            @Override
            public void apply() {
                final Set<BasicAgent> recipients = message.getRecipients();
                for (BasicAgent recipient : recipients) {
                    recipient.receive(message);
                }
            }
        });
    }

    private interface ModificationEvent {
        void apply();
    }

    /**
     * Create a new {@code Builder} for a {@code DefaultBasicSimulation} with name equal to given {@code name}.
     * @param name the name of the simulation
     * @return a new {@code Builder}
     */
    public static Builder builder(final String name) {
        checkNotNull(name);
        return new Builder(name);
    }

    /**
     * A builder for {@code DefaultBasicSimulation}.
     */
    public static class Builder {
        private final String name;
        private ExecutorService executorService = Executors.newCachedThreadPool();
        private SimulationLogger<? super BasicAgent> logger = SimulationLoggers.nullLogger();
        private DefaultSimulationContextFactory<BasicSimulation, BasicAgent> simulationContextFactory =
                DefaultSimulationContextFactory.<BasicSimulation, BasicAgent>create();

        private Builder(final String name) {
            this.name = name;
        }

        public Builder executorService(final ExecutorService executorService) {
            this.executorService = checkNotNull(executorService);
            return this;
        }

        public Builder logger(final SimulationLogger<? super BasicAgent> logger) {
            this.logger = checkNotNull(logger);
            return this;
        }

        public DefaultBasicSimulation build() {
            return new DefaultBasicSimulation(name, executorService, logger, simulationContextFactory);
        }
    }

    private enum Phase {
        IDLE,
        MODIFICATION,
        EXECUTION
    }
}
