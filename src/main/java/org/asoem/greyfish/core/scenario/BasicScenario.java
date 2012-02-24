package org.asoem.greyfish.core.scenario;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.space.TileLocation;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Root(name="scenario")
public class BasicScenario implements Scenario {

    @Element(name="space")
    private final TiledSpace prototypeSpace;

    @ElementList(name = "prototypes", entry = "prototype")
    private final Set<Agent> prototypes = Sets.newHashSet();

    @Attribute(name="name")
    private String name;

    @SimpleXMLConstructor
    private BasicScenario(
            @ElementList(name = "prototypes", entry = "prototype") Set<Agent> prototypes,
            @Element(name = "space") TiledSpace space) {
        assert prototypes != null;
        assert space != null;

        this.prototypeSpace = space;
        this.prototypes.addAll(prototypes);
    }

    public BasicScenario(Scenario scenario) {
        this.name = scenario.getName();
        this.prototypeSpace = new TiledSpace(scenario.getSpace());
        for (Map.Entry<Object2D, Agent> entry : Maps.uniqueIndex(scenario.getPlaceholder(), scenario.getSpace()).entrySet()) {
            addAgent(entry.getValue(), entry.getKey());
        }
    }
    
    private BasicScenario(Builder builder) {
        this.name = builder.name;
        this.prototypeSpace = builder.space;
        for (Map.Entry<Agent, Object2D> entry : builder.map.entries()) {
            addAgent(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean addAgent(Agent prototype, Object2D location) {
        checkNotNull(prototype);
        checkNotNull(location);

        addAsPrototypeIfUnknown(prototype);

        prototypeSpace.addObject(prototype, location);
        return true;
    }

    private void addAsPrototypeIfUnknown(final Agent prototype) {
        if (Iterables.find(prototypes, new Predicate<Agent>() {
            @Override
            public boolean apply(@Nullable Agent agent) {
                assert agent != null;
                return agent.getPopulation().equals(prototype.getPopulation());
            }
        }, null) == null) {
            prototypes.add(prototype);
        }
    }

    @Override
    public boolean removePlaceholder(Agent placeholder) {
        return prototypeSpace.removeObject(placeholder);
    }

    @Override
    public Set<Agent> getPrototypes() {
        return Collections.unmodifiableSet(prototypes);
    }

    @Override
    @Nullable
    public Agent getPrototype(final Population population) { // TODO: would be faster if prototypes is a BiMap
        return Iterables.find(prototypes, new Predicate<Agent>() {
            @Override
            public boolean apply(Agent agent) {
                return agent.getPopulation().equals(population);
            }
        });
    }

    @Override
    public Iterable<Agent> getPlaceholder() {
        return Iterables.filter(prototypeSpace.getOccupants(), Agent.class);
    }

    @Override
    public Iterable<Agent> getPlaceholder(TileLocation location) {
        return Iterables.filter(prototypeSpace.getOccupants(location), Agent.class);
    }

    @Override
    public Iterable<Agent> getPlaceholder(final Iterable<? extends TileLocation> locations) {
        return Iterables.filter(getPlaceholder(), new Predicate<Agent>() {
            @Override
            public boolean apply(final Agent placeholder) {
                return Iterables.any(locations, new Predicate<TileLocation>() {
                    @Override
                    public boolean apply(TileLocation o) {
                        return o.covers(prototypeSpace.getCoordinates(placeholder));
                    }
                });
            }
        });
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public TiledSpace getSpace() {
        return prototypeSpace; // TODO: should return an immutable view of prototypeSpace
    }

    /**
     * Returns a new builder. The generated builder is equivalent to the builder created by the {@link org.asoem.greyfish.core.scenario.BasicScenario.Builder} constructor.
     * @param name the name of this {@code Scenario}
     * @param space the {@code TiledSpace} for this {@code Scenario}
     * @return a new {@link org.asoem.greyfish.core.scenario.BasicScenario.Builder} instance
     */
    public static Builder builder(String name, TiledSpace space) {
        return new Builder(name, space);
    }

    public static class Builder implements org.asoem.greyfish.utils.base.Builder<BasicScenario> {
        private final TiledSpace space;
        private final Multimap<Agent, Object2D> map = ArrayListMultimap.create();
        private final String name;

        /**
         * Creates a new builder. The returned builder is equivalent to the builder generated by {@linkplain BasicScenario#builder(String, TiledSpace)}.
         * @param name the name of this {@code Scenario}
         * @param space the {@code TiledSpace} for this {@code Scenario}
         */
        public Builder(String name, TiledSpace space) {
            this.space = checkNotNull(space);
            this.name = checkNotNull(name);
        }

        /**
         * Create and createChildNode a {@link Agent} of the given {@code prototype}
         * represented by the given {@code object2D} to the {@code Scenario}.
         * @param prototype the prototype
         * @param object2D an {@code Object2D} to be placed in the {@code TiledSpace} defined in the constructor of this {@code Builder}
         * @return this {@code Builder} object
         */
        public Builder putAgent(final Agent prototype, Object2D object2D) {
            checkNotNull(prototype);
            checkNotNull(object2D);
            map.put(prototype, object2D);
            return this;
        }

        /**
         * Returns a newly-created {@code Scenario} based on the contents of the {@code Builder}.
         */
        @Override
        public BasicScenario build() {
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
