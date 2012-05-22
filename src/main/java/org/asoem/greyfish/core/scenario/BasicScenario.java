package org.asoem.greyfish.core.scenario;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.space.Tile;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Root(name="scenario")
public class BasicScenario implements Scenario {

    @Element(name="space")
    private final TiledSpace<Agent> space;

    @ElementList(name = "prototypes", entry = "prototype")
    private final Set<Agent> prototypes = Sets.newHashSet();

    @Attribute(name="name")
    private String name;

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private BasicScenario(
            @ElementList(name = "prototypes", entry = "prototype") Set<Agent> prototypes,
            @Element(name = "space") TiledSpace<Agent> space) {
        assert prototypes != null;
        assert space != null;

        this.space = space;
        this.prototypes.addAll(prototypes);
    }

    @SuppressWarnings("UnusedDeclaration")
    public BasicScenario(Scenario scenario) {
        this.name = scenario.getName();
        this.space = new TiledSpace<Agent>(scenario.getSpace());
        for (Agent agent : scenario.getPlaceholder()) {
            addAgent(agent, agent.getProjection());
        }
    }
    
    private BasicScenario(Builder builder) {
        this.name = builder.name;
        this.space = builder.space;
        for (Builder.AgentProjectionPair entry : builder.projections) {
            addAgent(entry.projectable, entry.projection);
        }
    }

    @Override
    public boolean addAgent(Agent prototype, Object2D object2D) {
        checkNotNull(prototype);
        checkNotNull(object2D);

        addAsPrototypeIfUnknown(prototype);

        space.addObject(prototype, object2D);
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
        return space.removeObject(placeholder);
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
        return space.getObjects();
    }

    @Override
    public Iterable<Agent> getPlaceholder(Tile location) {
        return space.getObjects(Collections.singleton(location));
    }

    @Override
    public Iterable<Agent> getPlaceholder(final Iterable<? extends Tile> locations) {
        return space.getObjects(locations);
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
    public TiledSpace<Agent> getSpace() {
        return space; // TODO: should return an immutable view of space
    }

    /**
     * Returns a new builder. The generated builder is equivalent to the builder created by the {@link org.asoem.greyfish.core.scenario.BasicScenario.Builder} constructor.
     * @param name the name of this {@code Scenario}
     * @param space the {@code TiledSpace} for this {@code Scenario}
     * @return a new {@link org.asoem.greyfish.core.scenario.BasicScenario.Builder} instance
     */
    public static Builder builder(String name, TiledSpace<Agent> space) {
        return new Builder(name, space);
    }

    public static class Builder implements org.asoem.greyfish.utils.base.Builder<BasicScenario> {
        private final TiledSpace<Agent> space;
        private final List<AgentProjectionPair> projections = Lists.newArrayList();
        private final String name;

        /**
         * Creates a new builder. The returned builder is equivalent to the builder generated by {@linkplain BasicScenario#builder(String, org.asoem.greyfish.core.space.TiledSpace)}.
         * @param name the name of this {@code Scenario}
         * @param space the {@code TiledSpace} for this {@code Scenario}
         */
        public Builder(String name, TiledSpace<Agent> space) {
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
        public Builder addAgent(final Agent prototype, Object2D object2D) {
            checkNotNull(prototype);
            checkNotNull(object2D);
            checkArgument(space.contains(object2D), "object2D " + object2D + " is out of the space range " + space);
            projections.add(new AgentProjectionPair(prototype, object2D));
            return this;
        }

        /**
         * Returns a newly-created {@code Scenario} based on the contents of the {@code Builder}.
         */
        @Override
        public BasicScenario build() {
            checkState(Iterables.all(projections, new Predicate<AgentProjectionPair>() {
                @Override
                public boolean apply(AgentProjectionPair object2D) {
                    return space.contains(object2D.projection);
                }
            }), "Scenario cannot be built: Projection of at least one agent is out of the space range");
            return new BasicScenario(this);
        }
        
        private static class AgentProjectionPair {
            private final Agent projectable;
            private final Object2D projection;

            private AgentProjectionPair(Agent projectable, Object2D projection) {
                this.projection = projection;
                this.projectable = projectable;
            }
        }
    }

    @Override
    public String toString() {
        return "Scenario['" + name + "']";
    }
}
