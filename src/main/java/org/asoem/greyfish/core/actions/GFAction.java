package org.asoem.sico.core.actions;

import org.asoem.sico.core.conditions.ConditionTree;
import org.asoem.sico.core.individual.NamedDeepCloneableIndividualComponent;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.utils.ConfigurableValueProvider;

public interface GFAction extends NamedDeepCloneableIndividualComponent, ConfigurableValueProvider {

	public boolean evaluate(final Simulation simulation);
	
	public boolean execute(final Simulation simulation);
	
	public void executeUnevaluated(final Simulation simulation);

	public ConditionTree getConditionTree();

	public int getExitValue();
	
	public double evaluateFormula();
	
	public int getExecutionCount();
	
//	public boolean isLast();
	
	public boolean wasNotExecutedForAtLeast(final Simulation simulation, final int steps);
	
	/*
	 * Check if the Action has completed its task.
	 */
	public boolean done();
}
