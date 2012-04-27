package org.asoem.greyfish.core.actions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.actions.utils.ActionState.*;

@Root
public abstract class AbstractGFAction extends AbstractAgentComponent implements GFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGFAction.class);

    @Nullable
    private GFCondition rootCondition = null;

    @Element(name="costs_formula", required = false)
    private GreyfishExpression energyCosts;

    @Element(name="energy_source", required=false)
    private DoubleProperty energySource;

    private int successCount;

    private int stepAtLastSuccess = -1;

    private double costsOnSuccess;

    private ActionState actionState = ActionState.INITIAL;

    protected AbstractGFAction(AbstractGFAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.rootCondition = map.cloneField(cloneable.getCondition(), GFCondition.class);
        this.energySource = map.cloneField(cloneable.energySource, DoubleProperty.class);
        this.energyCosts = cloneable.energyCosts;
    }

    protected AbstractGFAction(AbstractBuilder<? extends AbstractGFAction, ? extends AbstractBuilder> builder) {
        super(builder);
        this.energyCosts = builder.formula;
        this.energySource = builder.source;
        this.rootCondition = builder.condition;
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

        assert stepAtLastSuccess < simulation.getCurrentStep() :
                "actions must not get executed twice per step: " + stepAtLastSuccess + " >= " + simulation.getCurrentStep();

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
        costsOnSuccess = -1;
    }

    @Override
    public boolean checkPreconditions(Simulation simulation) {
        final boolean preconditionsMet = evaluateCondition(simulation) && checkCosts();
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

    private void calculateCosts() {
        costsOnSuccess = energyCosts.evaluateForContext(this).asDouble();
    }

    private boolean checkCosts() {
        if (generatesCosts()) {
            calculateCosts();
            return energySource.get().compareTo(costsOnSuccess) < 0;
        }
        else
            return true;
    }

    private boolean generatesCosts() {
        return energySource != null && energyCosts != null;
    }

    private void onSuccess(Simulation simulation) {
        ++successCount;
        stepAtLastSuccess = simulation.getCurrentStep();

        if (generatesCosts()) {
            energySource.subtract(costsOnSuccess);
            LOGGER.debug("{}: Subtracted {} execution costs from {}: {} remaining",
                    this, costsOnSuccess, energySource.getName(), energySource.get());
        }
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

        e.add("Energy Costs", new AbstractTypedValueModel<GreyfishExpression>() {
            @Override
            public GreyfishExpression get() {
                return energyCosts;
            }

            @Override
            protected void set(GreyfishExpression arg0) {
                energyCosts = GreyfishExpressionFactoryHolder.compile(arg0.getExpression());
            }
        });

        e.add("Energy Source", new SetAdaptor<DoubleProperty>(DoubleProperty.class
        ) {
            @Override
            protected void set(DoubleProperty arg0) {
                energySource = arg0;
            }

            @Override
            public DoubleProperty get() {
                return energySource;
            }

            @Override
            public Iterable<DoubleProperty> values() {
                return Iterables.filter(agent().getProperties(), DoubleProperty.class);
            }
        });
    }

    public boolean wasNotExecutedForAtLeast(final Simulation simulation, final int steps) {
        // TODO: logical error: stepAtLastSuccess = 0 does not mean, that it really did execute at 0
        return simulation.getCurrentStep() - stepAtLastSuccess >= steps;
    }

    @Override
    public int stepsSinceLastExecution() {
        return agent().getSimulationContext().getSimulation().getCurrentStep() - stepAtLastSuccess;
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

        if (Double.compare(that.costsOnSuccess, costsOnSuccess) != 0) return false;
        if (successCount != that.successCount) return false;
        if (stepAtLastSuccess != that.stepAtLastSuccess) return false;
        if (actionState != that.actionState) return false;
        if (energyCosts != null ? !energyCosts.equals(that.energyCosts) : that.energyCosts != null) return false;
        if (energySource != null ? !energySource.equals(that.energySource) : that.energySource != null) return false;
        if (rootCondition != null ? !rootCondition.equals(that.rootCondition) : that.rootCondition != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + (rootCondition != null ? rootCondition.hashCode() : 0);
        result = 31 * result + (energyCosts != null ? energyCosts.hashCode() : 0);
        result = 31 * result + (energySource != null ? energySource.hashCode() : 0);
        result = 31 * result + successCount;
        result = 31 * result + stepAtLastSuccess;
        temp = costsOnSuccess != +0.0d ? Double.doubleToLongBits(costsOnSuccess) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (actionState != null ? actionState.hashCode() : 0);
        return result;
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends AbstractGFAction, T extends AbstractBuilder<E,T>> extends AbstractAgentComponent.AbstractBuilder<E,T> {
        private GFCondition condition;
        private DoubleProperty source;
        private GreyfishExpression formula = GreyfishExpressionFactoryHolder.compile("0");

        public T executesIf(GFCondition condition) { this.condition = condition; return self(); }
        private T source(DoubleProperty source) { this.source = source; return self(); }
        private T formula(String formula) { this.formula = GreyfishExpressionFactoryHolder.compile(formula); return self(); }
        public T generatesCosts(DoubleProperty source, String formula) {
            return source(checkNotNull(source)).formula(checkNotNull(formula)); /* TODO: formula should be evaluated */ }
    }
}