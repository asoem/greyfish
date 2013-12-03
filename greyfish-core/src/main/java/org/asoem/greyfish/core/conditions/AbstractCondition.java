package org.asoem.greyfish.core.conditions;

import com.google.common.base.Optional;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.InheritableBuilder;

import javax.annotation.Nullable;

/**
 * A class that implements the <code>Condition</code> interface. Can be used to make a <code>AgentAction</code>
 * conditional.
 *
 * @author christoph
 */
public abstract class AbstractCondition<A extends Agent<A, ?>> implements ActionCondition<A> {

    @Nullable
    private ActionCondition<A> parentCondition;

    @Nullable
    private transient AgentAction<A> action;

    protected AbstractCondition() {
    }

    @SuppressWarnings("unchecked") // casting a clone should be safe
    protected AbstractCondition(final AbstractCondition<A> cloneable, final DeepCloner cloner) {
        cloner.addClone(cloneable, this);
        this.action = cloner.getClone(cloneable.action);
        this.parentCondition = cloner.getClone(cloneable.parentCondition);
    }

    protected AbstractCondition(final AbstractBuilder<A, ? extends AbstractCondition<A>, ?> builder) {
    }

    @Override
    public void setParent(@Nullable final ActionCondition<A> parent) {
        this.parentCondition = parent;
        setAction(parent != null ? parent.getAction().orNull() : null);
    }

    @Override
    public ActionCondition<A> getParent() {
        return parentCondition;
    }

    @Override
    public void setAction(@Nullable final AgentAction<A> action) {
        this.action = action;
        assert parentCondition == null || parentCondition.getAction().orNull() == action;
    }

    @Override
    public Optional<AgentAction<A>> getAction() {
        return Optional.fromNullable(action);
    }

    @Override
    public AgentAction<A> action() {
        return Optional.fromNullable(action).get();
    }

    @Override
    public final boolean isRootCondition() {
        return getParent() == null;
    }

    @Override
    public void setAgent(@Nullable final A agent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final ActionCondition<A> getRoot() {
        return (getParent() != null)
                ? getParent().getRoot()
                : this;
    }

    public Optional<A> agent() {
        final A nullableAgent = getAction().isPresent() ? getAction().get().agent().orNull() : null;
        return Optional.fromNullable(nullableAgent);
    }

    @Override
    public void setName(final String name) {
        throw new UnsupportedOperationException("Conditions don't use names");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Conditions don't use names");
    }

    @Override
    public void initialize() {
    }

    @Override
    public AgentNode parent() {
        return parentCondition != null ? parentCondition : action;
    }

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends AbstractCondition<A>, B extends AbstractBuilder<A, C, B>> extends InheritableBuilder<C, B> {
        public AbstractBuilder(final AbstractCondition<A> leafCondition) {
        }

        protected AbstractBuilder() {
        }
    }

    @Override
    public String toString() {
        return getParent() + "<-" + this.getClass().getSimpleName();
    }
}
