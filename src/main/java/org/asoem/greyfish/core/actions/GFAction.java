package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.actions.utils.ExecutionResult;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.simulation.Simulation;

import javax.annotation.Nullable;

public interface GFAction extends AgentComponent {

	public boolean evaluateCondition(Simulation simulation);
	
	public ExecutionResult execute(Simulation simulation);

	public double evaluateFormula(Simulation simulation);

    public ActionState getActionState();

    /**
     * @return the number of times this {@code GFAction} was executed when in {@link org.asoem.greyfish.core.actions.utils.ActionState#DORMANT}
     */
	public int getExecutionCount();
	
//	public boolean isLast();
	
	public boolean wasNotExecutedForAtLeast(final Simulation simulation, final int steps);

    public void setRootCondition(GFCondition rootCondition);

    @Nullable
    public GFCondition getRootCondition();

	/*
	 * Check if the Action has completed its task.
	 */
	public boolean isDormant();
}
