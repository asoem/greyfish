package org.asoem.greyfish.core.conditions;

import com.google.common.base.Optional;
import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.core.agent.SimulationContext;

import javax.annotation.Nullable;
import java.util.List;

/**
 * User: christoph Date: 06.12.12 Time: 10:29
 */
public abstract class ForwardingCondition<A extends Agent<A, SimulationContext<?>>> extends ForwardingObject implements ActionCondition<A> {

    protected abstract ActionCondition<A> delegate();

    @Override
    public Optional<AgentAction<A>> getAction() {
        return delegate().getAction();
    }

    @Override
    public AgentAction<A> action() {
        return delegate().action();
    }

    public void setAction(final AgentAction<A> action) {
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

    public void setParent(@Nullable final ActionCondition<A> parent) {
        delegate().setParent(parent);
    }

    @Override
    public ActionCondition<A> getParent() {
        return delegate().getParent();
    }

    public void insert(final ActionCondition<A> condition, final int index) {
        delegate().insert(condition, index);
    }

    public void add(final ActionCondition<A> condition) {
        delegate().add(condition);
    }

    public void remove(final ActionCondition<A> condition) {
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
    public Optional<A> agent() throws IllegalStateException {
        return delegate().agent();
    }

    public void setAgent(@Nullable final A agent) {
        delegate().setAgent(agent);
    }

    @Override
    public void setName(final String name) {
        delegate().setName(name);
    }

    @Override
    public String getName() {
        return delegate().getName();
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
    public Iterable<AgentNode> children() {
        return delegate().children();
    }

}
