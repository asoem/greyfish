package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.agent.Agent;

public interface ExecutionContext<A extends Agent<A, ?>> {
    A agent();
}
