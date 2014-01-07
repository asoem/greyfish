package org.asoem.greyfish.core.conditions;

import com.google.common.base.Optional;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.InheritableBuilder;

import javax.annotation.Nullable;

/**
 * A class that implements the <code>Condition</code> interface. Can be used to make a <code>AgentAction</code>
 * conditional.
 *
 * @author christoph
 */
public abstract class AbstractCondition<A extends Agent<?>> implements ActionCondition<A> {

    @Nullable
    private ActionCondition<A> parentCondition;

    @Nullable
    private transient AgentAction<?> action;
    private Optional<A> agent = Optional.absent();

    protected AbstractCondition() {
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
    public void setAction(@Nullable final AgentAction<?> action) {
        this.action = action;
        assert parentCondition == null || parentCondition.getAction().orNull() == action;
    }

    @Override
    public Optional<AgentAction<?>> getAction() {
        return Optional.<AgentAction<?>>fromNullable(action);
    }

    @Override
    public AgentAction<?> action() {
        return Optional.fromNullable(action).get();
    }

    @Override
    public final boolean isRootCondition() {
        return getParent() == null;
    }

    @Override
    public void setAgent(@Nullable final A agent) {
        this.agent = Optional.fromNullable(agent);
    }

    @Override
    public final ActionCondition<A> getRoot() {
        return (getParent() != null)
                ? getParent().getRoot()
                : this;
    }

    public Optional<A> agent() {
        return agent;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Conditions don't use names");
    }

    @Override
    public void initialize() {
    }

    protected static abstract class AbstractBuilder<A extends Agent<?>, C extends AbstractCondition<A>, B extends AbstractBuilder<A, C, B>> extends InheritableBuilder<C, B> {
        public AbstractBuilder(final AbstractCondition<A> leafCondition) {
        }

        protected AbstractBuilder() {
        }
    }

    @Override
    public String toString() {
        return getParent() + "<-" + this.getClass().getSimpleName();
    }

    @Override
    public <T> T tell(final A context, final Object message, final Class<T> replyType) {
        throw new UnsupportedOperationException();
    }
}
