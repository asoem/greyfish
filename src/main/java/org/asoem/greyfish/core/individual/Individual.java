package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.interfaces.GFInterface;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Initializeable;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.Location2DInterface;
import org.asoem.greyfish.core.space.MovingObject2DInterface;
import org.asoem.greyfish.core.space.Object2DListener;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.ListenerSupport;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import java.awt.*;
import java.util.*;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Iterables.filter;
import static java.util.Arrays.asList;

@Root
public class Individual extends AbstractDeepCloneable<Individual> implements MovingObject2DInterface, SimulationObject {

    private final ListenerSupport<IndividualCompositionListener> listenerSupport = new ListenerSupport<IndividualCompositionListener>();

    @Element(name="population")
    private Population population = new Population();

    @ElementList(inline=true, entry="property", required=false)
    private List<GFProperty> properties = new ArrayList<GFProperty>();

    @ElementList(inline=true, entry="action", required=false)
    private List<GFAction> actions = new ArrayList<GFAction>();

    private Collection<GFInterface> interfaces = new ArrayList<GFInterface>();

    public enum State {
        PROTOTYPE,
        PASSIVE_CLONE,
        ACTIVE_CLONE
    }

    private State state = State.PROTOTYPE;

    /************ Required only during simulation */
//	private AbstractLocation location;

    private Genome genome;

    private Body body;

    private int id;

    private int timeOfBirth;

    private GFAction lastExecutedAction;
    /************ Required only during simulation */

    public Individual() {
        this.genome = new Genome();
        this.body = new Body(this);
    }

    public Individual(Builder builder) {
        this.population = builder.population;
        for (GFProperty property : builder.properties.build())
            addProperty(property);
        for (GFAction property : builder.actions.build())
            addAction(property);

        this.genome = new Genome();
        this.body = new Body(this);
        freeze();
    }

    public Individual(final Population population) {
        this.population = population;
        this.genome = new Genome();
        this.body = new Body(this);
    }

    protected Individual(Individual individual, CloneMap mapDict) {
        this.state = individual.state;
        this.population = individual.population;
        this.genome = new Genome();
        this.body = new Body(this, individual.body);

        properties = new ArrayList<GFProperty>(individual.properties.size());
        for (GFProperty property : individual.properties)
            addProperty(deepClone(property, mapDict));

        actions = new ArrayList<GFAction>(individual.actions.size());
        for (GFAction action : individual.actions) {
            addAction(deepClone(action, mapDict));
        }

        freeze();
    }

    public Population getPopulation() {
        return population;
    }

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
    public boolean addAction(final GFAction action) {
        return addComponent(actions, action);
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
        component.setComponentOwner(this);

        fireComponentAdded(component);

        if (GreyfishLogger.isTraceEnabled())
            GreyfishLogger.trace("Component " + component.getName() + " added to " + this);

        return true;
    }

    public boolean removeAction(final GFAction action) {
        checkNotFrozen();
        if (this.actions.remove(action)) {
            action.setComponentOwner(null);
            componentRemoved(action);
            return true;
        }
        return false;
    }

    public void removeAllActions() {
        checkNotFrozen();
        for (GFAction a : actions) {
            removeAction(a);
        }
    }

    public List<GFAction> getActions() {
        return this.actions;
    }

    public <T extends GFAction> T getAction(Class<T> t, String actionName) {
        for (GFAction individualAction : actions) {
            if (individualAction.getName().equals(actionName)
                    && t.isInstance(individualAction)) {
                return t.cast(individualAction);
            }
        }
        return null;
    }

    public <T extends GFAction> Iterable<GFAction> getActions(
            final Class<T> class1) {
        return filter(actions, instanceOf(class1));
    }

    /**
     * Add <code>property</code> to the Individuals properties if it does not contain one with the same key (i.e. property.getPropertyName() ).
     * @param property The property to add
     * @return <code>true</code> if <code>property</code> could be added, <code>false</code> otherwise.
     */
    public boolean addProperty(final GFProperty property) {
        return addComponent(properties, property);
    }

    public boolean hasProperty(final String name) {
        for (GFProperty p : properties) {
            if (p.getName().equals(name))
                return true;
        }
        return false;
    }

    public boolean hasAction(final String name) {
        for (GFAction a : actions) {
            if (a.getName().equals(name))
                return false;
        }
        return true;
    }

    private void fireComponentAdded(final GFComponent component) {
        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {

            @Override
            public void update(IndividualCompositionListener listener) {
                listener.componentAdded(Individual.this, component);
            }
        });
    }

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

    public boolean removeProperty(final GFProperty property) {
        checkNotFrozen();
        if (this.properties.remove(property)) {
            property.setComponentOwner(null);
            componentRemoved(property);
            return true;
        }
        return false;
    }

    /**
     *
     */
    public void removeAllProperties() {
        checkNotFrozen();
        for (GFProperty p : properties) {
            removeProperty(p);
        }
    }

    public List<GFProperty> getProperties() {
        return properties;
    }

    public <T extends GFProperty> Collection<T> getProperties(Class<T> clazz) {
        ArrayList<T> ret = new ArrayList<T>();
        for (GFProperty property : properties)
            if(clazz.isInstance(property))
                ret.add(clazz.cast(property));
        return ret;
    }

    @Override
    public double getRadius() {
        return body.getRadius();
    }

    @Override
    public Color getColor() {
        return body.getColor();
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
        return String.format("%1d (%2s)", id, population.toString());
    }

    @SuppressWarnings("unused")
    @Commit
    private final void commit() {
        for (GFComponent component : getComponents()) {
            component.setComponentOwner(this);
        }
    }

    public void mutate() {
        Preconditions.checkState(state == State.ACTIVE_CLONE);
        genome.mutate();
    }

    /**
     * Get the genome. This is <code>null</code> if no call to
     * <code>assembleGenome()</code> has been made at least once before.
     * @return This individuals genome.
     */
    public Genome getGenome() {
        return genome;
    }

    public void setGenome(final Genome genome) {
        assert this.genome != null;
        Preconditions.checkNotNull(genome);
        this.genome.initGenome(genome);
    }

    /**
     * Assemble the genome from the current set of the individual's properties.
     * This means, that the genome is not updated automatically.
     */
    private void assembleGenome() {
        assert genome != null;
        assert properties != null;
        genome.clear();
        for (GFProperty property : this.properties) {
            genome.addAll(property.getGeneList());
        }
    }

    public void finishAssembly() {
        assembleGenome();
    }

    @Override
    public void initialize(Simulation simulation) {
        Preconditions.checkNotNull(simulation);

        body.initialize(simulation);
        genome.initialize();

        // call initializers
        for (Initializeable component : getComponents()) {
            component.initialize(simulation);
        }
        for (Initializeable component : getComponents()) { // new sensors and actuators might have got instantiated after the first round //TODO Make this dirty hack unnecessary
            component.initialize(simulation);
        }

        state = State.ACTIVE_CLONE;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimeOfBirth(int timeOfBirth) {
        this.timeOfBirth = timeOfBirth;
    }

    public void passivate() {
        Preconditions.checkState(state == State.ACTIVE_CLONE);
        state = State.PASSIVE_CLONE;
    }

    public boolean isAlive() {
        return state == State.ACTIVE_CLONE;
    }

    /**
     * Get the unique (for this scenario) ID of this Individual which is set at <code>birth()</code>.
     * IDs start at 1 => 0 is NOT an ID and therefore not unique
     * @return The unique ID of this Individual or 0
     */
    public int getId() {
        return id;
    }

    public int getTimeOfBirth() {
        return timeOfBirth;
    }

    public boolean isCloneOf(Object object) {
        return object instanceof Individual
                && population.equals(((Individual)object).population);
    }

    public String getName() {
        return population.getName();
    }

    // TODO: Should better return a Component Graph
    public Iterable<? extends GFComponent> getComponents() {

        return Iterables.concat(
                properties,
                actions,
                interfaces);
        // Conditions?
    }

    public Body getBody() {
        return body;
    }

    /**
     * Get the instance of {@code clazz} associated with this individual.
     * If none is stored yet, the interface will be created using reflection.
     * @param <T> The Type of the Interface object
     * @param clazz The Class for type T
     * @return The instance of {@code clazz} associated with this individual
     * @throws NoSuchElementException if no Interface of type clazz could be found
     */
    public <T extends GFInterface> T getInterface(Class<T> clazz) throws NoSuchElementException {
        checkNotNull(clazz);
        T ret = clazz.cast(Iterables.find(interfaces, instanceOf(clazz), null));
        if (ret == null) {
            try {
                ret = clazz.cast(clazz.getDeclaredMethod("newInstance").invoke(null));
                ret.setComponentOwner(this);
                interfaces = ImmutableList.<GFInterface>builder().addAll(interfaces).add(ret).build();
            } catch (Exception e) {
                NoSuchElementException nsee = new NoSuchElementException(clazz.getName());
                nsee.initCause(e);
                throw nsee;
            }
        }
        return ret;
    }

    public State getState() {
        return state;
    }

    public GFAction getLastExecutedAction() {
        return lastExecutedAction;
    }

    /**
     * Find the first actions in the order of priority and execute it.
     * @param simulation The simulation context
     */
    public void execute(final Simulation simulation) {
        GFAction toExecute = lastExecutedAction;

        if (toExecute == null
                || lastExecutedAction.done()) {
            toExecute = null;
            for (GFAction action : actions) {
                if (action.evaluate(simulation)) {
                    toExecute = action;
                    break;
                }
            }
        }
        try {
            if (toExecute != null) {
                toExecute.executeUnevaluated(simulation);
                lastExecutedAction = toExecute;

                if (GreyfishLogger.isDebugEnabled())
                    GreyfishLogger.debug("Executed " + toExecute + "@" + this.getId());
            }
        }
        catch (RuntimeException e) {
            GreyfishLogger.error("Error during execution of " + toExecute.getName(), e);
        }
    }

    public void addCompositionListener(
            IndividualCompositionListener individualCompositionListener) {
        listenerSupport.addListener(individualCompositionListener);
    }

    public void removeCompositionListener(
            IndividualCompositionListener individualCompositionListener) {
        listenerSupport.removeListener(individualCompositionListener);
    }

    public void changeActionExecutionOrder(final GFAction object, final GFAction object2) {
        Preconditions.checkNotNull(object);
        Preconditions.checkNotNull(object2);
        if ( ! actions.contains(object) || ! actions.contains(object2))
            throw new IllegalArgumentException();
        int index1 = actions.indexOf(object);
        int index2 = actions.indexOf(object2);
        actions.add(index2, actions.remove(index1));
        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {

            @Override
            public void update(IndividualCompositionListener listener) {
                listener.componentChanged(Individual.this, object);
            }
        });
    }

    @Override
    protected Individual deepCloneHelper(CloneMap map) {
        return new Individual(this, map);
    }

    private void componentRemoved(final GFComponent component) {
        fireComponentRemoved(component);

        final Iterable<? extends GFComponent> components = getComponents();
        for (GFComponent individualComponent : components) {
            individualComponent.checkIfFreezable(components);
        }
    }

    private void fireComponentRemoved(final GFComponent component) {
        listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {

            @Override
            public void update(IndividualCompositionListener listener) {
                listener.componentRemoved(Individual.this, component);
            }
        });
    }

    /**
     * @param simulation The simulation context
     * @return a deepClone of this individual fetched from the simulations pool of clones with the identical genetic constitution
     */
    public Individual createClone(Simulation simulation) {
        Preconditions.checkNotNull(simulation);
        final Individual ret = simulation.createClone(population);
        ret.setGenome(new Genome(genome));
        return ret;
    }

    @Override
    public Location2D getAnchorPoint() {
        return body.getAnchorPoint();
    }

    @Override
    public void addListener(Object2DListener listener) {
        body.addListener(listener);
    }

    @Override
    public void removeListener(Object2DListener listener) {
        body.removeListener(listener);
    }

    @Override
    public void setAnchorPoint(Location2DInterface location2d) {
        body.setAnchorPoint(location2d);
    }

    public double getOrientation() {
        return body.getOrientation();
    }

    public double getSpeed() {
        return body.getSpeed();
    }

    public void rotate(double alpha) {
        body.rotate(alpha);
    }

    @Override
    public double getX() {
        return body.getX();
    }

    @Override
    public double getY() {
        return body.getY();
    }

    @Override
    public void freeze() {
        finishAssembly();
        for (GFComponent component : getComponents()) {
            component.checkIfFreezable(getComponents());
            component.freeze();
        }
        actions = ImmutableList.copyOf(actions);
        properties = ImmutableList.copyOf(properties);
        interfaces = ImmutableList.copyOf(interfaces);
    }

    @Override
    public void checkIfFreezable(Iterable<? extends GFComponent> components) throws IllegalStateException {
        for (GFComponent component : getComponents())
            component.checkIfFreezable(components);
    }

    @Override
    public <T> T checkFrozen(T value) {
        checkNotFrozen();
        return value;
    }

    @Override
    public boolean isFrozen() {
        return state != State.PROTOTYPE;
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
