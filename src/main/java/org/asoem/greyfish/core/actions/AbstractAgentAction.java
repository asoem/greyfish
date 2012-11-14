package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.core.conditions.ActionCondition;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.core.actions.utils.ActionState.*;

@Root
public abstract class AbstractAgentAction<A extends Agent<S,A,Z,P>, S extends Simulation<S,A,Z,P>, Z extends Space2D<A,P>, P extends Object2D> extends AbstractAgentComponent<A,S,Z,P> implements AgentAction<A,S,Z,P> {

    @Nullable
    private ActionCondition<A,S,Z,P> rootCondition;
    private Callback<? super AbstractAgentAction<A,S,Z,P>, Void> onSuccess;
    private int successCount;
    private int stepAtLastSuccess;
    private ActionState actionState;

    protected AbstractAgentAction(AbstractAgentAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.rootCondition = map.getClone(cloneable.getCondition(), ActionCondition.class);
        this.onSuccess = cloneable.onSuccess;
    }

    protected AbstractAgentAction(AbstractBuilder<A,S,Z,P,? extends AbstractAgentAction, ? extends AbstractBuilder<A,S,Z,P,?,?>> builder) {
        super(builder);
        this.onSuccess = builder.onSuccess;
        this.successCount = builder.successCount;
        this.stepAtLastSuccess = builder.stepAtLastSuccess;
        this.actionState = builder.actionState;

        setCondition(builder.condition);
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

        assert stepAtLastSuccess < agent().getSimulationStep() :
                "actions must not get executed twice per step: " + stepAtLastSuccess + " >= " + agent().getSimulationStep();

        if (INITIAL == actionState)
            checkPreconditions();

        if (PRECONDITIONS_MET == actionState
                || INTERMEDIATE == actionState) {

            final ActionState state = proceed();

            switch (state) {

                case COMPLETED:
                    ++successCount;
                    stepAtLastSuccess = agent().getSimulationStep();
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
    public ActionCondition<A,S,Z,P> getCondition() {
        return rootCondition;
    }

    @Element(name = "condition", required = false)
    @Override
    public void setCondition(@Nullable ActionCondition<A,S,Z,P> rootCondition) {
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
        return agent().getSimulationStep() - stepAtLastSuccess >= steps;
    }

    @Override
    public int lastCompletionStep() {
        return stepAtLastSuccess;
    }

    @Override
    public Iterable<AgentNode> children() {
        return rootCondition != null ? Collections.<AgentNode>singletonList(getCondition()) : Collections.<AgentNode>emptyList();
    }

    @Override
    public AgentNode parent() {
        return getAgent();
    }

    public Callback<? super AbstractAgentAction<A,S,Z,P>, Void> getSuccessCallback() {
        return onSuccess;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends Agent<S, A, Z, P>, S extends Simulation<S, A, Z, P>, Z extends Space2D<A, P>, P extends Object2D, T extends AbstractAgentAction, B extends AbstractBuilder<A,S,Z,P,T,B>> extends AbstractAgentComponent.AbstractBuilder<A,S,Z,P,T,B> implements Serializable {
        private ActionCondition<A,S,Z,P> condition;
        private Callback<? super AbstractAgentAction<A,S,Z,P>, Void> onSuccess = Callbacks.emptyCallback();
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

        public B executedIf(ActionCondition<A,S,Z,P> condition) {
            this.condition = condition;
            return self();
        }

        public B onSuccess(Callback<? super AbstractAgentAction<A,S,Z,P>, Void> expression) {
            this.onSuccess = checkNotNull(expression);
            return self();
        }
    }
}