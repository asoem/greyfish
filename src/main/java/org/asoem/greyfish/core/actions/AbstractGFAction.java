package org.asoem.greyfish.core.actions;

import java.util.Map;
import java.util.NoSuchElementException;

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
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.ContinuosProperty;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@Root
public abstract class AbstractGFAction extends AbstractGFComponent implements GFAction {

	private static final Evaluator FORMULA_EVALUATOR = new Evaluator(EvaluationConstants.SINGLE_QUOTE ,true,true,false,true);

	private final ConditionTree conditionTree = new ConditionTree();

	@Attribute(name="level")
	private int parameterExecutionLevel = -1;

	@Element(name="costs_formula")
	private String energyCostsFormula = "0";
	private double energyCosts;

	@Element(name="energy_source", required=false)
	private DoubleProperty energySource;

	private int exitValue;

	private int executionCount;

	private int lastExecutionStep;

	//	@Attribute(name="last", required=false)
	//	private boolean parameterLast = true;

	public AbstractGFAction() {
		initFields();
	}

	public AbstractGFAction(String name) {
		super(name);
		initFields();
	}

	public AbstractGFAction(AbstractGFAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);

		conditionTree.setRootCondition(deepClone(action.getRootCondition(), mapDict));
		//		parameterLast = action.parameterLast;
		parameterExecutionLevel = action.parameterExecutionLevel;
		energyCostsFormula = action.energyCostsFormula;
		energySource = deepClone(action.energySource, mapDict);

		initFields();
	}

	private void initFields() {
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
	}

	@Override
	public boolean evaluate(Simulation simulation) {

		if (energySource != null) {
			if (energySource.getValue().compareTo(energyCosts) < 0 )
				return false;		
		}

		GFCondition rootCondition = conditionTree.getRootCondition();	
		return (rootCondition == null) ? true : rootCondition.evaluate(simulation);
	}

	/**
	 * Called by the individual to evaluate the condition if set and trigger the action
	 * @param simulation
	 * @param componentOwner
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
		lastExecutionStep = simulation.getSteps();

		if (energySource != null && done()) {
			energySource.substract(energyCosts);
		}
	}

	/**
	 * This is the actual action
	 * @param simulation 
	 * @param componentOwner
	 */
	protected abstract void performAction(Simulation simulation);

	@Override
	public void setComponentOwner(Individual individual) {
		super.setComponentOwner(individual);
		conditionTree.setComponentOwner(componentOwner);
	}

	@Override
	public int getExitValue() {
		return exitValue;
	}

	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
		if (conditionTree.hasRootCondition())
			conditionTree.getRootCondition().initialize(simulation);
		executionCount = 0;
		lastExecutionStep = simulation.getSteps();
		energyCosts = evaluateFormula();
	}

	@Override
	public ConditionTree getConditionTree() {
		return conditionTree;
	}

	@Override
	public double evaluateFormula() {
		try {
			return FORMULA_EVALUATOR.getNumberResult(energyCostsFormula);
		}
		catch (EvaluationException e) {
			GreyfishLogger.warn("CostsFormula is not a valid expression", e);
			return 0;
		}
	}

	public void setParameterCostsFormula(String parameterCostsFormula) {
		this.energyCostsFormula = parameterCostsFormula;
	}

	public String getParameterCostsFormula() {
		return energyCostsFormula;
	}

	@Element(name="condition", required=false)
	public GFCondition getRootCondition() {
		return conditionTree.getRootCondition();
	}

	@Element(name="condition", required=false)
	public void setRootCondition(GFCondition rootCondition) {
		conditionTree.setRootCondition(rootCondition);
	}

	@Override
	public int getExecutionCount() {
		return this.executionCount;
	}

	@Override
	public void export(Exporter e) {
		e.addField( new ValueAdaptor<String>("Energy Costs", String.class, energyCostsFormula)
				{ @Override protected void writeThrough(String arg0) { energyCostsFormula = arg0; }});
		//		e.addField( new ValueAdaptor<Boolean>("Is last?", Boolean.class, parameterLast)
		//				{ @Override protected void writeThrough(Boolean arg0) { parameterLast = arg0; }});
		e.addField(new ValueSelectionAdaptor<DoubleProperty>("Energy Source", DoubleProperty.class, energySource, componentOwner.getProperties(DoubleProperty.class)) {

			@Override
			protected void writeThrough(DoubleProperty arg0) {
				energySource = arg0;
			}
		});
	}

	@Override
	public void checkDependencies(Iterable<? extends GFComponent> components) {
		super.checkDependencies(components);
		if (! Iterables.contains(components, energySource))
			energySource = null;
	}

	//	@Override
	//	public boolean isLast() {
	//		return parameterLast;
	//	}

	public boolean wasNotExecutedForAtLeast(final Simulation simulation, final int steps) {
		// TODO: logical error: lastExecutionStep = 0 does not mean, that it really did execute at 0
		return simulation.getSteps() - lastExecutionStep >= steps;
	}

	@Override
	public boolean done() {
		return true;
	}
}
