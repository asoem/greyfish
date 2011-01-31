package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.interfaces.GFInterface;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepClonable;
import org.asoem.greyfish.utils.ListenerSupport;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Prototype extends GFAgentDecorator implements IndividualInterface {

    private Prototype(Prototype prototype, CloneMap map) {
        super(map.clone(prototype.getDelegate(), IndividualInterface.class));
    }

    public Prototype(IndividualInterface individual) {
        super(individual);
    }

    @Override
    public boolean addAction(GFAction action) {
        if (getDelegate().addAction(action)) {
            fireComponentAdded(action);
            return true;
        }
        return false;
    }

    private final ListenerSupport<IndividualCompositionListener> listenerSupport = new ListenerSupport<IndividualCompositionListener>();

    public Prototype(Individual delegate) {
        super(delegate);
    }


    public void addCompositionListener(
            IndividualCompositionListener individualCompositionListener) {
        listenerSupport.addListener(individualCompositionListener);
    }

    public void removeCompositionListener(
            IndividualCompositionListener individualCompositionListener) {
        listenerSupport.removeListener(individualCompositionListener);
    }

    private void fireComponentAdded(final GFComponent component) {
        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {

            @Override
            public void update(IndividualCompositionListener listener) {
                listener.componentAdded(getDelegate(), component);
            }
        });
    }

    private void fireComponentRemoved(final GFComponent component) {
        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {

            @Override
            public void update(IndividualCompositionListener listener) {
                listener.componentRemoved(getDelegate(), component);
            }
        });
    }

    @Override
    public void changeActionExecutionOrder(final GFAction object, final GFAction object2) {
        getDelegate().changeActionExecutionOrder(object, object2);
        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {

            @Override
            public void update(IndividualCompositionListener listener) {
                listener.componentChanged(getDelegate(), object);
            }
        });
    }

    @Override
    public boolean removeAction(GFAction action) {
        if (getDelegate().removeAction(action)) {
            fireComponentRemoved(action);
            return true;
        }
        return false;
    }

    @Override
    public void removeAllActions() {
        for (GFAction action : getActions())
        removeAction(action);
    }

    @Override
    public boolean addProperty(GFProperty property) {
        if (getDelegate().addProperty(property)) {
            fireComponentAdded(property);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeProperty(GFProperty property) {
        if (getDelegate().removeProperty(property)) {
            fireComponentRemoved(property);
            return true;
        }
        return false;
    }

    @Override
    public void removeAllProperties() {
        for (GFProperty property : getProperties()) {
            removeProperty(property);
        }
    }

    @Override
    public DeepClonable deepCloneHelper(CloneMap map) {
        return new Prototype(this, map);
    }

    public static Prototype newInstance(IndividualInterface individual) {
        return new Prototype(individual);
    }
}
