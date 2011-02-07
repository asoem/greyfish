package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.space.MovingObject2DInterface;
import org.asoem.greyfish.utils.DeepCloneable;

import java.awt.*;
import java.util.List;

public interface IndividualInterface extends DeepCloneable, Freezable, Iterable<GFComponent>, MovingObject2DInterface {
    Population getPopulation();
    void setPopulation(Population population);
    Body getBody();

    boolean addAction(GFAction action);
    boolean removeAction(GFAction action);
    void removeAllActions();
    List<GFAction> getActions();

    boolean addProperty(GFProperty property);
    boolean removeProperty(GFProperty property);
    void removeAllProperties();
    List<GFProperty> getProperties();

    boolean isCloneOf(Object object);

    Iterable<? extends GFComponent> getComponents();

    void changeActionExecutionOrder(GFAction object, GFAction object2);

    int getId();
    int getTimeOfBirth();
    Genome getGenome();
    void setGenome(Genome genome);

    Color getColor();
    void setColor(Color color);
    double getRadius();
    GFAction getLastExecutedAction();

    void execute();
}
