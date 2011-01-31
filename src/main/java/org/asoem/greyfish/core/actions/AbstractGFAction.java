package org.asoem.greyfish.core.actions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import net.sourceforge.jeval.EvaluationConstants;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import net.sourceforge.jeval.VariableResolver;
import net.sourceforge.jeval.function.FunctionException;
import org.asoem.greyfish.core.conditions.ConditionTree;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.individual.IndividualInterface;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.ContinuosProperty;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Root
public abstract class AbstractGFAction extends AbstractGFComponent implements GFAction {

    private final Evaluator FORMULA_EVALUATOR = new Evaluator(EvaluationConstants.SINGLE_QUOTE ,true,true,false,true);

    private ConditionTree conditionTree;

    @Element(name="costs_formula")
    private String energyCostsFormula = "0";

    @Element(name="energy_source", required=false)
    private DoubleProperty energySource;

    private int executionCount;

    private int timeOfLastExecution;

    @Override
    public boolean evaluate(Simulation simulation) {

        return !(energySource != null && energySource.getValue().compareTo(evaluateFormula()) < 0) && conditionTree.evaluate(simulation);

    }

    /**
     * Called by the individual to evaluate the condition if set and trigger the actions
     * @param simulation
     */
    @Override
    public final boolean execute(final Simulation simulation) {
        if( evaluate(simulation) ) {
            executeUnevaluated(simulation);
            return true;
        }
        else {
            return false;
        }
    }

    public void executeUnevaluated(final Simulation simulation) {
        performAction(simulation);
        ++executionCount;
        timeOfLastExecution = simulation.getSteps();

        if (energySource != null && done()) {
            energySource.subtract(evaluateFormula());
        }
    }

    /**
     * This is the actual actions
     * @param simulation
     */
    protected abstract void performAction(Simulation simulation);

    @Override
    public void setComponentRoot(IndividualInterface individual) {
        super.setComponentRoot(individual);
        conditionTree.setComponentRoot(componentOwner);
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
        try {
            return Double.valueOf(FORMULA_EVALUATOR.evaluate());
        }
        catch (EvaluationException e) {
            GreyfishLogger.warn("CostsFormula is not a valid expression", e);
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
    }

    @Override
    public int getExecutionCount() {
        return this.executionCount;
    }

    @Override
    public void export(Exporter e) {
        e.addField( new ValueAdaptor<String>("Energy Costs", String.class, energyCostsFormula) {
            @Override protected void writeThrough(String arg0) { energyCostsFormula = checkFrozen(arg0); }
        });
        //		e.addField( new ValueAdaptor<Boolean>("Is last?", Boolean.class, parameterLast)
        //				{ @Override protected void writeThrough(Boolean arg0) { parameterLast = arg0; }});
        e.addField(new ValueSelectionAdaptor<DoubleProperty>("Energy Source", DoubleProperty.class,
                energySource, componentOwner.getProperties(DoubleProperty.class)) {
            @Override protected void writeThrough(DoubleProperty arg0) { energySource = checkFrozen(arg0); }
        });
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) {
        super.checkConsistency(components);

        if (energySource != null) {
            checkState(Iterables.contains(components, energySource));
            checkState(energyCostsFormula != null);

            FORMULA_EVALUATOR.setVariableResolver( new VariableResolver() {

                @Override
                public String resolveVariable(final String arg0) throws FunctionException {
                    try {
                        ContinuosProperty<?> property = Iterables.find(componentOwner.getProperties(ContinuosProperty.class), new Predicate<ContinuosProperty>() {

                            @Override
                            public boolean apply(ContinuosProperty object) {
                                return object.getName().equals(arg0);
                            }
                        });
                        return String.valueOf(property.getAmount());
                    } catch(NoSuchElementException e) {
                        GreyfishLogger.warn(e);
                        return "0";
                    }
                }
            });

            try{
                FORMULA_EVALUATOR.parse(energyCostsFormula);
            } catch (Exception e) {
                throw new IllegalStateException("formula is not valid: " + energyCostsFormula);
            }
        }
    }

    public boolean wasNotExecutedForAtLeast(final Simulation simulation, final int steps) {
        // TODO: logical error: timeOfLastExecution = 0 does not mean, that it really did execute at 0
        return simulation.getSteps() - timeOfLastExecution >= steps;
    }

    @Override
    public boolean done() {
        return true;
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
        private String formula;

        public T executesIf(GFCondition condition) { this.condition = condition; return self(); }
        private T source(DoubleProperty source) { this.source = source; return self(); }
        private T formula(String formula) { this.formula = formula; return self(); }
        public T generatesCosts(DoubleProperty source, String formula) { return source(checkNotNull(source)).formula(checkNotNull(formula)); }
    }

    protected AbstractGFAction(AbstractGFAction cloneable, CloneMap map) {
        super(cloneable, map);
        this.conditionTree = new ConditionTree(map.clone(cloneable.getRootCondition(), GFCondition.class));
        this.energySource = map.clone(cloneable.energySource, DoubleProperty.class);
        this.energyCostsFormula = cloneable.energyCostsFormula;
    }
}