package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.ListenerSupport;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class Prototype extends AgentDecorator {

    private final ListenerSupport<IndividualCompositionListener> listenerSupport = ListenerSupport.newInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(Prototype.class);

    private Prototype(Prototype prototype, DeepCloner map) {
        super(map.continueWith(prototype.delegate(), Agent.class));
    }

    public Prototype(@Element(name="delegate") Agent delegate) {
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
                listener.componentAdded(delegate(), component);
            }
        });
    }

    private void fireComponentRemoved(final GFComponent component) {
        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {

            @Override
            public void update(IndividualCompositionListener listener) {
                listener.componentRemoved(delegate(), component);
            }
        });
    }

    @Override
    public void changeActionExecutionOrder(final GFAction object, final GFAction object2) {
        delegate().changeActionExecutionOrder(object, object2);
        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {

            @Override
            public void update(IndividualCompositionListener listener) {
                listener.componentChanged(delegate(), object);
            }
        });
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException("Execution not allowed for a prototype");
    }

    @Override
    public Simulation getSimulation() {
        return delegate().getSimulation();
    }

    @Override
    public boolean addAction(GFAction action) {
        if (componentCanBeAdded(action, getActions())
                && delegate().addAction(action)) {
            action.setAgent(this);
            fireComponentAdded(action);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAction(final GFAction action) {
        if (delegate().removeAction(action)) {
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
        if (componentCanBeAdded(property, getProperties())
                && delegate().addProperty(property)) {
            property.setAgent(this);
            fireComponentAdded(property);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeProperty(final GFProperty property) {
        if (delegate().removeProperty(property)) {
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
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new Prototype(this, cloner);
    }

    public static Prototype newInstance(Agent individual) {
        return new Prototype(individual);
    }

    @Override
    public void setOrientation(double alpha) {
        throw new UnsupportedOperationException();
    }

    private <T extends GFComponent> boolean componentCanBeAdded(final T component, Iterable<T> target) {
        Preconditions.checkNotNull(component);

        if(component.getAgent() != null && component.getAgent() != this) {
            LOGGER.debug("Component already part of another individual");
            return false;
        }

        // duplicate check
        return Iterables.find(target, new Predicate<T>() {

            @Override
            public boolean apply(T object) {
                return object.getName().equals(component.getName());
            }
        }, null) == null;

    }

    @Override
    public String toString() {
        return "Prototype[" + getPopulation() + "]";
    }

    @Override
    public void prepare(Simulation context) {
        throw new UnsupportedOperationException("A prototype should not be used in a simulation");
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public void setSimulation(Simulation simulation) {
        throw new UnsupportedOperationException();
    }

    public static Builder with() {
        return new Builder();
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<Prototype> {
        @Override
        public Prototype build() {
            return new Prototype(new MutableAgent(checkedSelf()));
        }
        @Override
        protected Builder self() {
            return this;
        }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends MutableAgent.AbstractBuilder<T> {
    }
}
