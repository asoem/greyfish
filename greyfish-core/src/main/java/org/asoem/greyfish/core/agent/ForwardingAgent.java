package org.asoem.greyfish.core.agent;

import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.actions.AgentContext;

abstract class ForwardingAgent<C extends Context<?, ?>, AC extends AgentContext<?>>
        extends ForwardingObject
        implements Agent<C> {

    @Override
    protected abstract Agent<?> delegate();

    @Override
    public PrototypeGroup getPrototypeGroup() {
        return delegate().getPrototypeGroup();
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
    public void initialize() {
        delegate().initialize();
    }

    @Override
    public Iterable<AgentNode> children() {
        return delegate().children();
    }

}
