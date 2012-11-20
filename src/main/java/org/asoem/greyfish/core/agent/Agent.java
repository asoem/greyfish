package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
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
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Moving;
import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Projectable;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Set;

public interface Agent<S extends Simulation<S, A, ?, P>, A extends Agent<S, A, P>, P extends Object2D> extends DeepCloneable, Freezable, Simulatable<S, A>, Moving<Motion2D>, Projectable<P>, AgentNode {

    void changeActionExecutionOrder(AgentAction object, AgentAction object2);

    @Nullable
    Population getPopulation();
    void setPopulation(@Nullable Population population);
    boolean hasPopulation(@Nullable Population population);

    boolean addAction(AgentAction<A> action);

    boolean removeAction(AgentAction<A> action);

    void removeAllActions();

    SearchableList<AgentAction<A>> getActions();

    <T extends AgentAction<A>> T getAction(String name, Class<T> clazz);

    boolean addProperty(AgentProperty<?, A> property);

    boolean removeProperty(AgentProperty<?, A> property);

    void removeAllProperties();

    SearchableList<AgentProperty<?,A>> getProperties();

    <T extends AgentProperty<?,A>> T getProperty(String name, Class<T> clazz);

    AgentProperty<?,A> findProperty(Predicate<? super AgentProperty<?,A>> predicate);

    boolean addTrait(AgentTrait<?, A> gene);

    boolean removeGene(AgentTrait<?, A> gene);

    void removeAllGenes();

    SearchableList<AgentTrait<?, A>> getTraits();

    <T extends AgentTrait> T getTrait(String name, Class<T> clazz);

    AgentTrait<?, A> findTrait(Predicate<? super AgentTrait<?, A>> traitPredicate);

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

    boolean didCollide();

    Set<Integer> getParents();

    int getSimulationStep();

    void reproduce(Initializer<? super A> initializer);

    Iterable<A> getAllAgents();

    Iterable<A> getAgents(Predicate<? super A> predicate);

    void die();
}
