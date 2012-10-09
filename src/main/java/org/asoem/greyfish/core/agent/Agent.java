package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.GeneComponentList;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.Simulatable2D;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.Freezable;

import java.awt.*;

public interface Agent extends DeepCloneable, Freezable, Simulatable2D, AgentNode<AgentComponent> {
    /**
     * @param object a possible clone
     * @return {@code true} if object is a clone of this agent, {@code false} otherwise
     */
    boolean isCloneOf(Object object);

    Iterable<AgentComponent> getComponents();

    AgentComponent getComponent(String name);

    void changeActionExecutionOrder(AgentAction object, AgentAction object2);

    Population getPopulation();
    void setPopulation(Population population);
    boolean hasPopulation(Population population);

    boolean addAction(AgentAction action);

    boolean removeAction(AgentAction action);

    void removeAllActions();

    ComponentList<AgentAction> getActions();

    <T extends AgentAction> T getAction(String name, Class<T> clazz);

    boolean addProperty(AgentProperty property);

    boolean removeProperty(AgentProperty property);

    void removeAllProperties();

    ComponentList<AgentProperty<?>> getProperties();

    <T extends AgentProperty> T getProperty(String name, Class<T> clazz);

    AgentProperty<?> findProperty(Predicate<? super AgentProperty<?>> predicate);

    boolean addGene(AgentTrait<?> gene);

    boolean removeGene(AgentTrait<?> gene);

    void removeAllGenes();

    GeneComponentList<AgentTrait<?>> getTraits();

    <T extends AgentTrait> T getGene(String name, Class<T> clazz);

    AgentTrait<?> findTrait(Predicate<? super AgentTrait<?>> traitPredicate);

    /**
     * Update the agent's agentTraitList with the values of the {@link org.asoem.greyfish.core.genes.Gene}s in the given {@code vector}
     *
     * @param vector the vector containing the information for the update
     */
    void updateGeneComponents(Chromosome vector);

    Body getBody();

    Color getColor();

    void setColor(Color color);

    boolean isActive();

    Simulation simulation();

    int getId();

    int getTimeOfBirth();

    int getAge();

    void receive(AgentMessage message);

    void receiveAll(Iterable<? extends AgentMessage> message);

    Iterable<AgentMessage> getMessages(MessageTemplate template);

    boolean hasMessages(MessageTemplate template);

    void logEvent(Object eventOrigin, String title, String message);

    boolean didCollide();
}
