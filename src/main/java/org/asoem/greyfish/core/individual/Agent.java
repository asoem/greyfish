package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.genes.ImmutableGenome;
import org.asoem.greyfish.core.io.AgentLog;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.Preparable;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public interface Agent extends DeepCloneable, Freezable, Iterable<GFComponent>, MovingObject2D, MessageReceiver, Preparable<Simulation> {
    Population getPopulation();
    void setPopulation(Population population);
    Body getBody();

    boolean addAction(GFAction action);
    boolean removeAction(GFAction action);
    void removeAllActions();
    Iterable<GFAction> getActions();
    @Nullable <T extends GFAction> T getAction(String actionName, Class<T> gfActionClass);

    boolean addProperty(GFProperty property);
    boolean removeProperty(GFProperty property);
    void removeAllProperties();
    Iterable<GFProperty> getProperties();
    @Nullable <T extends GFProperty> T getProperty(String name, Class<T> propertyClass);

    Genome getGenome();
    void setGenome(Genome genome);
    @Nullable <T extends Gene> T getGene(String geneName, Class<T> geneClass);

    boolean isCloneOf(Object object);

    Iterable<GFComponent> getComponents();

    void changeActionExecutionOrder(GFAction object, GFAction object2);

    int getId();
    int getTimeOfBirth();
    int getAge();

    Color getColor();
    void setColor(Color color);
    double getRadius();
    GFAction getLastExecutedAction();

    void execute();

    void sendMessage(ACLMessage message);
    List<ACLMessage> pullMessages(MessageTemplate template);
    boolean hasMessages(MessageTemplate template);

    AgentLog getLog();

    Iterable<MovingObject2D> findNeighbours(double range);

    void shutDown();

    Simulation getSimulation();
    public void setSimulation(Simulation simulation);
}
