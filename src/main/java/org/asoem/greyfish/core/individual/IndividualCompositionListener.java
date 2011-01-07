package org.asoem.greyfish.core.individual;


public interface IndividualCompositionListener {
	public void componentAdded(Individual source, GFComponent component);
	public void componentRemoved(Individual source, GFComponent component);
	public void componentChanged(Individual source, GFComponent component);
}
