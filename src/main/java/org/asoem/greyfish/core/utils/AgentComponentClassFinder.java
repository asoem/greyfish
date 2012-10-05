package org.asoem.greyfish.core.utils;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.conditions.GFCondition;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.properties.GFProperty;

/**
 * User: christoph
 * Date: 11.01.12
 * Time: 12:02
 */
public interface AgentComponentClassFinder {
    
    Iterable<Class<? extends GFAction>> getAvailableActions();
    Iterable<Class<? extends GFProperty>> getAvailableProperties();
    Iterable<Class<? extends AgentTrait>> getAvailableGenes();
    Iterable<Class<? extends GFCondition>> getAvailableConditionClasses();
}
