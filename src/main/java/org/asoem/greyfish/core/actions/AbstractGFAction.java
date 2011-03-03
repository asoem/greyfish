package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.conditions.ConditionTree;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.IndividualInterface;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.GreyfishMathExpression;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.core.io.GreyfishLogger.CORE_LOGGER;
import static org.asoem.greyfish.core.io.GreyfishLogger.GFACTIONS_LOGGER;

@Root
public abstract class AbstractGFAction extends AbstractGFComponent implements GFAction {

    private ConditionTree conditionTree;

    @Element(name="costs_formula", required = false)
    private String energyCostsFormula = "0";

    @Element(name="energy_source", required=false)
    private DoubleProperty energySource;

    private int executionCount;

    private int timeOfLastExecution;

    public enum ExecutionResult {
        CONDITIONS_FAILED,
        INSUFFICIENT_ENERGY,
        INVALID_INTERNAL_STATE,
        EXECUTED,
        ERROR,
    }

    @Override
    public final boolean evaluateConditions(Simulation simulation) {
        return conditionTree.evaluate(simulation);
    }

    protected final boolean evaluateCosts() {
        // test for energy
        double needed = evaluateFormula();
        if (energySource != null && energySource.get().compareTo(needed) < 0) {
            if (GFACTIONS_LOGGER.hasDebugEnabled())
                GFACTIONS_LOGGER.debug("Evaluation of " + this + " evaluated to false for energy reasons. " +
                        "Needed=" + needed + "; available=" + energySource.get());
            return false;
        }

        return true;
    }

    protected boolean evaluateInternalState(final Simulation simulation) {
        return true;
    }

    /**
     * Called by the individual to evaluateConditions the condition if set and trigger the actions
     * @param simulation
     */
    @Override
    public final ExecutionResult execute(final Simulation simulation) {
        try {

            if (!isResuming()) {
                if (!evaluateConditions(simulation))
                    return ExecutionResult.CONDITIONS_FAILED;

                if (!evaluateCosts())
                    return ExecutionResult.INSUFFICIENT_ENERGY;

                if (!evaluateInternalState(simulation))
                    return ExecutionResult.INVALID_INTERNAL_STATE;
            }

            return executeUnevaluated(simulation);
        }
        catch (Exception e) {
            CORE_LOGGER.error("Error during execution of " + this, e);
            return AbstractGFAction.ExecutionResult.ERROR;
        }
    }

    private ExecutionResult executeUnevaluated(final Simulation simulation) {
        performAction(simulation);

        ++executionCount;
        timeOfLastExecution = simulation.getSteps();

        if (energySource != null && !isResuming()) {
            energySource.subtract(evaluateFormula());
        }

        return ExecutionResult.EXECUTED;
    }

    /**
     * This is the actual actions
     * @param simulation
     */
    protected abstract void performAction(Simulation simulation);

    @Override
    public void setComponentRoot(IndividualInterface individual) {
        super.setComponentRoot(individual);
        conditionTree.setComponentRoot(getComponentOwner());
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        conditionTree.initialize(simulation);
        executionCount = 0;
        timeOfLastExecution = simulation.getSteps();
    }

    @Override
    public ConditionTree getConditionTree() {
        return conditionTree;
    }

    @Override
    public double evaluateFormula() {
        return GreyfishMathExpression.evaluate(energyCostsFormula, Agent.class.cast(getComponentOwner()));
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
    public void export(Exporter e) {
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
        //		e.add( new ValueAdaptor<Boolean>("Is last?", Boolean.class, parameterLast)
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
    public boolean isResuming() {
        return false;
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

    protected void sendMessage(ACLMessage message) {
        getSimulation().deliverMessage(message);
    }

    protected List<ACLMessage> receiveMessages(MessageTemplate template) {
        return getComponentOwner().pollMessages(template);
    }

    protected boolean hasMessages(MessageTemplate template) {
        return getComponentOwner().hasMessages(template);
    }
}