package org.asoem.greyfish.core.actions;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.conditions.ConditionTree;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishMathExpression;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.IndividualInterface;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.core.actions.AbstractGFAction.ExecutionResult.*;

@Root
public abstract class AbstractGFAction extends AbstractGFComponent implements GFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGFAction.class);

    private ConditionTree conditionTree;

    @Element(name="costs_formula", required = false)
    private String energyCostsFormula = "0";

    @Element(name="energy_source", required=false)
    private DoubleProperty energySource;

    private int executionCount;

    private int timeOfLastExecution;
    private double evaluatedCostsFormula;

    public static enum State {
        DORMANT,
        ACTIVE,
        END_SUCCESS,
        END_FAILED,
        END_ERROR
    }

    public State getState() {
        return state;
    }

    private State state = State.DORMANT;

    public static enum ExecutionResult {
        CONDITIONS_FAILED,
        INSUFFICIENT_ENERGY,
        FAILED,
        EXECUTED,
        ERROR,
    }

    @Override
    public final boolean evaluateConditions(ActionContext context) {
        return conditionTree.evaluate(context);
    }

    /**
     * Called by the individual to evaluateConditions the condition if set and trigger the actions
     * @param context
     */
    @Override
    public final ExecutionResult execute(final ActionContext context) {
        Preconditions.checkNotNull(context);

        try {
            if (isDormant()) {
                if (!evaluateConditions(context)) {
                    return CONDITIONS_FAILED;
                }
                if (hasCosts()) {
                    evaluatedCostsFormula = evaluateFormula(context);
                    if (energySource.get().compareTo(evaluatedCostsFormula) < 0)
                        return INSUFFICIENT_ENERGY;
                }

            }

            this.state = executeUnconditioned(context);


            switch (state) {
                case ACTIVE:
                    return EXECUTED;
                case END_SUCCESS:
                case END_FAILED:
                    postExecutionTasks(context);
                    reset();
                    state = State.DORMANT;
                    return EXECUTED;
                default:
                    throw new Exception("Overwritten method executeUnconditioned() must not return " + state + " in " + this);
            }
        }
        catch (Exception e) {
            LOGGER.error("Execution error in {} with simulation = {}.", this, context, e);
            return ERROR;
        }
    }

    private boolean hasCosts() {
        return energySource != null && energyCostsFormula != null;
    }

    private void postExecutionTasks(ActionContext context) {
        ++executionCount;
        timeOfLastExecution = context.getSimulation().getSteps();

        if (state == State.END_SUCCESS)
            if (hasCosts()) {
                if (evaluatedCostsFormula != 0) {
                    energySource.subtract(evaluatedCostsFormula);
                    LOGGER.debug("{}: Subtracted {} execution costs from {}: {} remaining",
                            this, evaluatedCostsFormula, energySource.getName(), energySource.get());
                }
            }
    }

    /**
     * This method is called after this action was executed and caused a transition into an END_* state.
     * If you overwrite this method make sure you call {@code super.reset()}.
     */
    protected void reset() {}

    /**
     * In this method the behaviour of this action is implemented. This method should not be called directly.
     *
     * @param context@return the state of the action after execution
     */
    protected abstract State executeUnconditioned(@Nonnull ActionContext context);

    @Override
    public void setComponentRoot(IndividualInterface individual) {
        super.setComponentRoot(individual);
        conditionTree.setComponentRoot(getComponentOwner());
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        conditionTree.prepare(simulation);
        executionCount = 0;
        timeOfLastExecution = simulation.getSteps();
    }

    @Override
    public ConditionTree getConditionTree() {
        return conditionTree;
    }

    @Override
    public double evaluateFormula(ActionContext context) {
        try {
            return GreyfishMathExpression.evaluateAsDouble(energyCostsFormula, context.getAgent());
        } catch (EvaluationException e) {
            LOGGER.error("Costs formula could not be evaluated: {}. Setting formula to '0'", energyCostsFormula, e);
            energyCostsFormula = "0";
            return 0;
        }
    }

    @Element(name="condition", required=false)
    public GFCondition getRootCondition() {
        return conditionTree.getRootCondition();
    }

    @Element(name="condition", required=false)
    @Override
    public void setRootCondition(GFCondition rootCondition) {
        conditionTree = new ConditionTree(checkFrozen(rootCondition));
        conditionTree.setComponentRoot(this.getComponentOwner());
    }

    @Override
    public int getExecutionCount() {
        return this.executionCount;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        e.add(new ValueAdaptor<String>("Energy Costs", String.class) {
            @Override
            public String get() {
                return energyCostsFormula;
            }

            @Override
            protected void set(String arg0) {
                energyCostsFormula = checkFrozen(arg0);
            }
        });
        //		e.sum( new ValueAdaptor<Boolean>("Is last?", Boolean.class, parameterLast)
        //				{ @Override protected void set(Boolean arg0) { parameterLast = arg0; }});
        e.add(new FiniteSetValueAdaptor<DoubleProperty>("Energy Source", DoubleProperty.class
        ) {
            @Override
            protected void set(DoubleProperty arg0) {
                energySource = checkFrozen(arg0);
            }

            @Override
            public DoubleProperty get() {
                return energySource;
            }

            @Override
            public Iterable<DoubleProperty> values() {
                return Iterables.filter(getComponentOwner().getProperties(), DoubleProperty.class);
            }
        });
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) {
        super.checkConsistency(components);
        checkState(Iterables.contains(components, energySource));
    }

    public boolean wasNotExecutedForAtLeast(final Simulation simulation, final int steps) {
        // TODO: logical error: timeOfLastExecution = 0 does not mean, that it really did execute at 0
        return simulation.getSteps() - timeOfLastExecution >= steps;
    }

    @Override
    public final boolean isDormant() {
        return state == State.DORMANT;
    }

    protected AbstractGFAction(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.energyCostsFormula = builder.formula;
        this.energySource = builder.source;
        this.conditionTree = new ConditionTree(builder.condition);
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFComponent.AbstractBuilder<T> {
        private GFCondition condition;
        private DoubleProperty source;
        private String formula = "0";

        public T executesIf(GFCondition condition) { this.condition = condition; return self(); }
        private T source(DoubleProperty source) { this.source = source; return self(); }
        private T formula(String formula) { this.formula = formula; return self(); }
        public T generatesCosts(DoubleProperty source, String formula) {
            return source(checkNotNull(source)).formula(checkNotNull(formula)); /* TODO: formula should be evaluated */ }
    }

    protected AbstractGFAction(AbstractGFAction cloneable, CloneMap map) {
        super(cloneable, map);
        this.conditionTree = new ConditionTree(map.clone(cloneable.getRootCondition(), GFCondition.class));
        this.energySource = map.clone(cloneable.energySource, DoubleProperty.class);
        this.energyCostsFormula = cloneable.energyCostsFormula;
    }
}