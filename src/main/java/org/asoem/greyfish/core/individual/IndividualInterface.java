package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.interfaces.GFInterface;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.MovingObject2DInterface;
import org.asoem.greyfish.utils.DeepCloneable;

import java.awt.*;
import java.util.List;
import java.util.NoSuchElementException;

public interface IndividualInterface extends DeepCloneable, Freezable, Iterable<GFComponent>, MovingObject2DInterface {
    Population getPopulation();

    void setPopulation(Population population);

    boolean addAction(GFAction action);
    boolean hasAction(String name);
    boolean removeAction(GFAction action);
    void removeAllActions();
    List<GFAction> getActions();
    <T extends GFAction> T getAction(Class<T> t, String actionName);
    <T extends GFAction> Iterable<GFAction> getActions(Class<T> class1);

    boolean addProperty(GFProperty property);
    boolean hasProperty(String name);
    boolean removeProperty(GFProperty property);
    void removeAllProperties();
    List<GFProperty> getProperties();
    <T extends GFProperty> Iterable<T> getProperties(Class<T> clazz);

    boolean isCloneOf(Object object);

    String getName();

    // TODO: Should better return a Component Graph
    Iterable<? extends GFComponent> getComponents();

    <T extends GFInterface> T getInterface(Class<T> clazz) throws NoSuchElementException;

    void changeActionExecutionOrder(GFAction object, GFAction object2);

    int getId();

    int getTimeOfBirth();

    Color getColor();
    void setColor(Color color);
    
    Genome getGenome();
    void setGenome(Genome genome);

    double getRadius();
    
    GFAction getLastExecutedAction();

    void execute();


}
