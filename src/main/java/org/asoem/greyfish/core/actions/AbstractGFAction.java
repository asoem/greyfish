package org.asoem.greyfish.core.actions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.actions.utils.ExecutionResult;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
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
import static org.asoem.greyfish.core.actions.utils.ExecutionResult.*;

@Root
public abstract class AbstractGFAction extends AbstractAgentComponent implements GFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGFAction.class);

    @Element(name="condition", required=false)
    @Nullable
    private GFCondition rootCondition = null;

    @Element(name="costs_formula", required = false)
    private GreyfishExpression energyCosts = GreyfishExpressionFactory.compile("0");

    @Element(name="energy_source", required=false)
    private DoubleProperty energySource;

    private int executionCount;

    private int timeOfLastExecution;

    private double evaluatedCostsFormula;

    protected AbstractGFAction(AbstractGFAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.rootCondition = map.cloneField(cloneable.getRootCondition(), GFCondition.class);
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
    public ActionState getActionState() {
        return actionState;
    }

    private ActionState actionState = ActionState.DORMANT;

    @Override
    public final boolean evaluateCondition(Simulation simulation) {
        return rootCondition == null || rootCondition.apply(simulation);
    }

    /**
     * Called by the {@code Agent} which contains this {@code GFAction}
     * @param simulation the simulation context
     */
    @Override
    public ExecutionResult execute(Simulation simulation) {
        Preconditions.checkNotNull(simulation);

        try {
            if (isDormant()) {
                if (!evaluateCondition(simulation)) {
                    return CONDITIONS_FAILED;
                }
                if (hasCosts()) {
                    evaluatedCostsFormula = evaluateFormula(simulation);
                    if (energySource.get().compareTo(evaluatedCostsFormula) < 0)
                        return INSUFFICIENT_ENERGY;
                }

            }

            this.actionState = executeUnconditioned(simulation);
            // TODO: currently only two states are sufficient. Change to boolean?

            switch (actionState) {
                case ACTIVE:
                    return EXECUTED;
                case END_FAILED:
                    actionState = ActionState.DORMANT;
                    return ERROR;
                case END_SUCCESS:
                    postExecutionTasks(simulation);
                    actionState = ActionState.DORMANT;
                    return EXECUTED;
                default:
                    throw new RuntimeException("Overwritten method executeUnconditioned() must not return " + actionState + " in " + this);
            }
        }
        catch (RuntimeException e) {
            LOGGER.error("Execution error in {} with simulation = {}.", this, simulation, e);
            throw e;
        }
    }

    private boolean hasCosts() {
        return energySource != null && energyCosts != null;
    }

    private void postExecutionTasks(Simulation simulation) {
        ++executionCount;
        timeOfLastExecution = simulation.getSteps();

        if (actionState == ActionState.END_SUCCESS)
            if (hasCosts()) {
                if (evaluatedCostsFormula != 0) {
                    energySource.subtract(evaluatedCostsFormula);
                    LOGGER.debug("{}: Subtracted {} execution costs from {}: {} remaining",
                            this, evaluatedCostsFormula, energySource.getName(), energySource.get());
                }
            }
    }

    /**
     * In this method the behaviour of this action is implemented. This method should not be called directly.
     *
     *
     * @param simulation the simulation context
     * @return the state in which this action should be after execution
     */
    protected abstract ActionState executeUnconditioned(Simulation simulation);

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        if (rootCondition != null)
            rootCondition.prepare(simulation);
        executionCount = 0;
        timeOfLastExecution = simulation.getSteps();
    }

    @Override
    public double evaluateFormula(Simulation simulation) {
        try {
            return energyCosts.evaluateForContext(this).asDouble();
        } catch (EvaluationException e) {
            LOGGER.error("Costs formula could not be evaluated: {}.", energyCosts, e);
            return 0;
        }
    }

    @Nullable
    @Element(name="condition", required=false)
    public GFCondition getRootCondition() {
        return rootCondition;
    }

    @Element(name="condition", required=false)
    @Override
    public void setRootCondition(@Nullable GFCondition rootCondition) {
        this.rootCondition = rootCondition;
        if (rootCondition != null)
            rootCondition.setAgent(this.getAgent());
    }

    @Override
    public int getExecutionCount() {
        return this.executionCount;
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
                energyCosts = GreyfishExpressionFactory.compile(arg0.getExpression());
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
        // TODO: logical error: timeOfLastExecution = 0 does not mean, that it really did execute at 0
        return simulation.getSteps() - timeOfLastExecution >= steps;
    }

    @Override
    public final boolean isDormant() {
        return actionState == ActionState.DORMANT;
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Iterable<AgentComponent> children() {
        return rootCondition != null ? Collections.<AgentComponent>singletonList(getRootCondition()) : Collections.<AgentComponent>emptyList();
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends AbstractGFAction, T extends AbstractBuilder<E,T>> extends AbstractAgentComponent.AbstractBuilder<E,T> {
        private GFCondition condition;
        private DoubleProperty source;
        private GreyfishExpression formula = GreyfishExpressionFactory.compile("0");

        public T executesIf(GFCondition condition) { this.condition = condition; return self(); }
        private T source(DoubleProperty source) { this.source = source; return self(); }
        private T formula(String formula) { this.formula = GreyfishExpressionFactory.compile(formula); return self(); }
        public T generatesCosts(DoubleProperty source, String formula) {
            return source(checkNotNull(source)).formula(checkNotNull(formula)); /* TODO: formula should be evaluated */ }
    }
}