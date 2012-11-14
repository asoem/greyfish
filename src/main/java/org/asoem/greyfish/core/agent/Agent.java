package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.Simulatable;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.Freezable;
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.collect.SearchableList;
import org.asoem.greyfish.utils.space.*;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Set;

public interface Agent<S extends Simulation<S, A, Z, P>, A extends Agent<S, A, Z, P>, Z extends Space2D<A, P>, P extends Object2D> extends DeepCloneable, Freezable, Simulatable<S,A,Z,P>, Moving<Motion2D>, Projectable<P>, AgentNode {

    void changeActionExecutionOrder(AgentAction object, AgentAction object2);

    @Nullable
    Population getPopulation();
    void setPopulation(@Nullable Population population);
    boolean hasPopulation(@Nullable Population population);

    boolean addAction(AgentAction<A, S, Z, P> action);

    boolean removeAction(AgentAction<A, S, Z, P> action);

    void removeAllActions();

    SearchableList<AgentAction<A, S, Z, P>> getActions();

    <T extends AgentAction<A, S, Z, P>> T getAction(String name, Class<T> clazz);

    boolean addProperty(AgentProperty property);

    boolean removeProperty(AgentProperty property);

    void removeAllProperties();

    SearchableList<AgentProperty<?>> getProperties();

    <T extends AgentProperty> T getProperty(String name, Class<T> clazz);

    AgentProperty<?> findProperty(Predicate<? super AgentProperty<?>> predicate);

    boolean addTrait(AgentTrait<?> gene);

    boolean removeGene(AgentTrait<?> gene);

    void removeAllGenes();

    SearchableList<AgentTrait<?>> getTraits();

    <T extends AgentTrait> T getTrait(String name, Class<T> clazz);

    AgentTrait<?> findTrait(Predicate<? super AgentTrait<?>> traitPredicate);

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

    void receive(AgentMessage message);

    void receiveAll(Iterable<? extends AgentMessage<A>> message);

    Iterable<AgentMessage<A>> getMessages(MessageTemplate template);

    boolean hasMessages(MessageTemplate template);

    void logEvent(Object eventOrigin, String title, String message);

    boolean didCollide();

    Set<Integer> getParents();

    int getSimulationStep();

    void reproduce(Initializer<? super A> initializer);

    Iterable<A> getAllAgents();

    Iterable<A> getAgents(Predicate<? super A> predicate);

    void die();
}
