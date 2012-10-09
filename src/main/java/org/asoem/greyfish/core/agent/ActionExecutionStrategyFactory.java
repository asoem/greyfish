package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;

import java.util.List;

/**
 * User: christoph
 * Date: 09.10.12
 * Time: 13:06
 */
public interface ActionExecutionStrategyFactory {
    ActionExecutionStrategy createStrategy(List<? extends AgentAction> actions);
}
