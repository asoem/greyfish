package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.interfaces.GFInterface;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Iterables.filter;
import static java.util.Arrays.asList;

@Root
public class Individual extends AbstractDeepCloneable<Individual> implements IndividualInterface {

//    private final ListenerSupport<IndividualCompositionListener> listenerSupport = new ListenerSupport<IndividualCompositionListener>();

    @Element(name="population")
    private Population population = new Population();

    @ElementList(inline=true, entry="property", required=false)
    private List<GFProperty> properties = new ArrayList<GFProperty>();

    @ElementList(inline=true, entry="action", required=false)
    private List<GFAction> actions = new ArrayList<GFAction>();

    private Collection<GFInterface> interfaces = new ArrayList<GFInterface>();

    @Override
    public Iterator<GFComponent> iterator() {
        return Iterators.concat(
                properties.iterator(),
                actions.iterator(),
                interfaces.iterator());
    }

    public Individual() {
    }

    public Individual(Builder builder) {
        this.population = builder.population;

        for (GFProperty property : builder.properties.build())
            addProperty(property);
        for (GFAction property : builder.actions.build())
            addAction(property);
    }

    public Individual(final Population population) {
        this.population = population;
    }

    protected Individual(Individual individual, CloneMap mapDict) {
        this.population = individual.population;

        for (GFProperty property : individual.properties)
            addProperty(deepClone(property, mapDict));

        for (GFAction action : individual.actions) {
            addAction(deepClone(action, mapDict));
        }
    }

    @Override
    public Population getPopulation() {
        return population;
    }

    @Override
    public void setPopulation(Population population) {
        checkNotNull(population);
        this.population = population;
    }

    /**
     * Adds the given actions to this individual.
     * The actions's execution level is set to the highest execution level found in this individual's actions +1;
     * @param action The action to add
     * @return {@code true} if actions could be added, {@code false} otherwise.
     */
    @Override
    public boolean addAction(final GFAction action) {
        return addComponent(actions, action);
    }

    protected <T extends NamedIndividualComponent> boolean addComponent(final List<T> collection, final T component) {
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

//        fireComponentAdded(component);

        if (GreyfishLogger.isTraceEnabled())
            GreyfishLogger.trace("Component " + component.getName() + " added to " + this);

        return true;
    }

    @Override
    public boolean removeAction(final GFAction action) {
        checkNotFrozen();
        if (this.actions.remove(action)) {
            action.setComponentRoot(null);
            componentRemoved(action);
            return true;
        }
        return false;
    }

    @Override
    public void removeAllActions() {
        checkNotFrozen();
        for (GFAction a : actions) {
            removeAction(a);
        }
    }

    @Override
    public List<GFAction> getActions() {
        return Collections.unmodifiableList(actions);
    }

    @Override
    public <T extends GFAction> T getAction(Class<T> t, String actionName) {
        for (GFAction individualAction : actions) {
            if (individualAction.getName().equals(actionName)
                    && t.isInstance(individualAction)) {
                return t.cast(individualAction);
            }
        }
        return null;
    }

    @Override
    public <T extends GFAction> Iterable<GFAction> getActions(
            final Class<T> class1) {
        return filter(actions, instanceOf(class1));
    }

    /**
     * Add <code>property</code> to the Individuals properties if it does not contain one with the same key (i.e. property.getPropertyName() ).
     * @param property The property to add
     * @return <code>true</code> if <code>property</code> could be added, <code>false</code> otherwise.
     */
    @Override
    public boolean addProperty(final GFProperty property) {
        return addComponent(properties, property);
    }

    @Override
    public boolean hasProperty(final String name) {
        for (GFProperty p : properties) {
            if (p.getName().equals(name))
                return true;
        }
        return false;
    }

    @Override
    public boolean hasAction(final String name) {
        for (GFAction a : actions) {
            if (a.getName().equals(name))
                return false;
        }
        return true;
    }

//    private void fireComponentAdded(final GFComponent component) {
//        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {
//
//            @Override
//            public void update(IndividualCompositionListener listener) {
//                listener.componentAdded(Individual.this, component);
//            }
//        });
//    }

    private boolean checkComponentAddition(final GFComponent component) {
        checkNotFrozen();
        Preconditions.checkNotNull(component);

        if(component.getComponentOwner() != null
                && component.getComponentOwner() != this) {
            if (GreyfishLogger.isDebugEnabled())
                GreyfishLogger.debug("Component already part of another individual");
            return false;
        }

        return true;
    }

    @Override
    public boolean removeProperty(final GFProperty property) {
        checkNotFrozen();
        if (this.properties.remove(property)) {
            property.setComponentRoot(null);
            componentRemoved(property);
            return true;
        }
        return false;
    }

    /**
     *
     */
    @Override
    public void removeAllProperties() {
        checkNotFrozen();
        for (GFProperty p : properties) {
            removeProperty(p);
        }
    }

    @Override
    public List<GFProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public <T extends GFProperty> Iterable<T> getProperties(Class<T> clazz) {
        return Iterables.filter(properties, clazz);
    }

//	/**
//	 * @return the location
//	 */
//	public AbstractLocation getLocation() {
//		return null;;
//	}

//	/**
//	 * @return
//	 */
//	public AbstractLocation[] getAdjacentLocations() {
//		Preconditions.checkState(getLocation() != null);
//		return getLocation().getAdjacentLocations();
//	}

//	/**
//	 * @param location the location to set
//	 */
//	public void setLocation(AbstractLocation location) {
//		if ( ! Objects.equal(this.getLocation(), location)) {
//			if(this.getLocation() != null)
//				this.getLocation().removeOwner(this);
//			this.location = location;
//			if(this.getLocation() != null)
//				this.getLocation().addOwner(this);
//		}
//	}

    @Override
    public String toString() {
        return population.toString();
    }

    @SuppressWarnings("unused")
    @Commit
    private void commit() {
        for (GFComponent component : getComponents()) {
            component.setComponentRoot(this);
        }
    }

    @Override
    public boolean isCloneOf(Object object) {
        return object instanceof Individual
                && population.equals(((Individual)object).population);
    }

    @Override
    public String getName() {
        return population.getName();
    }

    // TODO: Should better return a Component Graph
    @Override
    public Iterable<? extends GFComponent> getComponents() {

        return Iterables.concat(
                properties,
                actions,
                interfaces);
        // Conditions?
    }

    /**
     * Get the instance of {@code clazz} associated with this individual.
     * If none is stored yet, the interface will be created using reflection.
     * @param <T> The Type of the Interface object
     * @param clazz The Class for type T
     * @return The instance of {@code clazz} associated with this individual
     * @throws NoSuchElementException if no Interface of type clazz could be found
     */
    @Override
    public <T extends GFInterface> T getInterface(Class<T> clazz) throws NoSuchElementException {
        checkNotNull(clazz);
        T ret = clazz.cast(Iterables.find(interfaces, instanceOf(clazz), null));
        if (ret == null) {
            try {
                ret = clazz.cast(clazz.getDeclaredMethod("newInstance").invoke(null));
                ret.setComponentRoot(this);
                interfaces = ImmutableList.<GFInterface>builder().addAll(interfaces).add(ret).build();
            } catch (Exception e) {
                NoSuchElementException nsee = new NoSuchElementException(clazz.getName());
                nsee.initCause(e);
                throw nsee;
            }
        }
        return ret;
    }

//    public void addCompositionListener(
//            IndividualCompositionListener individualCompositionListener) {
//        listenerSupport.addListener(individualCompositionListener);
//    }
//
//    public void removeCompositionListener(
//            IndividualCompositionListener individualCompositionListener) {
//        listenerSupport.removeListener(individualCompositionListener);
//    }

    @Override
    public void changeActionExecutionOrder(final GFAction object, final GFAction object2) {
        Preconditions.checkNotNull(object);
        Preconditions.checkNotNull(object2);
        if ( ! actions.contains(object) || ! actions.contains(object2))
            throw new IllegalArgumentException();
        int index1 = actions.indexOf(object);
        int index2 = actions.indexOf(object2);
        actions.add(index2, actions.remove(index1));
//        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {
//
//            @Override
//            public void update(IndividualCompositionListener listener) {
//                listener.componentChanged(Individual.this, object);
//            }
//        });
    }

    @Override
    protected Individual deepCloneHelper(CloneMap map) {
        return new Individual(this, map);
    }

    protected void componentRemoved(final GFComponent component) {
//        fireComponentRemoved(component);

        final Iterable<? extends GFComponent> components = getComponents();
        for (GFComponent individualComponent : components) {
            individualComponent.checkConsistency(components);
        }
    }

//    private void fireComponentRemoved(final GFComponent component) {
//        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {
//
//            @Override
//            public void update(IndividualCompositionListener listener) {
//                listener.componentRemoved(Individual.this, component);
//            }
//        });
//    }

    @Override
    public void freeze() {
        checkConsistency(getComponents());
        for (GFComponent component : getComponents())
            component.freeze();
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) throws IllegalStateException {
        for (GFComponent component : getComponents())
            component.checkConsistency(components);
    }

    @Override
    public <T> T checkFrozen(T value) {
        checkNotFrozen();
        return value;
    }

    @Override
    public void checkNotFrozen() {
        if (isFrozen()) throw new IllegalStateException("Individual is frozen");
    }

    public static Builder with() { return new Builder(); }
    public static class Builder implements BuilderInterface<Individual> {
        private ImmutableList.Builder<GFAction> actions = ImmutableList.builder();
        private ImmutableList.Builder<GFProperty> properties =  ImmutableList.builder();
        private Population population;

        public Builder population(Population population) { this.population = checkNotNull(population); return this; }
        public Builder addActions(GFAction ... actions) { this.actions.addAll(asList(checkNotNull(actions))); return this; }
        public Builder addProperties(GFProperty ... properties) { this.properties.addAll(asList(checkNotNull(properties))); return this; }

        @Override
        public Individual build() {
            checkState(population != null);
            return new Individual(this);
        }
    }
}
