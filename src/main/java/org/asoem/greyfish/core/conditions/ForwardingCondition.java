package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * User: christoph
 * Date: 13.11.11
 * Time: 19:17
 */
public abstract class ForwardingCondition implements GFCondition {
    
    protected abstract GFCondition delegate();

    @Override
    public boolean isLeafCondition() {
        return delegate().isLeafCondition();
    }

    @Override
    public boolean isRootCondition() {
        return delegate().isRootCondition();
    }

    @Override
    public List<GFCondition> getChildConditions() {
        return delegate().getChildConditions();
    }

    @Override
    @Nullable
    public GFCondition getParentCondition() {
        return delegate().getParentCondition();
    }

    @Override
    public GFCondition getRoot() {
        return delegate().getRoot();
    }

    @Override
    public void setParent(@Nullable GFCondition parent) {
        delegate().setParent(parent);
    }

    @Override
    public void add(GFCondition condition) {
        delegate().add(condition);
    }

    @Override
    public void remove(GFCondition condition) {
        delegate().remove(condition);
    }

    @Override
    @Nullable
    public Agent getAgent() {
        return delegate().getAgent();
    }

    @Override
    public void setAgent(@Nullable Agent agent) {
        delegate().setAgent(agent);
    }

    @Override
    public void setName(String name) {
        delegate().setName(name);
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        delegate().accept(visitor);
    }

    @Override
    public void prepare(Simulation context) {
        delegate().prepare(context);
    }

    @Override
    public Iterable<AgentComponent> children() {
        return delegate().children();
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
    public boolean hasName(String s) {
        return delegate().hasName(s);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return delegate().deepClone(cloner);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        delegate().configure(e);
    }

    @Override
    public boolean apply(@Nullable Simulation simulation) {
        return delegate().apply(simulation);
    }
}
