package org.asoem.greyfish.core.scenario;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Placeholder;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.core.space.Object2D;
import org.asoem.greyfish.core.space.TileLocation;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.Root;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Root(name="scenario")
public class BasicScenario implements Scenario {

    @Element(name="space")
    private final TiledSpace prototypeSpace;

    @Attribute(name="name")
    private final String name;

    private final Set<Agent> prototypes = Sets.newHashSet();

    @SimpleXMLConstructor
    private BasicScenario(
            @Attribute(name = "name") String name,
            @ElementArray(name = "prototypes", entry = "prototype") Agent[] prototypes,
            @Element(name = "space") TiledSpace space,
            @ElementArray(name = "placeholders", entry = "placeholder") Placeholder[] placeholders) {
        assert name != null;
        assert prototypes != null;
        assert space != null;
        assert placeholders != null;

        this.name = name;
        this.prototypeSpace = space;
        this.prototypes.addAll(Arrays.asList(prototypes));
        for (Agent placeholder : placeholders) {
            prototypeSpace.addOccupant(placeholder);
        }
    }

    private BasicScenario(Builder builder) {
        this.name = builder.name;
        this.prototypeSpace = TiledSpace.copyOf(builder.space);
        for (Map.Entry<Agent, Object2D> entry : builder.map.entries()) {
            addAgent(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean addAgent(Agent prototype, Object2D location) {
        checkNotNull(prototype);
        checkNotNull(location);

        if (!prototypes.contains(prototype))
            if (!prototypes.add(prototype))
                return false;

        prototypeSpace.addOccupant(Placeholder.newInstance(prototype, location));
        return true;
    }

    @Override
    public boolean removePlaceholder(Placeholder ph) {
        // TODO: keep prototypes in sync
        return prototypeSpace.removeOccupant(ph);
    }

    @ElementArray(name="prototypes", entry="prototype")
    private Agent[] getPrototypesArray() {
        return Iterables.toArray(getPrototypes(), Agent.class);
    }

    @Override
    public Set<Agent> getPrototypes() {
        return Collections.unmodifiableSet(prototypes);
    }

    @ElementArray(name="placeholders", entry="placeholder")
    private Placeholder[] getPlaceholderArray() {
        return Iterables.toArray(getPlaceholder(), Placeholder.class);
    }

    @Override
    public Iterable<Placeholder> getPlaceholder() {
        return Iterables.unmodifiableIterable(Iterables.transform(prototypeSpace.getOccupants(),
                new Function<MovingObject2D, Placeholder>() {
                    @Override
                    public Placeholder apply(MovingObject2D input) {
                        return Placeholder.class.cast(input);
                    }
                }));
    }

    @Override
    public Iterable<Placeholder> getPlaceholder(TileLocation location) {
        return Iterables.filter(prototypeSpace.getOccupants(location), Placeholder.class);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TiledSpace getSpace() {
        return prototypeSpace; // TODO: should return an immutable view of prototypeSpace
    }

    /**
     * Returns a new builder. The generated builder is equivalent to the builder created by the {@link BasicScenario.Builder} constructor.
     * @param name the name of this {@code Scenario}
     * @param space the {@code TiledSpace} for this {@code Scenario}
     * @return a new {@link Builder} instance
     */
    public static Builder builder(String name, TiledSpace space) {
        return new Builder(name, space);
    }

    public static class Builder implements BuilderInterface<Scenario> {
        private TiledSpace space;
        private final Multimap<Agent, Object2D> map = ArrayListMultimap.create();
        private String name;

        /**
         * Creates a new builder. The returned builder is equivalent to the builder generated by {@linkplain BasicScenario#builder(String, TiledSpace)}.
         * @param name the name of this {@code Scenario}
         * @param space the {@code TiledSpace} for this {@code Scenario}
         */
        public Builder(String name, TiledSpace space) {
            this.space = space;
            this.name = name;
        }

        /**
         * Create and add a {@link Placeholder} of the given {@code prototype}
         * represented by the given {@code object2D} to the {@code Scenario}.
         * @param prototype the prototype
         * @param object2D an {@code Object2D} to be placed in the {@code TiledSpace} defined in the constructor of this {@code Builder}
         * @return this {@code Builder} object
         */
        public Builder addAgent(final Agent prototype, Object2D object2D) {
            checkNotNull(prototype);
            checkNotNull(object2D);
            map.put(prototype, object2D);
            return this;
        }

        /**
         * Returns a newly-created {@code Scenario} based on the contents of the {@code Builder}.
         */
        @Override
        public Scenario build() {
            checkState(space != null, "Builder cannot build: A space is required");
            checkState(Iterables.all(map.values(), new Predicate<Object2D>() {
                @Override
                public boolean apply(Object2D object2D) {
                    return space.covers(object2D.getCoordinates());
                }
            }));
            return new BasicScenario(this);
        }
    }

    @Override
    public String toString() {
        return "Scenario['" + name + "']";
    }
}
