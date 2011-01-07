package org.asoem.greyfish.core.simulation;

import java.util.EventListener;

import org.asoem.greyfish.core.actions.GFAction;

public interface ActionGotExecutedListener extends EventListener {

	public void actionGotExecuted(GFAction action);
}
