package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.core.conditions.ActionCondition;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloner;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.core.actions.utils.ActionState.*;

public abstract class AbstractAgentAction<A extends Agent<A, ?>>
        extends AbstractAgentComponent<A> implements AgentAction<A> {

    @Nullable
    private ActionCondition<A> condition;
    private Callback<? super AbstractAgentAction<A>, Void> onSuccess;
    private int successCount;
    private long stepAtLastSuccess;
    private ActionState actionState;

    @SuppressWarnings("unchecked")
    protected AbstractAgentAction(final AbstractAgentAction<A> cloneable, final DeepCloner cloner) {
        super(cloneable, cloner);
        this.condition = cloner.getClone(cloneable.condition);
        this.onSuccess = cloneable.onSuccess;
        this.stepAtLastSuccess = cloneable.stepAtLastSuccess;
        this.successCount = cloneable.successCount;
        this.actionState = cloneable.actionState;
    }

    protected AbstractAgentAction(final AbstractBuilder<A, ? extends AbstractAgentAction<A>,
            ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
        this.onSuccess = builder.onSuccess;
        this.successCount = builder.successCount;
        this.stepAtLastSuccess = builder.stepAtLastSuccess;
        this.actionState = builder.actionState;

        setCondition(builder.condition);
    }

    @Override
    public final boolean evaluateCondition() {
        return condition == null || condition.evaluate();
    }

    /**
     * Called by the {@code Agent} which contains this {@code AgentAction}
     */
    @Override
    public final ActionState apply() {
        final A agent = agent().get();
        assert stepAtLastSuccess < agent.getContext().get().getTime()
                : "actions must not get executed twice per step: "
                + stepAtLastSuccess + " >= " + agent.getContext().get().getTime();

        if (INITIAL == actionState) {
            checkPreconditions();
        }

        if (PRECONDITIONS_MET == actionState
                || INTERMEDIATE == actionState) {

            final ActionState state = proceed();

            switch (state) {

                case COMPLETED:
                    ++successCount;
                    stepAtLastSuccess = agent.getContext().get().getTime();
                    Callbacks.call(onSuccess, this);
                    break;

                default:
                    break;
            }

            setState(state);
        }

        return actionState;
    }

    protected abstract ActionState proceed();

    private void setState(final ActionState state) {
        assert state != null;
        actionState = state;
    }

    @Override
    public final void reset() {
        setState(INITIAL);
    }

    @Override
    public final ActionState checkPreconditions() {
        checkState(actionState == INITIAL, "Action not is state %s", INITIAL);
        final boolean preconditionsMet = evaluateCondition();
        if (preconditionsMet) {
            setState(PRECONDITIONS_MET);
        } else {
            setState(PRECONDITIONS_FAILED);
        }
        return getState();
    }

    @Override
    public final ActionState getState() {
        return actionState;
    }

    @Override
    public void initialize() {
        super.initialize();
        reset();
        if (condition != null) {
            condition.initialize();
        }
        successCount = 0;
        stepAtLastSuccess = -1;
    }

    @Nullable
    public final ActionCondition<A> getCondition() {
        return condition;
    }

    @Override
    public final void setCondition(@Nullable final ActionCondition<A> condition) {
        this.condition = condition;
        if (condition != null) {
            condition.setAction(this);
        }
    }

    @Override
    public final long lastCompletionStep() {
        return stepAtLastSuccess;
    }

    @Override
    public final Iterable<AgentNode> children() {
        return condition != null ? Collections.<AgentNode>singletonList(getCondition()) : Collections.<AgentNode>emptyList();
    }

    @Override
    public final AgentNode parent() {
        return agent().orNull();
    }

    public final Callback<? super AbstractAgentAction<A>, Void> getSuccessCallback() {
        return onSuccess;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected abstract static class AbstractBuilder<A extends Agent<A, ?>,
            T extends AbstractAgentAction<A>,
            B extends AbstractBuilder<A, T, B>> extends AbstractAgentComponent.AbstractBuilder<A, T, B>
            implements Serializable {
        private ActionCondition<A> condition;
        private Callback<? super AbstractAgentAction<A>, Void> onSuccess = Callbacks.emptyCallback();
        private int successCount;
        private long stepAtLastSuccess = -1;
        private ActionState actionState = INITIAL;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(final AbstractAgentAction<A> action) {
            super(action);
            this.condition = action.condition;
            this.onSuccess = action.onSuccess;
            this.successCount = action.successCount;
            this.stepAtLastSuccess = action.stepAtLastSuccess;
            this.actionState = action.actionState;
        }

        public final B executedIf(final ActionCondition<A> condition) {
            this.condition = condition;
            return self();
        }

        public final B onSuccess(final Callback<? super AbstractAgentAction<A>, Void> expression) {
            this.onSuccess = checkNotNull(expression);
            return self();
        }
    }
}
