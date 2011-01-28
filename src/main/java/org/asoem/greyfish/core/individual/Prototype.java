package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.interfaces.GFInterface;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.ListenerSupport;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Prototype implements IndividualInterface {

    @Override
    public Iterator<GFComponent> iterator() {
        return delegate.iterator();
    }

    @Override
    public Population getPopulation() {
        return delegate.getPopulation();
    }

    @Override
    public void setPopulation(Population population) {
        delegate.setPopulation(population);
    }

    @Override
    public boolean addAction(GFAction action) {
        if (delegate.addAction(action)) {
            fireComponentAdded(action);
            return true;
        }
        return false;
    }

    private final Individual delegate;

    private final ListenerSupport<IndividualCompositionListener> listenerSupport = new ListenerSupport<IndividualCompositionListener>();

    public Prototype(Individual delegate) {
        this.delegate = delegate;
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
                listener.componentAdded(delegate, component);
            }
        });
    }

    private void fireComponentRemoved(final GFComponent component) {
        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {

            @Override
            public void update(IndividualCompositionListener listener) {
                listener.componentRemoved(delegate, component);
            }
        });
    }

    @Override
    public void changeActionExecutionOrder(final GFAction object, final GFAction object2) {
        delegate.changeActionExecutionOrder(object, object2);
        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {

            @Override
            public void update(IndividualCompositionListener listener) {
                listener.componentChanged(delegate, object);
            }
        });
    }

    public Individual deepCloneHelper(AbstractDeepCloneable.CloneMap map) {
        return delegate.deepCloneHelper(map);
    }

    protected void componentRemoved(final GFComponent component) {
        delegate.componentRemoved(component);
        fireComponentRemoved(component);
    }

    @Override
    public void freeze() {
        delegate.freeze();
    }

    @Override
    public boolean isFrozen() {
        return delegate.isFrozen();
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) throws IllegalStateException {
        delegate.checkConsistency(components);
    }

    @Override
    public <T> T checkFrozen(T value) {
        return delegate.checkFrozen(value);
    }

    @Override
    public void checkNotFrozen() {
        delegate.checkNotFrozen();
    }

    public Individual.Builder with() {
        return delegate.with();
    }

    @Override
    public Individual deepClone() {
        return delegate.deepClone();
    }

    @Override
    public boolean removeAction(GFAction action) {
        if (delegate.removeAction(action)) {
            fireComponentRemoved(action);
            return true;
        }
        return false;
    }

    @Override
    public void removeAllActions() {
        delegate.removeAllActions();
    }

    @Override
    public List<GFAction> getActions() {
        return delegate.getActions();
    }

    @Override
    public <T extends GFAction> T getAction(Class<T> t, String actionName) {
        return delegate.getAction(t, actionName);
    }

    @Override
    public <T extends GFAction> Iterable<GFAction> getActions(Class<T> class1) {
        return delegate.getActions(class1);
    }

    @Override
    public boolean addProperty(GFProperty property) {
        if (delegate.addProperty(property)) {
            fireComponentAdded(property);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasProperty(String name) {
        return delegate.hasProperty(name);
    }

    @Override
    public boolean hasAction(String name) {
        return delegate.hasAction(name);
    }

    @Override
    public boolean removeProperty(GFProperty property) {
        if (delegate.removeProperty(property)) {
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
    public List<GFProperty> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public <T extends GFProperty> Iterable<T> getProperties(Class<T> clazz) {
        return delegate.getProperties(clazz);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean isCloneOf(Object object) {
        return delegate.isCloneOf(object);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Iterable<? extends GFComponent> getComponents() {
        return delegate.getComponents();
    }

    @Override
    public <T extends GFInterface> T getInterface(Class<T> clazz) throws NoSuchElementException {
        return delegate.getInterface(clazz);
    }
}
