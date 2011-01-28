package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.interfaces.GFInterface;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.utils.DeepClonable;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by IntelliJ IDEA.
 * User: christoph
 * Date: 28.01.11
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
public interface IndividualInterface extends DeepClonable, Freezable, Iterable<GFComponent> {
    Population getPopulation();

    void setPopulation(Population population);

    boolean addAction(GFAction action);

    boolean removeAction(GFAction action);

    void removeAllActions();

    List<GFAction> getActions();

    <T extends GFAction> T getAction(Class<T> t, String actionName);

    <T extends GFAction> Iterable<GFAction> getActions(
            Class<T> class1);

    boolean addProperty(GFProperty property);

    boolean hasProperty(String name);

    boolean hasAction(String name);

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
}
