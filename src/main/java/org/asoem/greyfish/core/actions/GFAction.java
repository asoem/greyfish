package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.NamedDeepCloneableIndividualComponent;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;

public interface GFAction extends AgentComponent, NamedDeepCloneableIndividualComponent {

	public boolean evaluateConditions(ParallelizedSimulation simulation);
	
	public AbstractGFAction.ExecutionResult execute(ParallelizedSimulation simulation);

	public double evaluateFormula(ParallelizedSimulation simulation);
	
	public int getExecutionCount();
	
//	public boolean isLast();
	
	public boolean wasNotExecutedForAtLeast(final ParallelizedSimulation simulation, final int steps);

    public void setRootCondition(GFCondition rootCondition);

    public GFCondition getRootCondition();

	/*
	 * Check if the Action has completed its task.
	 */
	public boolean isDormant();
}
