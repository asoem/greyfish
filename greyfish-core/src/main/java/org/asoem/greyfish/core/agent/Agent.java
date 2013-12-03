package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.collect.FunctionalList;

import java.util.Set;

/**
 * An Agent which is the basic unit of a {@link org.asoem.greyfish.core.simulation.DiscreteTimeSimulation}.
 * @param <A> The type of the Agent implementation
 * @param <S> The type of the Simulation implementation
 */
public interface Agent<A extends Agent<A, S>, S extends DiscreteTimeSimulation<A>>
        extends DeepCloneable, AgentNode, Runnable {

    /**
     * Get the population
     * @return the population
     */
    PrototypeGroup getPrototypeGroup();

    /**
     * Check if the prototypeGroup of this agent is equal to {@code prototypeGroup}.
     * @param prototypeGroup the prototypeGroup to test against
     * @return {@code true} if the populations are equal, {@code false} otherwise
     */
    boolean hasPopulation(PrototypeGroup prototypeGroup);

    /**
     * Add a new action to this agent.
     * @param action the action to add
     * @return {@code true} if the action was added, {@code false} otherwise
     */
    boolean addAction(AgentAction<A> action);

    boolean removeAction(AgentAction<A> action);

    void removeAllActions();

    FunctionalList<AgentAction<A>> getActions();

    AgentAction<A> getAction(String name);

    /**
     * Add a new property to this agent.
     * @param property the property to add
     * @return {@code true} if the property was added, {@code false} otherwise
     */
    boolean addProperty(AgentProperty<A, ?> property);

    boolean removeProperty(AgentProperty<A, ?> property);

    void removeAllProperties();

    FunctionalList<AgentProperty<A, ?>> getProperties();

    AgentProperty<A, ?> getProperty(String name);

    AgentProperty<A, ?> findProperty(Predicate<? super AgentProperty<A, ?>> predicate);

    /**
     * Add a new trait to this agent.
     * @param trait the trait to add
     * @return {@code true} if the trait was added, {@code false} otherwise
     */
    boolean addTrait(AgentTrait<A, ?> trait);

    boolean removeGene(AgentTrait<A, ?> gene);

    void removeAllGenes();

    FunctionalList<AgentTrait<A, ?>> getTraits();

    AgentTrait<A, ?> getTrait(String name);

    AgentTrait<A, ?> findTrait(Predicate<? super AgentTrait<A, ?>> traitPredicate);

    boolean isActive();

    int getId();

    long getTimeOfBirth();

    long getAge();

    void receive(ACLMessage<A> message);

    void receiveAll(Iterable<? extends ACLMessage<A>> message);

    Iterable<ACLMessage<A>> getMessages(MessageTemplate template);

    boolean hasMessages(MessageTemplate template);

    Set<Integer> getParents();

    long getSimulationStep();

    @Deprecated
    void reproduce(Chromosome chromosome);

    /**
     * Get all currently active agents from the {@link #simulation() getSimulation} in which this agent is active.
     * @return an iterable over all active agents returned by {@link #simulation()}
     * @throws IllegalStateException if this agent is not active in any getSimulation
     */
    Iterable<A> getAllAgents();

    Iterable<A> filterAgents(Predicate<? super A> predicate);

    void sendMessage(ACLMessage<A> message);

    void setParents(Set<Integer> parents);

    S simulation();

    void activate(SimulationContext<S, A> context);

    /**
     * Let the agent execute it's next action
     */
    @Override
    void run();

    void deactivate();
}
