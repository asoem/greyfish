package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.actions.utils.ActionState;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 09.10.12
 * Time: 10:39
 */
public interface ActionExecutionStrategy {
    void execute();
    @Nullable
    AgentAction<?> lastExecutedAction();
    @Nullable
    ActionState lastExecutedActionState();

    void reset();
}
