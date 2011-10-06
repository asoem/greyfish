package org.asoem.greyfish.core.actions;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.actions.AbstractGFAction.ExecutionResult.*;
import static org.asoem.greyfish.core.eval.GreyfishExpressionFactory.compileExpression;

@Root
public abstract class AbstractGFAction extends AbstractAgentComponent implements GFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGFAction.class);

    @Element(name="condition", required=false)
    private Optional<GFCondition> rootCondition = Optional.absent();

    @Element(name="costs_formula", required = false)
    private GreyfishExpression<AbstractGFAction> energyCosts = compileExpression("0").forContext(AbstractGFAction.class);

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
    public final boolean evaluateConditions(Simulation simulation) {
        return rootCondition.isPresent() && rootCondition.get().evaluate(simulation);
    }

    /**
     * Called by the individual to evaluateConditions the condition if set and trigger the actions
     * @param simulation the simulation context
     */
    @Override
    public final ExecutionResult execute(Simulation simulation) {
        Preconditions.checkNotNull(simulation);
        Preconditions.checkState(agent.isPresent());

        try {
            if (isDormant()) {
                if (!evaluateConditions(simulation)) {
                    return CONDITIONS_FAILED;
                }
                if (hasCosts()) {
                    evaluatedCostsFormula = evaluateFormula(simulation);
                    if (energySource.get().compareTo(evaluatedCostsFormula) < 0)
                        return INSUFFICIENT_ENERGY;
                }

            }

            this.state = executeUnconditioned(simulation);


            switch (state) {
                case ACTIVE:
                    return EXECUTED;
                case END_SUCCESS:
                case END_FAILED:
                    postExecutionTasks(simulation);
                    reset();
                    state = State.DORMANT;
                    return EXECUTED;
                default:
                    throw new Exception("Overwritten method executeUnconditioned() must not return " + state + " in " + this);
            }
        }
        catch (Exception e) {
            LOGGER.error("Execution error in {} with simulation = {}.", this, simulation, e);
            return ERROR;
        }
    }

    private boolean hasCosts() {
        return energySource != null && energyCosts != null;
    }

    private void postExecutionTasks(Simulation simulation) {
        ++executionCount;
        timeOfLastExecution = simulation.getSteps();

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
     * @param simulation the simulation context
     * @return the state in which this action should be after execution
     */
    protected abstract State executeUnconditioned(Simulation simulation);

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        if (rootCondition.isPresent())
            rootCondition.get().prepare(simulation);
        executionCount = 0;
        timeOfLastExecution = simulation.getSteps();
    }

    @Override
    public double evaluateFormula(Simulation simulation) {
        try {
            return energyCosts.evaluateAsDouble(this);
        } catch (EvaluationException e) {
            LOGGER.error("Costs formula could not be evaluated: {}.", energyCosts, e);
            return 0;
        }
    }

    @Element(name="condition", required=false)
    public GFCondition getRootCondition() {
        return rootCondition.orNull();
    }

    @Element(name="condition", required=false)
    @Override
    public void setRootCondition(GFCondition rootCondition) {
        this.rootCondition = Optional.fromNullable(rootCondition);
        rootCondition.setAgent(this.getAgent());
    }

    @Override
    public int getExecutionCount() {
        return this.executionCount;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);

        e.add(new ValueAdaptor<GreyfishExpression>("Energy Costs", GreyfishExpression.class) {
            @Override
            public GreyfishExpression get() {
                return energyCosts;
            }

            @Override
            protected void set(GreyfishExpression arg0) {
                energyCosts = arg0;
            }
        });

        e.add(new FiniteSetValueAdaptor<DoubleProperty>("Energy Source", DoubleProperty.class
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
                return Iterables.filter(agent.get().getProperties(), DoubleProperty.class);
            }
        });
    }

    public boolean wasNotExecutedForAtLeast(final Simulation simulation, final int steps) {
        // TODO: logical error: timeOfLastExecution = 0 does not mean, that it really did execute at 0
        return simulation.getSteps() - timeOfLastExecution >= steps;
    }

    @Override
    public final boolean isDormant() {
        return state == State.DORMANT;
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    protected AbstractGFAction(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.energyCosts = builder.formula;
        this.energySource = builder.source;
        this.rootCondition = Optional.fromNullable(builder.condition);
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractAgentComponent.AbstractBuilder<T> {
        private GFCondition condition;
        private DoubleProperty source;
        private GreyfishExpression<AbstractGFAction> formula = compileExpression("0").forContext(AbstractGFAction.class);

        public T executesIf(GFCondition condition) { this.condition = condition; return self(); }
        private T source(DoubleProperty source) { this.source = source; return self(); }
        private T formula(String formula) { this.formula = compileExpression(formula).forContext(AbstractGFAction.class); return self(); }
        public T generatesCosts(DoubleProperty source, String formula) {
            return source(checkNotNull(source)).formula(checkNotNull(formula)); /* TODO: formula should be evaluated */ }
    }

    protected AbstractGFAction(AbstractGFAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.rootCondition = Optional.fromNullable(map.continueWith(cloneable.getRootCondition(), GFCondition.class));
        this.energySource = map.continueWith(cloneable.energySource, DoubleProperty.class);
        this.energyCosts = cloneable.energyCosts;
    }

    @Override
    public Iterable<AgentComponent> children() {
        return rootCondition.isPresent() ? Collections.<AgentComponent>singletonList(getRootCondition()) : Collections.<AgentComponent>emptyList();
    }
}