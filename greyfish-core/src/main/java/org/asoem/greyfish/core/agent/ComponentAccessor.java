package org.asoem.greyfish.core.agent;

/**
 * {@code ComponentAccessor}s are helper objects to access repeated read accesses to {@code AgentComponent}s faster
 * than using {@code Agent#getXXX(String)} by creating a {@link com.google.common.base.Predicate} and delegating
 * accesses to {@code Agent#getXXX(com.google.common.base.Predicate)}.
 */
public interface ComponentAccessor<A extends Agent<A, ?>, T extends AgentComponent<A>> {
    /**
     * Get the defined component from {@code agent}.
     * @param agent the agent to find the component in
     * @return the component
     */
    T apply(A agent);
}
