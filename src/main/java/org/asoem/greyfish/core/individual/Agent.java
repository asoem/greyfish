package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.genes.IncompatibleGenomeException;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.Preparable;
import org.asoem.greyfish.utils.collect.TreeNode;
import org.asoem.greyfish.utils.space.MotionVector2D;
import org.asoem.greyfish.utils.space.Movable;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public interface Agent extends DeepCloneable, Freezable, Iterable<AgentComponent>, Movable, Preparable<Simulation> {
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
    Iterable<GFAction> getActions();
    @Nullable <T extends GFAction> T getAction(String name, Class<T> clazz);

    boolean addProperty(GFProperty property);
    boolean removeProperty(GFProperty property);
    void removeAllProperties();
    Iterable<GFProperty> getProperties();
    @Nullable <T extends GFProperty> T getProperty(String name, Class<T> clazz);

    boolean addGene(Gene<?> gene);
    boolean removeGene(Gene<?> gene);
    void removeAllGenes();
    Iterable<Gene<?>> getGenes();
    @Nullable <T extends Gene> T getGene(String name, Class<T> clazz);

    /**
     * Creates a new Genome with exact copies of this agents genes.
     * @return the new Genome
     */
    Genome<Gene<?>> createGamete();

    /**
     * Inject a copy of this agents genome so that this agents genes return the values of the genes in the given genome
     * @param genome a copy of this agents genome
     * @throws org.asoem.greyfish.core.genes.IncompatibleGenomeException
     * if {@code genome} is not a copy of this agents genome as defined by {@link Genome#isCompatibleGenome}
     */
    void injectGamete(Genome genome) throws IncompatibleGenomeException;

    Body getBody();
    Color getColor();
    void setColor(Color color);
    double getRadius();

    @Override
    MotionVector2D getMotionVector();

    void changeMotion(double angle, double velocity);
    void setMotion(double angle, double velocity);
    void setOrientation(double alpha);
    void setSpeed(double speed);

    Simulation getSimulation();
    public void setSimulation(Simulation simulation);
    int getId();
    int getTimeOfBirth();
    int getAge();
    GFAction getLastExecutedAction();

    void receive(AgentMessage message);
    void receiveAll(Iterable<? extends AgentMessage> message);
    List<AgentMessage> pullMessages(MessageTemplate template);
    boolean hasMessages(MessageTemplate template);

    void execute();
    void shutDown();

}
