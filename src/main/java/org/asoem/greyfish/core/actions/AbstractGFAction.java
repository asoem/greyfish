package org.asoem.greyfish.core.actions;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.individual.*;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;
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
        this.rootCondition = map.cloneField(cloneable.getCondition(), GFCondition.class);
        this.onSuccess = cloneable.onSuccess;
    }

    protected AbstractGFAction(AbstractActionBuilder<? extends AbstractGFAction, ? extends AbstractActionBuilder> builder) {
        super(builder);
        this.rootCondition = builder.condition;
        this.onSuccess = builder.onSuccess;
    }

    @Override
    public final boolean evaluateCondition(Simulation simulation) {
        return rootCondition == null || rootCondition.evaluate(simulation);
    }

    /**
     * Called by the {@code Agent} which contains this {@code GFAction}
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

                case SUCCESS:
                    onSuccess(simulation);
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
    public boolean checkPreconditions(Simulation simulation) {
        final boolean preconditionsMet = evaluateCondition(simulation);
        if (preconditionsMet)
            setState(PRECONDITIONS_MET);
        else
            setState(PRECONDITIONS_FAILED);
        return preconditionsMet;
    }

    @Override
    public ActionState getState() {
        return actionState;
    }

    private void onSuccess(Simulation simulation) {
        ++successCount;
        stepAtLastSuccess = simulation.getStep();
        onSuccess.apply(this, ImmutableMap.of());
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
    @Element(name="condition", required=false)
    public GFCondition getCondition() {
        return rootCondition;
    }

    @Element(name="condition", required=false)
    @Override
    public void setCondition(@Nullable GFCondition rootCondition) {
        this.rootCondition = rootCondition;
        if (rootCondition != null)
            rootCondition.setAgent(this.getAgent());
    }

    @Override
    public int getSuccessCount() {
        return this.successCount;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
    }

    public boolean wasNotExecutedForAtLeast(final Simulation simulation, final int steps) {
        // TODO: logical error: stepAtLastSuccess = 0 does not mean, that it really did execute at 0
        return simulation.getStep() - stepAtLastSuccess >= steps;
    }

    @Override
    public int stepsSinceLastExecution() {
        return agent().getSimulationContext().getSimulation().getStep() - stepAtLastSuccess;
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Iterable<AgentComponent> children() {
        return rootCondition != null ? Collections.<AgentComponent>singletonList(getCondition()) : Collections.<AgentComponent>emptyList();
    }

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
        long temp;
        result = 31 * result + (rootCondition != null ? rootCondition.hashCode() : 0);
        result = 31 * result + successCount;
        result = 31 * result + stepAtLastSuccess;
        result = 31 * result + (actionState != null ? actionState.hashCode() : 0);
        return result;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractActionBuilder<A extends AbstractGFAction, B extends AbstractActionBuilder<A, B>> extends AbstractComponentBuilder<A, B> {
        private GFCondition condition;
        private DoubleProperty source;
        private GreyfishExpression formula = GreyfishExpressionFactoryHolder.compile("0");
        private Callback<? super AbstractGFAction, Void> onSuccess = Callbacks.emptyCallback();

        public B executesIf(GFCondition condition) { this.condition = condition; return self(); }
        private B source(DoubleProperty source) { this.source = source; return self(); }
        private B formula(String formula) { this.formula = GreyfishExpressionFactoryHolder.compile(formula); return self(); }
        public B generatesCosts(DoubleProperty source, String formula) {
            return source(checkNotNull(source)).formula(checkNotNull(formula)); /* TODO: formula should be evaluated */ }
        public B onSuccess(Callback<? super AbstractGFAction, Void> expression) { this.onSuccess = checkNotNull(expression); return self(); }
    }
}