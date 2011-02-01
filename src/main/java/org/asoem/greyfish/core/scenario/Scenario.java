package org.asoem.greyfish.core.scenario;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.individual.*;
import org.asoem.greyfish.core.space.*;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.DeepCloneable;
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
            @ElementList(name="prototypes", entry="individual") Collection<DeepCloneable> prototypes,
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
        for (Map.Entry<IndividualInterface, Location2DInterface> entry : builder.map.entries()) {
            addPlaceholder(Placeholder.newInstance(entry.getKey(), entry.getValue()));
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
    public Collection<Prototype> getPrototypes() {
        return Sets.newHashSet(Iterables.transform(prototypeSpace.getOccupants(), new Function<Object2DInterface, Prototype>() {
            @Override
            public Prototype apply(Object2DInterface input) {
                Preconditions.checkArgument(input instanceof Placeholder);
                return Placeholder.class.cast(input).asPrototype();
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
                               Prototype prototype, int index) {
        /* IGNORE */
    }

    @Override
    public void prototypeRemoved(PrototypeManager source,
                                 Prototype prototype, int index) {
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
        private Multimap<IndividualInterface, Location2DInterface> map = ArrayListMultimap.create();
        private String name;

        public Builder name(String name) { this.name = name; return this; }
        public Builder space(TiledSpace space) { this.space = checkNotNull(space); return this; }
        public Builder add(final IndividualInterface clonable, Location2DInterface location2d) {
            checkNotNull(clonable);
            checkNotNull(location2d);
            checkState(!Iterables.any(map.keySet(), new Predicate<IndividualInterface>() {
                @Override
                public boolean apply(IndividualInterface simulationObject) {
                    return simulationObject.getPopulation().equals(clonable.getPopulation());
                }
            }));
            map.put(clonable, checkNotNull(location2d)); return this;
        }
        @Override
        public Scenario build() {
            checkState(space != null);
            checkState(Iterables.all(map.entries(), new Predicate<Map.Entry<IndividualInterface, Location2DInterface>>() {
                @Override
                public boolean apply(Map.Entry<IndividualInterface, Location2DInterface> simulationObjectLocation2DEntry) {
                    return space.covers(simulationObjectLocation2DEntry.getValue());
                }
            }));
            return new Scenario(this);
        }
    }
}
