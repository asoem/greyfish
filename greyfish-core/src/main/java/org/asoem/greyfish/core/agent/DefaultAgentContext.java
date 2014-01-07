package org.asoem.greyfish.core.agent;

import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentContext;

public final class DefaultAgentContext<A extends Agent<?>> implements AgentContext<A> {
    private A agent;
    private final BasicSimulationContext<?, A> simulationContext;

    public DefaultAgentContext(final A agent, final BasicSimulationContext<?, A> simulationContext) {
        this.agent = agent;
        this.simulationContext = simulationContext;
    }

    @Override
    public A agent() {
        return agent;
    }

    @Override
    public Iterable<A> getActiveAgents() {
        return ImmutableList.copyOf(simulationContext.getActiveAgents());
    }

    @Override
    public Iterable<A> getAgents(final PrototypeGroup prototypeGroup) {
        return ImmutableList.copyOf(simulationContext.getAgents(prototypeGroup));
    }

    @Override
    public void receive(final ACLMessage<A> message) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Iterable<ACLMessage<A>> getMessages(final MessageTemplate template) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void sendMessage(final ACLMessage<A> message) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
