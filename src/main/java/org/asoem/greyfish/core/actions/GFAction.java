package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.conditions.ConditionTree;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.individual.NamedDeepCloneableIndividualComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.ConfigurableValueProvider;

public interface GFAction extends NamedDeepCloneableIndividualComponent, ConfigurableValueProvider {

	public boolean evaluate(final Simulation simulation);
	
	public boolean execute(final Simulation simulation);
	
	public void executeUnevaluated(final Simulation simulation);

	public ConditionTree getConditionTree();
	
	public double evaluateFormula();
	
	public int getExecutionCount();
	
//	public boolean isLast();
	
	public boolean wasNotExecutedForAtLeast(final Simulation simulation, final int steps);

    public void setRootCondition(GFCondition rootCondition);
    public GFCondition getRootCondition();

	/*
	 * Check if the Action has completed its task.
	 */
	public boolean done();
}
