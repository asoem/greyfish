package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.conditions.ConditionTree;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.NamedDeepCloneableIndividualComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.ConfigurableObject;

public interface GFAction extends GFComponent, NamedDeepCloneableIndividualComponent, ConfigurableObject {

	public boolean evaluateConditions(final ActionContext context);
	
	public AbstractGFAction.ExecutionResult execute(final ActionContext context);

	public ConditionTree getConditionTree();
	
	public double evaluateFormula(ActionContext context);
	
	public int getExecutionCount();
	
//	public boolean isLast();
	
	public boolean wasNotExecutedForAtLeast(final Simulation simulation, final int steps);

    public void setRootCondition(GFCondition rootCondition);
    public GFCondition getRootCondition();

	/*
	 * Check if the Action has completed its task.
	 */
	public boolean isDormant();
}
