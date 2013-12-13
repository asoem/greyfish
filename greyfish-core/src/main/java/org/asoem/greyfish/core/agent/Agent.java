package org.asoem.greyfish.core.agent;

import com.google.common.base.Optional;
import org.asoem.greyfish.core.acl.MessageConsumer;
import org.asoem.greyfish.core.acl.MessageProducer;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.utils.collect.FunctionalList;

/**
 * An Agent which is the basic unit of a {@link org.asoem.greyfish.core.simulation.DiscreteTimeSimulation}.
 *
 * @param <A> The actual type of this agent
 * @param <C> The type of the simulation context
 */
public interface Agent<A extends Agent<A, C>, C extends SimulationContext<?>>
        extends AgentNode, Runnable, MessageConsumer<A>, MessageProducer<A> {

    /**
     * Get the population
     *
     * @return the population
     */
    PrototypeGroup getPrototypeGroup();

    /**
     * Get all actions of this agent
     *
     * @return the actions of this agent
     */
    FunctionalList<AgentAction<A>> getActions();

    /**
     * Get the action with it's {@link AgentComponent#getName() name} equal to {@code name}
     *
     * @param name the name of the action to get
     * @return the action of agent with it's name equal to {@code name}
     * @throws java.util.NoSuchElementException if no such action exists
     */
    AgentAction<A> getAction(String name);

    /**
     * Get all properties of this agent
     *
     * @return the properties of this agent
     */
    FunctionalList<AgentProperty<A, ?>> getProperties();

    /**
     * Get the property with it's {@link AgentComponent#getName() name} equal to {@code name}
     *
     * @param name the name of the property to get
     * @return the property of agent with it's name equal to {@code name}
     * @throws java.util.NoSuchElementException if no such property exists
     */
    AgentProperty<A, ?> getProperty(String name);

    /**
     * Get all traits of this agent
     *
     * @return the traits of this agent
     */
    FunctionalList<AgentTrait<A, ?>> getTraits();

    /**
     * Get the trait with it's {@link AgentComponent#getName() name} equal to {@code name}
     *
     * @param name the name of the trait to get
     * @return the trait of agent with it's name equal to {@code name}
     * @throws java.util.NoSuchElementException if no such trait exists
     */
    AgentTrait<A, ?> getTrait(String name);


    /**
     * Let the agent execute it's next action
     */
    @Override
    void run();

    /**
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

    /**
     * Get the simulation context holder for this agent.
     *
     * @return the optional simulation context
     */
    Optional<C> getContext();
}
