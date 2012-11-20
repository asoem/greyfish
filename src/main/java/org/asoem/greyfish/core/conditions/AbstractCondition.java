package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.InheritableBuilder;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.core.Commit;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkState;

/**
 * A class that implements the <code>Condition</code> interface.
 * Can be used to make a <code>AgentAction</code> conditional.
 * @author christoph
 */
public abstract class AbstractCondition<A extends Agent<S, A, P>, S extends Simulation<S, A, Z, P>, Z extends Space2D<A, P>, P extends Object2D> implements ActionCondition<A> {

    @Nullable
    private ActionCondition<A> parentCondition;
    @Nullable
    private transient AgentAction<A> action;

    protected AbstractCondition() {}

    protected AbstractCondition(AbstractCondition<A,S,Z,P> cloneable, DeepCloner cloner) {
        cloner.addClone(cloneable, this);
        this.action = cloner.getClone(cloneable.action, AgentAction.class);
        this.parentCondition = cloner.getClone(cloneable.parentCondition, ActionCondition.class);
    }

    protected AbstractCondition(AbstractBuilder<? extends AbstractCondition, ? extends AbstractBuilder> builder) {
    }

    @Override
    public void setParent(@Nullable ActionCondition<A> parent) {
        this.parentCondition = parent;
        setAction(parent != null ? parent.getAction() : null);
    }

    @Override
    public ActionCondition<A> getParent() {
        return parentCondition;
    }

    @Override
    public void setAction(@Nullable AgentAction<A> action) {
        this.action = action;
        assert parentCondition == null || parentCondition.getAction() == action;
    }

    @Override
    @Nullable
    public AgentAction<A> getAction() {
        return action;
    }

    @Override
    public final boolean isRootCondition() {
        return getParent() == null;
    }

    @Override
    public void setAgent(@Nullable Agent agent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final ActionCondition<A> getRoot() {
        return (getParent() != null)
                ? getParent().getRoot()
                : this;
    }

    @Commit
    private void commit() {
        if (!isLeafCondition()) {
            for (ActionCondition condition : getChildConditions()) {
                condition.setParent(this);
            }
        }
    }

    @Override
    public void configure(ConfigurationHandler e) {
    }

    @Override
    @Nullable
    public A getAgent() {
        final AgentAction<A> action = getAction();
        return action != null ? action.getAgent() : null;
    }

    public A agent() {
        A agent = getAgent();
        checkState(agent != null);
        return agent;
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Conditions don't use names");
    }

    @Override
    public void freeze() {
        throw new UnsupportedOperationException("Conditions don't have a frozen state of their own and use their action's state");
    }

    @Override
    public boolean isFrozen() {
        final AgentAction action = getAction();
        return action != null && action.isFrozen();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Conditions don't use names");
    }

    @Override
    public void initialize() {
    }

    public S simulation() {
        return agent().simulation();
    }

    @Override
    public AgentNode parent() {
        return parentCondition != null ? parentCondition : action;
    }

    protected static abstract class AbstractBuilder<C extends AbstractCondition, B extends AbstractBuilder<C, B>> extends InheritableBuilder<C, B> {
        public AbstractBuilder(AbstractCondition leafCondition) {
        }

        protected AbstractBuilder() {
        }
    }

    @Override
    public String toString() {
        return getParent() + "<-" + this.getClass().getSimpleName();
    }
}
