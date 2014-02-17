package org.asoem.greyfish.core.utils;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.conditions.ActionCondition;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.traits.AgentTrait;


public interface AgentComponentClassFinder {

    Iterable<Class<? extends AgentAction>> getAvailableActions();

    Iterable<Class<? extends AgentProperty>> getAvailableProperties();

    Iterable<Class<? extends AgentTrait>> getAvailableGenes();

    Iterable<Class<? extends ActionCondition>> getAvailableConditionClasses();
}
