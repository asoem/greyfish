package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.actions.GFAction;

import java.util.EventListener;

public interface ActionGotExecutedListener extends EventListener {

	public void actionGotExecuted(GFAction action);
}
