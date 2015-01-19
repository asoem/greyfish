package org.asoem.greyfish.impl.environment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.agent.ContextFactory;
import org.asoem.greyfish.core.agent.DefaultContextFactory;
import org.asoem.greyfish.core.environment.AbstractEnvironment;
import org.asoem.greyfish.core.scheduler.Event;
import org.asoem.greyfish.impl.agent.BasicAgent;
import org.asoem.greyfish.utils.concurrent.Runnables;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
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
@ThreadSafe
public final class DefaultBasicEnvironment
        extends AbstractEnvironment<BasicAgent>
        implements BasicEnvironment {

    @GuardedBy("agents")
    private final List<BasicAgent> agents = Lists.newLinkedList();
    private final Queue<DelayedModification> delayedModifications = Queues.newConcurrentLinkedQueue();
    private final ListeningExecutorService executorService;
    private final String name;
    private final ContextFactory<BasicEnvironment, BasicAgent> contextFactory;
    private final AtomicLong steps = new AtomicLong(0);
    private final AtomicInteger agentIdSequence = new AtomicInteger();
    private final AtomicReference<Phase> phase = new AtomicReference<Phase>(Phase.IDLE);
    private final EventBus eventBus;

    public DefaultBasicEnvironment(final Builder builder) {
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
            checkState(this.phase.compareAndSet(Phase.IDLE, Phase.UPDATE));
            applyModifications();

            checkState(this.phase.compareAndSet(Phase.UPDATE, Phase.EXECUTION));
            executeAgents();

            incrementTime(); // TODO: should be moved to the beginning of the loop
            checkState(this.phase.compareAndSet(Phase.EXECUTION, Phase.IDLE));
        }
    }

    private void incrementTime() {
        final long previousStep = steps.getAndIncrement();
        final long currentStep = steps.get();
        eventBus.post(new TimeChangedEvent(this, previousStep, currentStep));
    }

    private void applyModifications() {
        for (DelayedModification delayedModification : delayedModifications) {
            delayedModification.apply();
        }
        delayedModifications.clear();
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
        synchronized (agents) {
            for (Iterator<BasicAgent> iterator = agents.iterator(); iterator.hasNext(); ) {
                BasicAgent agent = iterator.next();
                if (!agent.isActive()) {
                    iterator.remove();
                    eventBus.post(new AgentRemovedEvent(agent, this));
                }
            }
        }
    }

    @Override
    public Iterable<BasicAgent> getActiveAgents() {
        synchronized (agents) {
            return ImmutableList.copyOf(agents);
        }
    }

    @Override
    public void enqueueRemoval(final BasicAgent agent) {
        enqueueRemoval(agent, Runnables.emptyRunnable(), MoreExecutors.sameThreadExecutor());
    }

    @Override
    public void enqueueRemoval(final BasicAgent agent, final Runnable listener, final Executor executor) {
        checkNotNull(agent);
        checkArgument(agent.isActive(), "Agent is not active");
        checkArgument(this.equals(agent.getContext().get().getEnvironment()), "Agent is active in another simulation");
        checkState(!Phase.UPDATE.equals(phase.get()));
        this.delayedModifications.add(new DelayedModification() {
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
        // TODO: inactive agents can be enqueued multiple times. Should this be prevented? Here?
        checkState(!Phase.UPDATE.equals(phase.get()));
        this.delayedModifications.add(new AgentActivation(agent));
    }

    private void activateAgent(final BasicAgent agent) {
        assert agent != null;
        agent.activate(contextFactory.createActiveContext(
                this, agentIdSequence.incrementAndGet(), getTime()));
        synchronized (agents) {
            agents.add(agent);
        }
        eventBus.post(new AgentAddedEvent(agent, this));
    }

    @Override
    public int countAgents() {
        return standardCountAgents();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void deliverMessage(final ACLMessage<BasicAgent> message) {
        checkNotNull(message);
        checkState(!Phase.UPDATE.equals(phase.get()));
        delayedModifications.add(new DelayedModification() {
            @Override
            public void apply() {
                final Set<BasicAgent> recipients = message.getRecipients();
                for (BasicAgent recipient : recipients) {
                    recipient.ask(message, Void.class);
                }
            }
        });
    }

    @Override
    public String getStatusInfo() {
        return String.format("%d agents; %d steps", countAgents(), getTime());
    }

    @Override
    public void schedule(final Event e) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private interface DelayedModification {
        void apply();
    }

    /**
     * Create a new {@code Builder} for a {@code DefaultBasicSimulation} with name equal to given {@code name}.
     *
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
        private ExecutorService executorService =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        private DefaultContextFactory<BasicEnvironment, BasicAgent> simulationContextFactory =
                DefaultContextFactory.<BasicEnvironment, BasicAgent>create();
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

        public DefaultBasicEnvironment build() {
            return new DefaultBasicEnvironment(this);
        }
    }

    private enum Phase {
        /**
         * In this phase the simulation is idle and waits for the next call to {@link #nextStep()}
         */
        IDLE,
        /**
         * In this phase the state of the simulation gets updated internally
         */
        UPDATE,
        /**
         * In this state agents interact with the simulation and enqueue modifications
         */
        EXECUTION
    }

    private class AgentActivation implements DelayedModification {
        private final BasicAgent agent;

        public AgentActivation(final BasicAgent agent) {
            this.agent = agent;
        }

        @Override
        public void apply() {
            activateAgent(agent);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final AgentActivation that = (AgentActivation) o;

            if (!agent.equals(that.agent)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return agent.hashCode();
        }
    }
}
