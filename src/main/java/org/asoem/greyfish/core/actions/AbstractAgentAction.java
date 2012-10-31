package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.asoem.greyfish.core.conditions.ActionCondition;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.core.actions.utils.ActionState.*;

@Root
public abstract class AbstractAgentAction extends AbstractAgentComponent implements AgentAction {

    @Nullable
    private ActionCondition rootCondition;
    private Callback<? super AbstractAgentAction, Void> onSuccess;
    private int successCount;
    private int stepAtLastSuccess;
    private ActionState actionState;

    protected AbstractAgentAction(AbstractAgentAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.rootCondition = map.getClone(cloneable.getCondition(), ActionCondition.class);
        this.onSuccess = cloneable.onSuccess;
    }

    protected AbstractAgentAction(AbstractBuilder<? extends AbstractAgentAction, ? extends AbstractBuilder> builder) {
        super(builder);
        this.rootCondition = builder.condition;
        this.onSuccess = builder.onSuccess;
        this.successCount = builder.successCount;
        this.stepAtLastSuccess = builder.stepAtLastSuccess;
        this.actionState = builder.actionState;
    }

    @Override
    public final boolean evaluateCondition() {
        return rootCondition == null || rootCondition.evaluate();
    }

    /**
     * Called by the {@code Agent} which contains this {@code AgentAction}
     *
     */
    @Override
    public ActionState apply() {

        final Simulation simulation = simulation();

        assert stepAtLastSuccess < simulation.getStep() :
                "actions must not get executed twice per step: " + stepAtLastSuccess + " >= " + simulation.getStep();

        if (INITIAL == actionState)
            checkPreconditions();

        if (PRECONDITIONS_MET == actionState
                || INTERMEDIATE == actionState) {

            final ActionState state = proceed(simulation);

            switch (state) {

                case COMPLETED:
                    ++successCount;
                    stepAtLastSuccess = simulation.getStep();
                    Callbacks.call(onSuccess, this);
                    break;

                default:
                    break;
            }

            setState(state);
        }

        return actionState;
    }

    protected abstract ActionState proceed(Simulation simulation);

    protected void setState(ActionState state) {
        assert state != null;
        actionState = state;
    }

    @Override
    public void reset() {
        setState(INITIAL);
    }

    @Override
    public ActionState checkPreconditions() {
        checkState(actionState == INITIAL, "Action not is state %s", INITIAL);
        final boolean preconditionsMet = evaluateCondition();
        if (preconditionsMet)
            setState(PRECONDITIONS_MET);
        else
            setState(PRECONDITIONS_FAILED);
        return getState();
    }

    @Override
    public ActionState getState() {
        return actionState;
    }

    @Override
    public void initialize() {
        super.initialize();
        reset();
        if (rootCondition != null)
            rootCondition.initialize();
        successCount = 0;
        stepAtLastSuccess = -1;
    }

    @Nullable
    @Element(name = "condition", required = false)
    public ActionCondition getCondition() {
        return rootCondition;
    }

    @Element(name = "condition", required = false)
    @Override
    public void setCondition(@Nullable ActionCondition rootCondition) {
        this.rootCondition = rootCondition;
        if (rootCondition != null) {
            rootCondition.setAction(this);
        }
    }

    @Override
    public int getCompletionCount() {
        return this.successCount;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
    }

    public boolean wasNotExecutedForAtLeast(int steps) {
        // TODO: logical error: stepAtLastSuccess = 0 does not mean, that it really did execute at 0
        return simulation().getStep() - stepAtLastSuccess >= steps;
    }

    @Override
    public int lastCompletionStep() {
        return stepAtLastSuccess;
    }

    @Override
    public Iterable<AgentComponent> children() {
        return rootCondition != null ? Collections.<AgentComponent>singletonList(getCondition()) : Collections.<AgentComponent>emptyList();
    }

    public Callback<? super AbstractAgentAction, Void> getSuccessCallback() {
        return onSuccess;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends AbstractAgentAction, B extends AbstractBuilder<A, B>> extends AbstractAgentComponent.AbstractBuilder<A, B> implements Serializable {
        private ActionCondition condition;
        private Callback<? super AbstractAgentAction, Void> onSuccess = Callbacks.emptyCallback();
        private int successCount;
        private int stepAtLastSuccess = -1;
        private ActionState actionState = ActionState.INITIAL;

        protected AbstractBuilder() {}

        protected AbstractBuilder(AbstractAgentAction action) {
            super(action);
            this.condition = action.rootCondition;
            this.onSuccess = action.onSuccess;
            this.successCount = action.successCount;
            this.stepAtLastSuccess = action.stepAtLastSuccess;
            this.actionState = action.actionState;
        }

        public B executedIf(ActionCondition condition) {
            this.condition = condition;
            return self();
        }

        public B onSuccess(Callback<? super AbstractAgentAction, Void> expression) {
            this.onSuccess = checkNotNull(expression);
            return self();
        }
    }
}