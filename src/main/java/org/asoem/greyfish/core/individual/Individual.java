package org.asoem.sico.core.individual;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Iterables.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.asoem.sico.core.actions.GFAction;
import org.asoem.sico.core.genes.Genome;
import org.asoem.sico.core.interfaces.GFInterface;
import org.asoem.sico.core.io.GreyfishLogger;
import org.asoem.sico.core.properties.GFProperty;
import org.asoem.sico.core.simulation.Initializeable;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.core.space.Location2D;
import org.asoem.sico.core.space.Location2DInterface;
import org.asoem.sico.core.space.MovingObject2DInterface;
import org.asoem.sico.core.space.Object2DListener;
import org.asoem.sico.lang.Functor;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.ListenerSupport;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@Root
public class Individual extends AbstractDeepCloneable implements MovingObject2DInterface {

	private final ListenerSupport<IndividualCompositionListener> listenerSupport = new ListenerSupport<IndividualCompositionListener>();

	@Element(name="population")
	private Population population = new Population();

	@ElementList(inline=true, entry="property", required=false)
	private List<GFProperty> properties = new ArrayList<GFProperty>();

	@ElementList(inline=true, entry="action", required=false)
	private List<GFAction> actions = new ArrayList<GFAction>();

	private final Collection<GFInterface> interfaces = new ArrayList<GFInterface>();

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

	public Individual(final Population population) {
		this.population = population;
		this.genome = new Genome();
		this.body = new Body(this);
	}

	protected Individual(Individual individual,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(individual, mapDict);

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

		finishAssembly();
	}

	/**
	 * @return the species
	 */
	public Population getPopulation() {
		return population;
	}

	/**
	 * @param population
	 */
	public void setPopulation(Population population) {
		Preconditions.checkNotNull(population);
		this.population = population;
	}

	/**
	 * Adds the given action to this individual.
	 * The action's execution level is set to the highest execution level found in this individual's actions +1;
	 * @param action
	 * @return {@code true} if action could be added, {@code false} otherwise.
	 */
	public boolean addAction(final GFAction action) throws IllegalArgumentException {
		return addComponent(actions, action);
	}

	private <T extends NamedIndividualComponent> boolean addComponent(final List<T> collection, final T component) {
		try {
			checkComponentAddition(component);
		}
		catch (IllegalArgumentException e) {
			GreyfishLogger.error("Could not add action", e);
			throw e;
		}

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

	/**
	 * @param action
	 */
	public boolean removeAction(final GFAction action) {
		if (this.actions.remove(action)) {
			action.setComponentOwner(null);
			componentRemoved(action);
			return true;
		}
		return false;
	}

	/**
	 * 
	 */
	public void removeAllActions() {
		for (GFAction a : actions) {
			removeAction(a);
		}
	}

	/**
	 * @return
	 */
	public List<GFAction> getActions() {
		return this.actions;
	}

	/**
	 * @param <T>
	 * @param t
	 * @param actionName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends GFAction> T getAction(Class<T> t, String actionName) {
		for (GFAction individualAction : actions) {
			if (individualAction.getName().equals(actionName)
					&& t.isInstance(individualAction)) {
				return (T) individualAction;
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
	 * @param property
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
		Preconditions.checkNotNull(component);

		if(component.getComponentOwner() != null
				&& component.getComponentOwner() != this) {
			if (GreyfishLogger.isDebugEnabled())
				GreyfishLogger.debug("Component already part of another individual");
			return false;
		}

		return true;
	}

	/**
	 * @param property
	 */
	public boolean removeProperty(final GFProperty property) {
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
		for (GFProperty p : properties) {
			removeProperty(p);
		}
	}

	public List<GFProperty> getProperties() {
		return properties;
	}

	/**
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends GFProperty> Collection<T> getProperties(Class<T> clazz) {
		ArrayList<T> ret = new ArrayList<T>();
		for (GFProperty property : properties)
			if(clazz.isInstance(property))
				ret.add((T) property);
		return ret;
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
			genome.addAll(Arrays.asList(property.getGenes()));
		}
	}

	public void finishAssembly() {
		assembleGenome();
	}

	public void activate(final Simulation simulation) {
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

	public int getComponentCount() {
		return properties.size() + actions.size();
	}

	public Body getBody() {
		return body;
	}
	
	/**
	 * Get the instance of {@code clazz} associated with this individual.
	 * If none is stored yet, the interface will be created using reflection.
	 * @param <T>
	 * @param clazz
	 * @return The instance of {@code clazz} associated with this individual
	 */
	public <T extends GFInterface> T getInterface(Class<T> clazz) {
		GFInterface ret = Iterables.find(interfaces, instanceOf(clazz), null);
		if (ret == null) {
			try {
				T newInstace = clazz.newInstance();
				newInstace.setComponentOwner(this);
				interfaces.add(newInstace);
				ret = newInstace;
			} catch (Exception e) {
				throw new AssertionError(e.getMessage());
			}
		}
		
		assert(ret != null);
		return (T) ret;
	}

	public State getState() {
		return state;
	}

	public GFAction getLastExecutedAction() {
		return lastExecutedAction;
	}

	/**
	 * Find the first action in the order of priority and execute it.
	 * @param simulation
	 * @return 
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

				if (GreyfishLogger.isTraceEnabled())
					GreyfishLogger.trace("Executed " + toExecute.getName() + "@" + this.getId());
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
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new Individual(this, mapDict);
	}

	private final void componentRemoved(final GFComponent component) {
		fireComponentRemoved(component);

		final Iterable<? extends GFComponent> components = getComponents();
		for (GFComponent individualComponent : components) {
			individualComponent.checkDependencies(components);
		}
	}

	private final void fireComponentRemoved(final GFComponent component) {
		listenerSupport.notifyListeners(new Functor<IndividualCompositionListener>() {

			@Override
			public void update(IndividualCompositionListener listener) {
				listener.componentRemoved(Individual.this, component);
			}
		});
	}

	/**
	 * @param simulation
	 * @return a deepClone of this individual fetched from the simulations pool of clones with the identical genetic constitution
	 * @throws Exception
	 */
	public Individual createClone(Simulation simulation) throws Exception {
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

	public float getOrientation() {
		return body.getOrientation();
	}

	public float getSpeed() {
		return body.getSpeed();
	}

	public void rotate(float alpha) {
		body.rotate(alpha);
	}

	@Override
	public float getX() {
		return body.getX();
	}

	@Override
	public float getY() {
		return body.getY();
	}	
}
