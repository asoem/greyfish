package org.asoem.greyfish.core.individual;


public interface IndividualCompositionListener {
	public void componentAdded(Agent source, AgentComponent component);
	public void componentRemoved(Agent source, AgentComponent component);
	public void componentChanged(Agent source, AgentComponent component);
}
