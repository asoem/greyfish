package org.asoem.greyfish.impl.simulation;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.DefaultSimulationContextFactory;
import org.asoem.greyfish.core.agent.PrototypeGroup;
import org.asoem.greyfish.core.agent.SimulationContextFactory;
import org.asoem.greyfish.core.simulation.AbstractSimulation;
import org.asoem.greyfish.impl.agent.BasicAgent;
import org.asoem.greyfish.utils.concurrent.Runnables;

import javax.annotation.concurrent.GuardedBy;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final ListeningExecutorService executorService;
    private final String name;
    private final SimulationContextFactory<BasicSimulation, BasicAgent> contextFactory;
    private final AtomicLong steps = new AtomicLong(0);
    private final AtomicInteger agentIdSequence = new AtomicInteger();
    private final AtomicReference<Phase> phase = new AtomicReference<Phase>(Phase.IDLE);
    private final EventBus eventBus;

    public DefaultBasicSimulation(final Builder builder) {
        this.executorService = MoreExecutors.listeningDecorator(builder.executorService);
        this.name = builder.name;
        this.contextFactory = builder.simulationContextFactory;
        this.eventBus = builder.eventPublisher;
    }

    @Override
    public long getTime() {
        return steps.get();
    }

    @Override
    public void nextStep() {
        synchronized (this) {
            checkState(this.phase.compareAndSet(Phase.IDLE, Phase.MODIFICATION));
            applyModifications();

            checkState(this.phase.compareAndSet(Phase.MODIFICATION, Phase.EXECUTION));
            executeAgents();

            incrementTime(); // TODO: verify that this is the correct place to increment
            checkState(this.phase.compareAndSet(Phase.EXECUTION, Phase.IDLE));
        }
    }

    private void incrementTime() {
        final long previousStep = steps.getAndIncrement();
        final long currentStep = steps.get();
        eventBus.post(new TimeChangedEvent(this, previousStep, currentStep));
    }

    private void applyModifications() {
        for (ModificationEvent modificationEvent : modificationEvents) {
            modificationEvent.apply();
        }
        modificationEvents.clear();
        removeInactiveAgents();
    }

    private void executeAgents() {
        final List<ListenableFuture<?>> agentExecutions = Lists.newArrayList();
        for (BasicAgent agent : agents) {
            agentExecutions.add(executorService.submit(agent));
        }
        Futures.getUnchecked(Futures.allAsList(agentExecutions));
    }

    private void removeInactiveAgents() {
        for (Iterator<BasicAgent> iterator = agents.iterator(); iterator.hasNext(); ) {
            BasicAgent agent = iterator.next();
            if (!agent.isActive()) {
                iterator.remove();
                eventBus.post(new AgentRemovedEvent(agent, this));
            }
        }
    }

    @Override
    public Iterable<BasicAgent> getAgents(final PrototypeGroup prototypeGroup) {
        return standardGetAgents(prototypeGroup);
    }

    @Override
    public Iterable<BasicAgent> getActiveAgents() {
        return Iterables.unmodifiableIterable(agents);
    }

    @Override
    public void enqueueRemoval(final BasicAgent agent) {
        enqueueRemoval(agent, Runnables.emptyRunnable(), MoreExecutors.sameThreadExecutor());
    }

    @Override
    public void enqueueRemoval(final BasicAgent agent, final Runnable listener, final Executor executor) {
        checkNotNull(agent);
        checkArgument(agent.isActive(), "Agent is not active");
        checkArgument(this.equals(agent.simulation()), "Agent is active in another getSimulation");
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
                activateAgent(agent);
            }
        });
    }

    private void activateAgent(final BasicAgent agent) {
        agent.activate(contextFactory.createActiveContext(
                this, agentIdSequence.incrementAndGet(), getTime()));
        agents.add(agent);
        eventBus.post(new AgentAddedEvent(agent, this));
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
    public int countAgents(final PrototypeGroup prototypeGroup) {
        return standardCountAgents(prototypeGroup);
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

    @Override
    public String getStatusInfo() {
        return String.format("%d agents; %d steps", countAgents(), getTime());
    }

    private interface ModificationEvent {
        void apply();
    }

    /**
     * Create a new {@code Builder} for a {@code DefaultBasicSimulation} with name equal to given {@code name}.
     * @param name the name of the getSimulation
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
        private DefaultSimulationContextFactory<BasicSimulation, BasicAgent> simulationContextFactory =
                DefaultSimulationContextFactory.<BasicSimulation, BasicAgent>create();
        private EventBus eventPublisher = new EventBus();

        private Builder(final String name) {
            this.name = name;
        }

        public Builder executorService(final ExecutorService executorService) {
            this.executorService = checkNotNull(executorService);
            return this;
        }

        public Builder eventBus(final EventBus eventBus) {
            this.eventPublisher = checkNotNull(eventBus);
            return this;
        }

        public DefaultBasicSimulation build() {
            return new DefaultBasicSimulation(this);
        }
    }

    private enum Phase {
        IDLE,
        MODIFICATION,
        EXECUTION
    }
}
