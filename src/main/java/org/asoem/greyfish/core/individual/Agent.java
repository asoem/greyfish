package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulatable2D;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.Freezable;
import org.asoem.greyfish.utils.collect.TreeNode;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public interface Agent extends DeepCloneable, Freezable, Simulatable2D {
    /**
     * @param object a possible clone
     * @return {@code true} if object is a clone of this agent, {@code false} otherwise
     */
    boolean isCloneOf(Object object);

    TreeNode<AgentComponent> getRootComponent();

    Iterable<AgentComponent> getComponents();
    void changeActionExecutionOrder(GFAction object, GFAction object2);

    Population getPopulation();
    void setPopulation(Population population);

    boolean addAction(GFAction action);
    boolean removeAction(GFAction action);
    void removeAllActions();
    ComponentList<GFAction> getActions();
    @Nullable <T extends GFAction> T getAction(String name, Class<T> clazz);

    boolean addProperty(GFProperty property);
    boolean removeProperty(GFProperty property);
    void removeAllProperties();
    ComponentList<GFProperty> getProperties();
    @Nullable <T extends GFProperty> T getProperty(String name, Class<T> clazz);

    boolean addGene(Gene<?> gene);
    boolean removeGene(Gene<?> gene);
    void removeAllGenes();
    Chromosome<Gene<?>> getChromosome();
    @Nullable <T extends Gene> T getGene(String name, Class<T> clazz);

    /**
     * Inject a copy of this agents chromosome so that this agents genes return the values of the genes in the given chromosome
     *
     *
     * @param chromosome a copy of this agents chromosome
     * @throws org.asoem.greyfish.core.genes.IncompatibleGenomeException
     * if {@code chromosome} is not a copy of this agents chromosome as defined by {@link org.asoem.greyfish.core.genes.Chromosome#isCompatibleGenome}
     */
    void injectGamete(Chromosome<? extends Gene<?>> chromosome);
    void initGenome();

    Body getBody();
    Color getColor();
    void setColor(Color color);

    SimulationContext getSimulationContext();
    public void setSimulationContext(SimulationContext context);
    int getId();
    int getTimeOfBirth();
    int getAge();
    GFAction getLastExecutedAction();

    void receive(AgentMessage message);
    void receiveAll(Iterable<? extends AgentMessage> message);
    List<AgentMessage> pullMessages(MessageTemplate template);
    boolean hasMessages(MessageTemplate template);

}
