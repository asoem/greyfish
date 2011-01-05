package org.asoem.sico.core.simulation;

import java.util.EventListener;

import org.asoem.sico.core.actions.GFAction;

public interface ActionGotExecutedListener extends EventListener {

	public void actionGotExecuted(GFAction action);
}
