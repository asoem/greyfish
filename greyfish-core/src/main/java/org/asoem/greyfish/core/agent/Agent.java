package org.asoem.greyfish.core.agent;

import com.google.common.base.Optional;

/**
 * An Agent which is the basic unit of a {@link org.asoem.greyfish.core.simulation.DiscreteTimeEnvironment}.
 *
 * @param <C> The type of the simulation context
 */
public interface Agent<C extends SimulationContext<?, ?>>
        extends AgentNode, Runnable {

    /**
     * Get the population
     *
     * @return the population
     */
    PrototypeGroup getPrototypeGroup();


    /**
     * Let the agent execute it's next action
     */
    @Override
    void run();

    /*
     * Activate this agent and set the current context to {@code context}.
     *
     * @param context the new context for this agent
     */
    void activate(C context);

    /**
     * Deactivate this agent. <p>Deactivation will remove the current {@link BasicSimulationContext context}</p>
     */
    void deactivate();

    /**
     * Check if the agent's {@link BasicSimulationContext context} is present.
     *
     * @return {@code true} if the context is present, {@code false} if absent
     */
    boolean isActive();

    /*
     * Get the simulation context holder for this agent.
     *
     * @return the optional simulation context
     */
    Optional<C> getContext();

    /**
     * Send a message to this agent.
     *
     * @param message   the message
     * @param replyType the class to cast the reply to
     * @return the reply to given {@code message}
     * @throws java.lang.IllegalArgumentException if given {@code message} could not be handled
     */
    <T> T ask(Object message, Class<T> replyType);

    /**
     * Get the value for trait named {@code traitName}.
     *
     * @param traitName the name of the trait
     * @param valueType the class of the trait value
     * @param <T>       the type of the trait value class
     * @return the value of the trait
     * @throws java.lang.IllegalStateException if no trait with name equal to {@code traitName} could be found.
     */
    <T> T getPropertyValue(String traitName, Class<T> valueType);

}
