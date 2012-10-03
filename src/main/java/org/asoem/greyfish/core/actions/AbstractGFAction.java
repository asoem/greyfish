package org.asoem.greyfish.core.actions;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.AgentNodes;
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
public abstract class AbstractGFAction extends AbstractAgentComponent implements GFAction {

    @Nullable
    private GFCondition rootCondition = null;

    private Callback<? super AbstractGFAction, Void> onSuccess;

    private int successCount;

    private int stepAtLastSuccess = -1;

    private ActionState actionState = ActionState.INITIAL;

    protected AbstractGFAction(AbstractGFAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.rootCondition = map.getClone(cloneable.getCondition(), GFCondition.class);
        this.onSuccess = cloneable.onSuccess;
    }

    protected AbstractGFAction(AbstractActionBuilder<? extends AbstractGFAction, ? extends AbstractActionBuilder> builder) {
        super(builder);
        this.rootCondition = builder.condition;
        this.onSuccess = builder.onSuccess;
    }

    @Override
    public final boolean evaluateCondition(Simulation simulation) {
        return rootCondition == null || rootCondition.apply(this);
    }

    /**
     * Called by the {@code Agent} which contains this {@code GFAction}
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
    public GFCondition getCondition() {
        return rootCondition;
    }

    @Element(name = "condition", required = false)
    @Override
    public void setCondition(@Nullable GFCondition rootCondition) {
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

        AbstractGFAction that = (AbstractGFAction) o;

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
    protected static abstract class AbstractActionBuilder<A extends AbstractGFAction, B extends AbstractActionBuilder<A, B>> extends AbstractComponentBuilder<A, B> {
        private GFCondition condition;
        private Callback<? super AbstractGFAction, Void> onSuccess = Callbacks.emptyCallback();

        public B executesIf(GFCondition condition) {
            this.condition = condition;
            return self();
        }

        public B onSuccess(Callback<? super AbstractGFAction, Void> expression) {
            this.onSuccess = checkNotNull(expression);
            return self();
        }
    }
}