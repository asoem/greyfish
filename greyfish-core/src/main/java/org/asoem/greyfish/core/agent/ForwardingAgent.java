package org.asoem.greyfish.core.agent;

import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.utils.collect.FunctionalList;

import java.util.Set;

abstract class ForwardingAgent<A extends Agent<A, C>, C extends SimulationContext<?>>
        extends ForwardingObject
        implements Agent<A, C> {

    @Override
    protected abstract Agent<A, C> delegate();

    @Override
    public PrototypeGroup getPrototypeGroup() {
        return delegate().getPrototypeGroup();
    }

    @Override
    public boolean isMemberOf(final PrototypeGroup prototypeGroup) {
        return delegate().isMemberOf(prototypeGroup);
    }

    @Override
    public FunctionalList<AgentAction<A>> getActions() {
        return delegate().getActions();
    }

    @Override
    public FunctionalList<AgentProperty<A, ?>> getProperties() {
        return delegate().getProperties();
    }

    @Override
    public AgentProperty<A, ?> getProperty(final String name) {
        return delegate().getProperty(name);
    }

    @Override
    public Iterable<ACLMessage<A>> getMessages(final MessageTemplate template) {
        return delegate().getMessages(template);
    }

    @Override
    public boolean hasMessages(final MessageTemplate template) {
        return delegate().hasMessages(template);
    }

    @Override
    public void receiveAll(final Iterable<? extends ACLMessage<A>> messages) {
        delegate().receiveAll(messages);
    }

    @Override
    public void receive(final ACLMessage<A> messages) {
        delegate().receive(messages);
    }

    @Override
    public void run() {
        delegate().run();
    }

    @Override
    public void deactivate() {
        delegate().deactivate();
    }

    @Override
    public boolean isActive() {
        return delegate().isActive();
    }

    @Override
    public void activate(final C context) {
        delegate().activate(context);
    }

    @Override
    public AgentAction<A> getAction(final String actionName) {
        return delegate().getAction(actionName);
    }

    @Override
    public AgentTrait<A, ?> getTrait(final String geneName) {
        return delegate().getTrait(geneName);
    }

    @Override
    public FunctionalList<AgentTrait<A, ?>> getTraits() {
        return delegate().getTraits();
    }

    @Override
    public void initialize() {
        delegate().initialize();
    }

    @Override
    public Iterable<AgentNode> children() {
        return delegate().children();
    }

    @Override
    public AgentNode parent() {
        return delegate().parent();
    }

    @Override
    public Set<Integer> getParents() {
        return delegate().getParents();
    }

    @Override
    public void sendMessage(final ACLMessage<A> message) {
        delegate().sendMessage(message);
    }
}
