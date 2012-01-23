package org.asoem.greyfish.core.utils;

import org.asoem.greyfish.core.individual.AgentComponent;

/**
 * User: christoph
 * Date: 23.01.12
 * Time: 13:20
 */
public interface AgentComponentFactory {
    <T extends AgentComponent> T createComponent(Class<T> componentClass);
}
