package org.asoem.greyfish.core.agent;

/**
 * User: christoph
 * Date: 28.09.12
 * Time: 11:29
 */
public interface ComponentAccessor<A extends Agent<A, ?>, T extends AgentComponent<A>> {
    T apply(A agent) throws ClassCastException;
}
