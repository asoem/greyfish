package org.asoem.greyfish.core.scenario;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.individual.PrototypeManager;
import org.asoem.greyfish.core.individual.PrototypeRegistryListener;
import org.asoem.greyfish.core.individual.SimulationObject;
import org.asoem.greyfish.core.space.*;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.DeepClonable;
import org.asoem.greyfish.utils.ListenerSupport;
import org.simpleframework.xml.*;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Root(name="scenario")
public class Scenario implements PrototypeRegistryListener {

    /**
     * TODO: nothing is fired yet
     */
    private final ListenerSupport<ScenarioListener> listenerSupport = new ListenerSupport<ScenarioListener>();

    @Element(name="space")
    private final TiledSpace prototypeSpace;

    @Attribute(name="name")
    private final String name;

    @SuppressWarnings("unused") // for deserialization using Simple API
    private Scenario(
            @Attribute(name="name") String name,
            @ElementList(name="prototypes", entry="individual") Collection<DeepClonable> prototypes,
            @Element(name="space") TiledSpace space,
            @ElementArray(name="placeholder-list", entry="placeholder") Placeholder[] placeholders) {
        assert name != null;
        assert prototypes != null;
        assert space != null;
        assert placeholders != null;

        this.name = name;
        this.prototypeSpace = space;
        for (Placeholder placeholder : placeholders) {
            prototypeSpace.addOccupant(placeholder);
        }
    }

    private Scenario(Builder builder) {
        this.name = builder.name;
        this.prototypeSpace = builder.space;
        for (Map.Entry<SimulationObject, Location2D> entry : builder.map.entries()) {
            addPlaceholder(new Placeholder(entry.getKey(), entry.getValue()));
        }
    }

    public void addPlaceholder(Placeholder placeholder) {
        Preconditions.checkNotNull(placeholder);
        prototypeSpace.addOccupant(placeholder);
    }

    public boolean removePlaceholder(Placeholder ph) {
        return prototypeSpace.removeOccupant(ph);
    }

    @ElementList(name="prototypes", entry="individual")
    public Collection<SimulationObject> getPrototypes() {
        return Sets.newHashSet(Iterables.transform(prototypeSpace.getOccupants(), new Function<Object2DInterface, SimulationObject>() {
            @Override
            public SimulationObject apply(Object2DInterface input) {
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

    @SuppressWarnings("unused")
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
        throw new UnsupportedOperationException("Not Implemented yet");
        // TODO: implement
    }

    public String getName() {
        return name;
    }

    public TiledSpace getSpace() {
        return prototypeSpace;
    }

    public static Builder with() {return new Builder(); }
    public static class Builder implements BuilderInterface<Scenario> {
        private TiledSpace space;
        private Multimap<SimulationObject, Location2D> map = ArrayListMultimap.create();
        private String name;

        public Builder name(String name) { this.name = name; return this; }
        public Builder space(TiledSpace space) { this.space = checkNotNull(space); return this; }
        public Builder add(final SimulationObject clonable, Location2D location2d) {
            checkNotNull(clonable);
            checkNotNull(location2d);
            checkState(!Iterables.any(map.keySet(), new Predicate<SimulationObject>() {
                @Override
                public boolean apply(SimulationObject simulationObject) {
                    return simulationObject.getPopulation().equals(clonable.getPopulation());
                }
            }));
            map.put(clonable, checkNotNull(location2d)); return this;
        }
        @Override
        public Scenario build() {
            checkState(space != null);
            checkState(Iterables.all(map.entries(), new Predicate<Map.Entry<SimulationObject, Location2D>>() {
                @Override
                public boolean apply(Map.Entry<SimulationObject, Location2D> simulationObjectLocation2DEntry) {
                    return space.covers(simulationObjectLocation2DEntry.getValue());
                }
            }));
            return new Scenario(this);
        }
    }
}
