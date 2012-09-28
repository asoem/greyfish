package org.asoem.greyfish.core.individual;

/**
 * User: christoph
 * Date: 28.09.12
 * Time: 11:29
 */
public interface ComponentAccessor<T extends AgentComponent> {
    T apply(Agent agent);
}
