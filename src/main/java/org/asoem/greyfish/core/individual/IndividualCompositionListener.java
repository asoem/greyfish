package org.asoem.greyfish.core.individual;


public interface IndividualCompositionListener {
	public void componentAdded(IndividualInterface source, GFComponent component);
	public void componentRemoved(IndividualInterface source, GFComponent component);
	public void componentChanged(IndividualInterface source, GFComponent component);
}
