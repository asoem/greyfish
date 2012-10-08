package org.asoem.greyfish.core.actions;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.asoem.greyfish.core.agent.AgentNodes;
import org.asoem.greyfish.core.conditions.ActionCondition;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.core.actions.utils.ActionState.*;

@Root
public abstract class AbstractAgentAction extends AbstractAgentComponent implements AgentAction {

    @Nullable
    private ActionCondition rootCondition = null;

    private Callback<? super AbstractAgentAction, Void> onSuccess;

    private int successCount;

    private int stepAtLastSuccess = -1;

    private ActionState actionState = ActionState.INITIAL;

    protected AbstractAgentAction(AbstractAgentAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.rootCondition = map.getClone(cloneable.getCondition(), ActionCondition.class);
        this.onSuccess = cloneable.onSuccess;
    }

    protected AbstractAgentAction(AbstractBuilder<? extends AbstractAgentAction, ? extends AbstractBuilder> builder) {
        super(builder);
        this.rootCondition = builder.condition;
        this.onSuccess = builder.onSuccess;
    }

    @Override
    public final boolean evaluateCondition(Simulation simulation) {
        return rootCondition == null || rootCondition.apply(this);
    }

    /**
     * Called by the {@code Agent} which contains this {@code AgentAction}
     *
     * @param simulation the simulation context
     */
    @Override
    public ActionState apply(Simulation simulation) {
        Preconditions.checkNotNull(simulation);

        assert stepAtLastSuccess < simulation.getStep() :
                "actions must not get executed twice per step: " + stepAtLastSuccess + " >= " + simulation.getStep();

        if (INITIAL == actionState)
            checkPreconditions(simulation);

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
    public ActionState checkPreconditions(Simulation simulation) {
        checkState(actionState == INITIAL, "Action not is state %s", INITIAL);
        final boolean preconditionsMet = evaluateCondition(simulation());
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
        if (rootCondition != null)
            for (AgentComponent component : AgentNodes.<AgentComponent>postOrderIteration(rootCondition))
                component.setAgent(this.getAgent());
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

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AbstractAgentAction that = (AbstractAgentAction) o;

        if (successCount != that.successCount) return false;
        if (stepAtLastSuccess != that.stepAtLastSuccess) return false;
        if (actionState != that.actionState) return false;
        if (rootCondition != null ? !rootCondition.equals(that.rootCondition) : that.rootCondition != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (rootCondition != null ? rootCondition.hashCode() : 0);
        result = 31 * result + successCount;
        result = 31 * result + stepAtLastSuccess;
        result = 31 * result + (actionState != null ? actionState.hashCode() : 0);
        return result;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends AbstractAgentAction, B extends AbstractBuilder<A, B>> extends AbstractAgentComponent.AbstractBuilder<A, B> {
        private ActionCondition condition;
        private Callback<? super AbstractAgentAction, Void> onSuccess = Callbacks.emptyCallback();

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