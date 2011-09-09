package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.ListenerSupport;
import org.simpleframework.xml.Root;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.asList;

@Root
public class Prototype extends AbstractAgent implements Agent {

    private static final Logger LOGGER = LoggerFactory.getLogger(Prototype.class);

    private final ListenerSupport<IndividualCompositionListener> listenerSupport = ListenerSupport.newInstance();

    public Prototype() {
    }

    protected Prototype(Prototype prototype, CloneMap map) {
        super(map.clone(prototype, Prototype.class), map);
        for (GFComponent component : getComponents())
            component.setAgent(this);
    }

    protected Prototype(Builder builder) {
        this.population = builder.population;

        for (GFProperty property : builder.properties.build())
            addProperty(property);

        for (GFAction property : builder.actions.build())
            addAction(property);
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
                listener.componentAdded(Prototype.this, component);
            }
        });
    }

    private void fireComponentRemoved(final GFComponent component) {
        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {

            @Override
            public void update(IndividualCompositionListener listener) {
                listener.componentRemoved(Prototype.this, component);
            }
        });
    }

    @Override
    public void changeActionExecutionOrder(final GFAction object, final GFAction object2) {
        super.changeActionExecutionOrder(object, object2);
        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {

            @Override
            public void update(IndividualCompositionListener listener) {
                listener.componentChanged(Prototype.this, object);
            }
        });
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAction(GFAction action) {
        if (componentCanBeAdded(action, getActions())
                && super.addAction(action)) {
            action.setAgent(this);
            fireComponentAdded(action);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAction(final GFAction action) {
        if (super.removeAction(action)) {
            action.setAgent(null);
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
                && super.addProperty(property)) {
            property.setAgent(this);
            fireComponentAdded(property);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeProperty(final GFProperty property) {
        if (super.removeProperty(property)) {
            property.setAgent(null);
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

    public static Prototype newInstance() {
        return new Prototype();
    }

    public static Prototype forPopulation(Population population) {
        Prototype abstractAgent = new Prototype();
        abstractAgent.setPopulation(population);
        return new Prototype();
    }

    @Override
    public void setOrientation(double alpha) {
        throw new UnsupportedOperationException();
    }

    private <T extends GFComponent> boolean componentCanBeAdded(final T component, Iterable<T> target) {
        Preconditions.checkNotNull(component);

        if(component.getAgent() != null
                && component.getAgent() != this) {
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

    public static class Builder implements BuilderInterface<AbstractAgent> {
        private final ImmutableList.Builder<GFAction> actions = ImmutableList.builder();
        private final ImmutableList.Builder<GFProperty> properties =  ImmutableList.builder();
        private Population population;

        public Builder population(Population population) { this.population = checkNotNull(population); return this; }
        public Builder addActions(GFAction ... actions) { this.actions.addAll(asList(checkNotNull(actions))); return this; }
        public Builder addProperties(GFProperty ... properties) { this.properties.addAll(asList(checkNotNull(properties))); return this; }

        @Override
        public AbstractAgent build() {
            checkState(population != null);
            return new Prototype(this);
        }
    }
}
