package org.asoem.greyfish.core.individual;


public interface IndividualCompositionListener {
	public void componentAdded(Agent source, GFComponent component);
	public void componentRemoved(Agent source, GFComponent component);
	public void componentChanged(Agent source, GFComponent component);
}
