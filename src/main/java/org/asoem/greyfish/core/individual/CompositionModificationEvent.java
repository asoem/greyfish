package org.asoem.greyfish.core.individual;

import javax.swing.event.ChangeEvent;

public class CompositionModificationEvent extends ChangeEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3275965658823202971L;
	public static final int COMPONENT_ADDED = 0;
	public static final int COMPONENT_REMOVED = 0;

	public CompositionModificationEvent(Object source, AgentComponent component) {
		super(source);
	}

}
