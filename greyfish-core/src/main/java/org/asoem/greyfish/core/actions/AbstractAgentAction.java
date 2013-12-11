package org.asoem.greyfish.core.actions;

import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;

import javax.annotation.Nullable;

public abstract class AbstractAgentAction<A extends Agent<A, ?>>
extends AbstractAgentComponent<A> implements AgentAction<A> {
    private ActionState state = ActionState.INITIAL;

    public AbstractAgentAction(final String name) {
        super(name);
    }

    @Nullable
    @Override
    public final AgentNode parent() {
        return null;
    }

    @Override
    public final Iterable<AgentNode> children() {
        return ImmutableList.of();
    }

    private ActionState setState(final ActionState newState) {
        this.state = newState;
        return this.state;
    }
}
