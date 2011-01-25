package org.asoem.greyfish.core.scenario;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.individual.PrototypeManager;
import org.asoem.greyfish.core.individual.PrototypeRegistryListener;
import org.asoem.greyfish.core.space.Object2DInterface;
import org.asoem.greyfish.core.space.Placeholder;
import org.asoem.greyfish.core.space.TileLocation;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.DeepClonable;
import org.asoem.greyfish.utils.ListenerSupport;
import org.simpleframework.xml.*;

import java.util.Collection;

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

    public Scenario(Builder builder) {
        this.prototypeSpace = builder.space;
        for (Placeholder placeholder : builder.placeholderBuilder.build()) {
            addPlaceholder(placeholder);
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

    public static Builder with() {return new Builder(); }
    public static class Builder implements BuilderInterface<Scenario> {
        private TiledSpace space;
        private ImmutableList.Builder<Placeholder> placeholderBuilder = ImmutableList.builder();

        public Builder space(TiledSpace space) { this.space = checkNotNull(space); return this; }
        public Builder addPlaceholder(Placeholder placeholder) { this.placeholderBuilder.add(checkNotNull(placeholder)); return this; }

        @Override
        public Scenario build() {
            checkState(space != null);
            return new Scenario(this);
        }
    }
}
