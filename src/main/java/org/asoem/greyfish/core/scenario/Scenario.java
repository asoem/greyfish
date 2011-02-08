package org.asoem.greyfish.core.scenario;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.individual.Placeholder;
import org.asoem.greyfish.core.individual.Prototype;
import org.asoem.greyfish.core.individual.PrototypeManager;
import org.asoem.greyfish.core.individual.PrototypeRegistryListener;
import org.asoem.greyfish.core.space.Location2DInterface;
import org.asoem.greyfish.core.space.Object2DInterface;
import org.asoem.greyfish.core.space.TileLocation;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.ListenerSupport;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.Root;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Root(name="scenario")
public class Scenario implements PrototypeRegistryListener {

    /**
     * TODO: nothing is fired yet
     */
    private final ListenerSupport<ScenarioListener> listenerSupport = ListenerSupport.newInstance();

    @Element(name="space")
    private final TiledSpace prototypeSpace;

    @Attribute(name="name")
    private final String name;

    private final Set<Prototype> prototypes = Sets.newHashSet();

    @SuppressWarnings("unused") // for deserialization using Simple API
    private Scenario(
            @Attribute(name="name") String name,
            @ElementArray(name="prototypes", entry="prototype") Prototype[] prototypes,
            @Element(name="space") TiledSpace space,
            @ElementArray(name="placeholders", entry="placeholder") Placeholder[] placeholders) {
        assert name != null;
        assert prototypes != null;
        assert space != null;
        assert placeholders != null;

        this.name = name;
        this.prototypeSpace = space;
        this.prototypes.addAll(Arrays.asList(prototypes));
        for (Placeholder placeholder : placeholders) {
            prototypeSpace.addOccupant(placeholder);
        }
    }

    private Scenario(Builder builder) {
        this.name = builder.name;
        this.prototypeSpace = TiledSpace.copyOf(builder.space);
        for (Map.Entry<Prototype, Location2DInterface> entry : builder.map.entries()) {
            addPlaceholder(entry.getKey(), entry.getValue());
        }
    }

    public void addPlaceholder(Prototype prototype, Location2DInterface location) {
        prototypes.add(prototype);
        prototypeSpace.addOccupant( Placeholder.newInstance(checkNotNull(prototype), checkNotNull(location)) );
    }

    public boolean removePlaceholder(Placeholder ph) {
        // TODO: keep prototypes in sync
        return prototypeSpace.removeOccupant(ph);
    }

    @ElementArray(name="prototypes", entry="prototype")
    private Prototype[] getPrototypesArray() {
        return Iterables.toArray(getPrototypes(), Prototype.class);
    }

    public Iterable<Prototype> getPrototypes() {
        return prototypes;
    }

    @ElementArray(name="placeholders", entry="placeholder")
    private Placeholder[] getPlaceholderArray() {
        return Iterables.toArray(getPlaceholder(), Placeholder.class);
    }

    public Iterable<Placeholder> getPlaceholder() {
        return Iterables.unmodifiableIterable(Iterables.transform(prototypeSpace.getOccupants(), new Function<Object2DInterface, Placeholder>() {
            @Override
            public Placeholder apply(Object2DInterface input) {
                return Placeholder.class.cast(input);
            }
        }));
    }

    public Iterable<Placeholder> getPlaceholder(TileLocation location) {
        return Iterables.unmodifiableIterable(Iterables.transform(prototypeSpace.getOccupants(location), new Function<Object2DInterface, Placeholder>() {
            @Override
            public Placeholder apply(Object2DInterface input) {
                return Placeholder.class.cast(input);
            }
        }));
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
        prototypes.add(prototype);
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
        return prototypeSpace; // TODO: should return an immutable view of prototypeSpace
    }

    public static Builder with() {return new Builder(); }
    public static class Builder implements BuilderInterface<Scenario> {
        private TiledSpace space;
        private Multimap<Prototype, Location2DInterface> map = ArrayListMultimap.create();
        private String name;

        public Builder name(String name) { this.name = name; return this; }
        public Builder space(int dimX, int dimY) { this.space = TiledSpace.newInstance(dimX, dimY); return this; }
        public Builder add(final Prototype clonable, Location2DInterface location2d) {
            checkNotNull(clonable);
            checkNotNull(location2d);
            map.put(clonable, checkNotNull(location2d)); return this;
        }
        @Override
        public Scenario build() {
            checkState(space != null);
            checkState(Iterables.all(map.entries(), new Predicate<Map.Entry<Prototype, Location2DInterface>>() {
                @Override
                public boolean apply(Map.Entry<Prototype, Location2DInterface> simulationObjectLocation2DEntry) {
                    return space.covers(simulationObjectLocation2DEntry.getValue());
                }
            }));
            return new Scenario(this);
        }
    }
}
