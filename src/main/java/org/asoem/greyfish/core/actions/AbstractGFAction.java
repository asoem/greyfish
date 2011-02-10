package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.conditions.ConditionTree;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.IndividualInterface;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.GreyfishMathExpression;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.core.io.GreyfishLogger.debug;
import static org.asoem.greyfish.core.io.GreyfishLogger.isDebugEnabled;

@Root
public abstract class AbstractGFAction extends AbstractGFComponent implements GFAction {

    private ConditionTree conditionTree;

    @Element(name="costs_formula", required = false)
    private String energyCostsFormula = "0";

    @Element(name="energy_source", required=false)
    private DoubleProperty energySource;

    private int executionCount;

    private int timeOfLastExecution;

    @Override
    public boolean evaluate(Simulation simulation) {
        if (!conditionTree.evaluate(simulation))
            return false;

        // test for energy
        double needed = evaluateFormula();
        if (energySource != null && energySource.get().compareTo(needed) < 0) {
            if (isDebugEnabled())
                debug("Evaluation of " + this + " evaluated to false for energy reasons. " +
                        "Needed=" + needed + "; available=" + energySource.get());
            return false;
        }

        return true;
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
        return GreyfishMathExpression.evaluate(energyCostsFormula, getComponentOwner());
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
        e.addField( new ValueAdaptor<String>("Energy Costs", String.class, energyCostsFormula) {
            @Override protected void writeThrough(String arg0) { energyCostsFormula = checkFrozen(arg0); }
        });
        //		e.addField( new ValueAdaptor<Boolean>("Is last?", Boolean.class, parameterLast)
        //				{ @Override protected void writeThrough(Boolean arg0) { parameterLast = arg0; }});
        e.addField(new ValueSelectionAdaptor<DoubleProperty>("Energy Source", DoubleProperty.class,
                energySource, Iterables.filter(getComponentOwner().getProperties(), DoubleProperty.class)) {
            @Override protected void writeThrough(DoubleProperty arg0) { energySource = checkFrozen(arg0); }
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
}