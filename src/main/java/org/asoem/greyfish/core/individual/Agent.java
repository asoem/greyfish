package org.asoem.greyfish.core.individual;

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.GeneComponentList;
import org.asoem.greyfish.core.properties.GFProperty;
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

    void changeActionExecutionOrder(GFAction object, GFAction object2);

    Population getPopulation();
    void setPopulation(Population population);
    boolean hasPopulation(Population population);

    boolean addAction(GFAction action);

    boolean removeAction(GFAction action);

    void removeAllActions();

    ComponentList<GFAction> getActions();

    <T extends GFAction> T getAction(String name, Class<T> clazz);

    boolean addProperty(GFProperty property);

    boolean removeProperty(GFProperty property);

    void removeAllProperties();

    ComponentList<GFProperty<?>> getProperties();

    <T extends GFProperty> T getProperty(String name, Class<T> clazz);

    GFProperty<?> findProperty(Predicate<? super GFProperty<?>> predicate);

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

    GFAction getLastExecutedAction();

    void receive(AgentMessage message);

    void receiveAll(Iterable<? extends AgentMessage> message);

    Iterable<AgentMessage> getMessages(MessageTemplate template);

    boolean hasMessages(MessageTemplate template);

    void logEvent(Object eventOrigin, String title, String message);

    boolean didCollide();
}
