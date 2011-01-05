package org.asoem.sico.core.scenario;

import java.util.Collection;

import org.asoem.sico.core.individual.Individual;
import org.asoem.sico.core.individual.PrototypeManager;
import org.asoem.sico.core.individual.PrototypeRegistryListener;
import org.asoem.sico.core.space.Object2DInterface;
import org.asoem.sico.core.space.Placeholder;
import org.asoem.sico.core.space.TileLocation;
import org.asoem.sico.core.space.TiledSpace;
import org.asoem.sico.utils.DeepClonable;
import org.asoem.sico.utils.ListenerSupport;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

@Root(name="scenario")
public class Scenario implements PrototypeRegistryListener {

	/**
	 * TODO: nothing is fired yet
	 */
	private final ListenerSupport<ScenarioListener> listenerSupport = new ListenerSupport<ScenarioListener>();
	
	@Element(name="space")
	private final TiledSpace prototypeSpace;

	@Attribute(name="name")
	private String name;

	/**
	 * @param prototypes
	 * @param space
	 */
	@SuppressWarnings("unused") // for deserialization using Simple API
	private Scenario(
			@ElementList(name="prototypes", entry="individual") Collection<DeepClonable> prototypes,
			@Element(name="space") TiledSpace space,
			@ElementArray(name="placeholder-list", entry="placeholder") Placeholder[] pIterable) {
		assert(prototypes != null);
		assert(space != null);
		assert(pIterable != null);
		
		this.prototypeSpace = space;
		for (Placeholder placeholder : pIterable) {
			prototypeSpace.addOccupant(placeholder);
		}
	}

	public Scenario(TiledSpace space) {
		Preconditions.checkNotNull(space);
		this.prototypeSpace = space;
	}

	public void addPlaceholder(Placeholder placeholder) {
		Preconditions.checkNotNull(placeholder);
		prototypeSpace.addOccupant(placeholder);
	}
	
	public boolean removePlaceholder(Placeholder ph) {
		return prototypeSpace.removeOccupant(ph);
	}

	@ElementList(name="prototypes", entry="individual")
	public Collection<DeepClonable> getPrototypes() {
		return Sets.newHashSet(Iterables.transform(prototypeSpace.getOccupants(), new Function<Object2DInterface, DeepClonable>() {
			@Override
			public DeepClonable apply(Object2DInterface input) {
				return ((Placeholder)input).getPrototype();
			}
		}));
	}

	@ElementArray(name="placeholder-list", entry="placeholder")
	public Placeholder[] getPlaceholder() {
		return Iterables.toArray(Iterables.transform(prototypeSpace.getOccupants(), new Function<Object2DInterface, Placeholder>() {
			@Override
			public Placeholder apply(Object2DInterface input) {
				return (Placeholder)input;
			}
		}), Placeholder.class);
	}
	
	public Iterable<Placeholder> getPlaceholder(TileLocation location) {
		return Iterables.transform(prototypeSpace.getOccupants(location), new Function<Object2DInterface, Placeholder>() {
			@Override
			public Placeholder apply(Object2DInterface input) {
				return (Placeholder)input;
			}
		});
	}

	public TiledSpace getPrototypeSpace() {
		return prototypeSpace;
	}

	public void addScenarioListener(ScenarioListener listener) {
		listenerSupport.addListener(listener);
	}

	public void removeScenarioListener(ScenarioListener listener) {
		listenerSupport.removeListener(listener);
	}

	@Override
	public void prototypeAdded(PrototypeManager source,
			Individual prototype, int index) {
		/* IGNORE */
	}

	@Override
	public void prototypeRemoved(PrototypeManager source,
			Individual prototype, int index) {
		// TODO: implement
	}

	public void setName(String text) {
		this.name = text;
	}

	public String getName() {
		return name;
	}

	public TiledSpace getSpace() {
		return prototypeSpace;
	}
}
