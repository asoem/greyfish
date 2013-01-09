package org.asoem.greyfish.core.conditions;

import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.utils.base.CloneMap;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * User: christoph
 * Date: 06.12.12
 * Time: 10:29
 */
public abstract class ForwardingCondition<A extends Agent<A, ?>> extends ForwardingObject implements ActionCondition<A> {

    protected abstract ActionCondition<A> delegate();

    @Override
    public AgentAction<A> getAction() {
        return delegate().getAction();
    }

    @Override
    public AgentAction<A> action() {
        return delegate().action();
    }

    public void setAction(AgentAction<A> action) {
        delegate().setAction(action);
    }

    @Override
    public List<ActionCondition<A>> getChildConditions() {
        return delegate().getChildConditions();
    }

    @Override
    public ActionCondition<A> getRoot() {
        return delegate().getRoot();
    }

    public void setParent(@Nullable ActionCondition<A> parent) {
        delegate().setParent(parent);
    }

    @Override
    public ActionCondition<A> getParent() {
        return delegate().getParent();
    }

    public void insert(ActionCondition<A> condition, int index) {
        delegate().insert(condition, index);
    }

    public void add(ActionCondition<A> condition) {
        delegate().add(condition);
    }

    public void remove(ActionCondition<A> condition) {
        delegate().remove(condition);
    }

    @Override
    public void removeAll() {
        delegate().removeAll();
    }

    @Override
    public boolean isLeafCondition() {
        return delegate().isLeafCondition();
    }

    @Override
    public boolean isRootCondition() {
        return delegate().isRootCondition();
    }

    @Override
    public boolean evaluate() {
        return delegate().evaluate();
    }

    @Override
    @Nullable
    public A getAgent() {
        return delegate().getAgent();
    }

    @Override
    public A agent() throws IllegalStateException {
        return delegate().agent();
    }

    public void setAgent(@Nullable A agent) {
        delegate().setAgent(agent);
    }

    @Override
    public void setName(String name) {
        delegate().setName(name);
    }

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public void configure(ConfigurationHandler e) {
        delegate().configure(e);
    }

    @Override
    public void initialize() {
        delegate().initialize();
    }

    @Override
    @Nullable
    public AgentNode parent() {
        return delegate().parent();
    }

    @Override
    public Iterable<AgentNode> childConditions() {
        return delegate().childConditions();
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
    public DeepCloneable deepClone(CloneMap cloneMap) {
        return delegate().deepClone(cloneMap);
    }
}
