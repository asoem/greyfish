package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.core.conditions.ActionCondition;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.actions.utils.ActionState.*;

/**
 * An abstract base implementation of a agent action which delegates the precondition check to an {@link
 * org.asoem.greyfish.core.conditions.ActionCondition ActionCondition}.
 *
 * @param <A> the type of the agent
 */
public abstract class BaseAgentAction<A extends Agent<A, ? extends BasicSimulationContext<?, A>>>
        extends AbstractAgentComponent<A> implements AgentAction<A> {

    @Nullable
    private ActionCondition<A> condition;
    private ActionState actionState;

    protected BaseAgentAction(final AbstractBuilder<A, ? extends BaseAgentAction<A>,
            ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
        this.setActionState(builder.actionState);

        setCondition(builder.condition);
    }

    public final boolean evaluateCondition() {
        return condition == null || condition.evaluate();
    }

    /**
     * Called by the {@code Agent} which contains this {@code AgentAction}
     *
     * @param componentContext the context for this action
     */
    @Override
    public final ActionExecutionResult apply(final ComponentContext<A, ?> componentContext) {
        checkNotNull(componentContext);
        ActionExecutionResult result = null;

        while (result == null) {
            final ActionState currentState = getActionState();
            switch (currentState) {
                case INITIAL:
                    final boolean preconditionsMet = evaluateCondition();
                    if (preconditionsMet) {
                        setState(PRECONDITIONS_MET);
                    } else {
                        setState(PRECONDITIONS_FAILED);
                    }
                    break;
                case PRECONDITIONS_MET:
                case INTERMEDIATE:
                    final ActionState state = proceed();
                    setState(state);
                    if (state == INTERMEDIATE) {
                        result = ActionExecutionResult.CONTINUE;
                    }
                    break;
                case PRECONDITIONS_FAILED:
                case ABORTED:
                    result = ActionExecutionResult.NEXT;
                    reset();
                    break;
                case COMPLETED:
                    result = ActionExecutionResult.BREAK;
                    reset();
                    break;
                default:
                    throw new AssertionError("Unexpected state: " + currentState);
            }
        }

        return result;
    }

    protected abstract ActionState proceed();

    private void setState(final ActionState state) {
        assert state != null;
        setActionState(state);
    }

    private void reset() {
        setState(INITIAL);
    }

    @Override
    public void initialize() {
        super.initialize();
        reset();
        if (condition != null) {
            condition.initialize();
        }
    }

    @Nullable
    public final ActionCondition<A> getCondition() {
        return condition;
    }

    private void setCondition(@Nullable final ActionCondition<A> condition) {
        this.condition = condition;
        if (condition != null) {
            condition.setAction(this);
        }
    }

    @Override
    public final Iterable<AgentNode> children() {
        return condition != null ? Collections.<AgentNode>singletonList(getCondition()) : Collections.<AgentNode>emptyList();
    }

    private ActionState getActionState() {
        return actionState;
    }

    private void setActionState(final ActionState actionState) {
        this.actionState = actionState;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected abstract static class AbstractBuilder<A extends Agent<A, ? extends BasicSimulationContext<?, A>>,
            T extends BaseAgentAction<A>,
            B extends AbstractBuilder<A, T, B>> extends AbstractAgentComponent.AbstractBuilder<A, T, B>
            implements Serializable {
        private ActionCondition<A> condition;
        private int successCount;
        private ActionState actionState = INITIAL;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(final BaseAgentAction<A> action) {
            super(action);
            this.condition = action.condition;
            this.actionState = action.getActionState();
        }

        public final B executedIf(final ActionCondition<A> condition) {
            this.condition = condition;
            return self();
        }

    }
}
