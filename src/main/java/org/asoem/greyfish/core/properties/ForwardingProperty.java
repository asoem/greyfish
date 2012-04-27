package org.asoem.greyfish.core.properties;

import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;

public abstract class ForwardingProperty extends ForwardingObject implements GFProperty {

    @Override
    protected abstract GFProperty delegate();

    @Override
    public Agent getAgent() {
        return delegate().getAgent();
    }

    @Override
    public void setAgent(Agent agent) {
        delegate().setAgent(agent);
    }

    @Override
    public void initialize() {
        delegate().initialize();
    }

    @Override
    public void freeze() {
        delegate().freeze();
    }

    @Override
    public boolean isFrozen() {
        return delegate().isFrozen();
    }

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public void setName(String name) {
        delegate().setName(name);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        delegate().configure(e);
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Iterable<AgentComponent> children() {
        return delegate().children();
    }
}
