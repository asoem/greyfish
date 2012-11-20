package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.utils.collect.SearchableList;

import java.util.List;

/**
 * User: christoph
 * Date: 09.10.12
 * Time: 13:06
 */
public interface AgentInitializationFactory {
    ActionExecutionStrategy createStrategy(List<? extends AgentAction<?>> actions);
    <T extends AgentComponent> SearchableList<T> newSearchableList(Iterable<T> elements);
    <A extends Agent> AgentMessageBox<A> createMessageBox();
}
