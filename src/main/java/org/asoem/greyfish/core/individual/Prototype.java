package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.ListenerSupport;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.List;

@Root
public class Prototype extends GFAgentDecorator implements IndividualInterface {

    private final ListenerSupport<IndividualCompositionListener> listenerSupport = ListenerSupport.newInstance();

    private Prototype(Prototype prototype, CloneMap map) {
        super(map.clone(prototype.getDelegate(), IndividualInterface.class));
        for (GFComponent component : getComponents())
            component.setComponentRoot(this);
    }

    private Prototype(@Element(name="delegate") IndividualInterface delegate) {
        super(delegate);
        for (GFComponent component : getComponents())
            component.setComponentRoot(this);
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
    public void execute() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAction(GFAction action) {
        return addComponent(getDelegate().getActions(), action);
    }

    @Override
    public boolean removeAction(final GFAction action) {
        if (getDelegate().removeAction(action)) {
            action.setComponentRoot(null);
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
        return addComponent(getDelegate().getProperties(), property);
    }

    @Override
    public boolean removeProperty(final GFProperty property) {
        if (getDelegate().removeProperty(property)) {
            property.setComponentRoot(null);
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
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new Prototype(this, map);
    }

    public static Prototype newInstance(Individual individual) {
        return new Prototype(individual);
    }

    @Override
    public void setOrientation(double alpha) {
        throw new UnsupportedOperationException();
    }

    private <T extends NamedIndividualComponent> boolean addComponent(final List<T> collection, final T component) {
        checkComponentAddition(component);

        // duplicate check
        if (Iterables.find(collection, new Predicate<NamedIndividualComponent>() {

            @Override
            public boolean apply(NamedIndividualComponent object) {
                return object.getName().equals(component.getName());
            }
        }, null) != null)
            return false;

        collection.add(component);
        component.setComponentRoot(this);

        fireComponentAdded(component);

        if (GreyfishLogger.isTraceEnabled())
            GreyfishLogger.trace("Component " + component.getName() + " added to " + this);

        return true;
    }

    private boolean checkComponentAddition(final GFComponent component) {
        Preconditions.checkNotNull(component);

        if(component.getComponentOwner() != null
                && component.getComponentOwner() != this) {
            if (GreyfishLogger.isDebugEnabled())
                GreyfishLogger.debug("Component already part of another individual");
            return false;
        }

        return true;
    }

    public Individual getIndividual() {
        return Individual.class.cast(getDelegate());
    }

    @Override
    public String toString() {
        return "Prototype[" + getPopulation() + "]";
    }
}
