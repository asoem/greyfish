package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.Simulatable;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.Freezable;
import org.asoem.greyfish.utils.collect.FunctionalList;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Set;

/**
 * An Agent which is the basic {@link Simulatable} unit of a {@link Simulation}.
 * @param <A> The type of the Agent implementation
 * @param <S> The type of the Simulation implementation
 */
public interface Agent<A extends Agent<A, S>, S extends Simulation<A>>
        extends DeepCloneable, Freezable, Simulatable<S, A>, AgentNode {

    void changeActionExecutionOrder(AgentAction<A> object, AgentAction<A> object2);

    /**
     * Get the population
     * @return the population
     */
    Population getPopulation();

    /**
     * Set the population
     * @param population the new population
     */
    void setPopulation(Population population);

    /**
     * Check if the population of this agent is equal to {@code population}.
     * @param population the population to test against
     * @return {@code true} if the populations are equal, {@code false} otherwise
     */
    boolean hasPopulation(Population population);

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

    @Nullable
    Color getColor();

    void setColor(Color color);

    boolean isActive();

    int getId();

    int getTimeOfBirth();

    int getAge();

    void receive(AgentMessage<A> message);

    void receiveAll(Iterable<? extends AgentMessage<A>> message);

    Iterable<AgentMessage<A>> getMessages(MessageTemplate template);

    boolean hasMessages(MessageTemplate template);

    void logEvent(Object eventOrigin, String title, String message);

    Set<Integer> getParents();

    int getSimulationStep();

    void reproduce(Chromosome chromosome);

    Iterable<A> getAllAgents();

    Iterable<A> filterAgents(Predicate<? super A> predicate);

    void die();

    void sendMessage(ACLMessage<A> message);

    void setParents(Set<Integer> parents);
}
