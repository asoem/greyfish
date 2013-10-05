package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;

import java.util.List;

/**
 *
 */
public enum DefaultActionExecutionStrategyFactory implements ActionExecutionStrategyFactory {
    INSTANCE;

    @Override
    public ActionExecutionStrategy create(final List<? extends AgentAction<?>> actions) {
        return new DefaultActionExecutionStrategy(actions);
    }
}
