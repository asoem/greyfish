package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.Simulatable;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.Freezable;
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.collect.SearchableList;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Set;

public interface Agent<A extends Agent<A, S>, S extends Simulation<A>> extends DeepCloneable, Freezable, Simulatable<S, A>, AgentNode {

    void changeActionExecutionOrder(AgentAction<A> object, AgentAction<A> object2);

    Population getPopulation();

    void setPopulation(Population population);

    boolean hasPopulation(Population population);

    boolean addAction(AgentAction<A> action);

    boolean removeAction(AgentAction<A> action);

    void removeAllActions();

    SearchableList<AgentAction<A>> getActions();

    AgentAction<A> getAction(String name);

    boolean addProperty(AgentProperty<A, ?> property);

    boolean removeProperty(AgentProperty<A, ?> property);

    void removeAllProperties();

    SearchableList<AgentProperty<A, ?>> getProperties();

    AgentProperty<A, ?> getProperty(String name);

    AgentProperty<A, ?> findProperty(Predicate<? super AgentProperty<A, ?>> predicate);

    boolean addTrait(AgentTrait<A, ?> gene);

    boolean removeGene(AgentTrait<A, ?> gene);

    void removeAllGenes();

    SearchableList<AgentTrait<A, ?>> getTraits();

    AgentTrait<A, ?> getTrait(String name);

    AgentTrait<A, ?> findTrait(Predicate<? super AgentTrait<A, ?>> traitPredicate);

    /**
     * Update the agent's agentTraitList with the values of the {@link org.asoem.greyfish.core.genes.Gene}s in the given {@code vector}
     *
     * @param vector the vector containing the information for the update
     */
    void updateGeneComponents(Chromosome vector);

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

    void reproduce(Initializer<? super A> initializer);

    Iterable<A> getAllAgents();

    Iterable<A> filterAgents(Predicate<? super A> predicate);

    void die();

    void sendMessage(ACLMessage<A> message);
}
