package org.asoem.greyfish.core.simulation;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.Avatar;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.space.WalledTileSpace;
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.collect.Product2;
import org.asoem.greyfish.utils.collect.Tuple2;
import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Point2D;
import org.asoem.greyfish.utils.space.Tile;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.*;

@Root(name="scenario")
public class BasicSimulationTemplate implements SimulationTemplate {

    @Element(name="space")
    private final WalledTileSpace<Agent> space;

    @ElementList(name = "prototypes", entry = "prototype")
    private final Set<Agent> prototypes = Sets.newHashSet();

    @Attribute(name="name")
    private String name;

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private BasicSimulationTemplate(
            @ElementList(name = "prototypes", entry = "prototype") Set<Agent> prototypes,
            @Element(name = "space") WalledTileSpace<Agent> space) {
        assert prototypes != null;
        assert space != null;

        this.space = space;
        this.prototypes.addAll(prototypes);
    }

    @SuppressWarnings("UnusedDeclaration")
    public BasicSimulationTemplate(SimulationTemplate simulationTemplate) {
        this.name = simulationTemplate.getName();
        this.space = new WalledTileSpace<Agent>(simulationTemplate.getSpace());
        for (Agent agent : simulationTemplate.getPlaceholder()) {
            addAgent(agent, agent.getProjection());
        }
    }
    
    private BasicSimulationTemplate(Builder builder) {
        this.name = builder.name;
        this.space = builder.space;
        for (Product2<? extends Agent, ? extends Object2D> entry : builder.projections) {
            addAgent(entry._1(), entry._2());
        }
    }

    @Override
    public boolean addAgent(Agent prototype, Object2D projection) {
        checkNotNull(prototype);
        checkNotNull(projection);

        addAsPrototypeIfUnknown(prototype);

        final Point2D anchorPoint = projection.getAnchorPoint();
        space.insertObject(new Avatar(prototype), anchorPoint.getX(), anchorPoint.getY(), projection.getOrientationAngle());
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
    public <T extends Simulation> T createSimulation(SimulationFactory<T> simulationFactory) {
        checkNotNull(simulationFactory);
        final WalledTileSpace<Agent> space = WalledTileSpace.copyOf(this.space);
        final T simulation = simulationFactory.createSimulation(space, prototypes);
        simulation.setName(getName());

        for (final Agent agent : getPlaceholder()) {
            simulation.createAgent(agent.getPopulation(), new Initializer<Agent>() {
                @Override
                public void initialize(Agent clone) {
                     clone.setProjection(agent.getProjection());
                }
            });
        }

        return simulation;
    }

    @Override
    public WalledTileSpace<Agent> getSpace() {
        return space; // TODO: should return an immutable view of space
    }

    /**
     * Returns a new builder. The generated builder is equivalent to the builder created by the {@link BasicSimulationTemplate.Builder} constructor.
     *
     * @param name the name of this {@code SimulationTemplate}
     * @param space the {@code TiledSpace} for this {@code SimulationTemplate}
     * @return a new {@link BasicSimulationTemplate.Builder} instance
     */
    public static Builder builder(String name, WalledTileSpace<Agent> space) {
        return new Builder(name, space);
    }

    public static class Builder implements org.asoem.greyfish.utils.base.Builder<BasicSimulationTemplate> {
        private final WalledTileSpace<Agent> space;
        private final List<Product2<? extends Agent, ? extends Object2D>> projections = Lists.newArrayList();
        private final String name;

        /**
         * Creates a new builder. The returned builder is equivalent to the builder generated by {@linkplain BasicSimulationTemplate#builder(String, org.asoem.greyfish.core.space.WalledTileSpace}.
         * @param name the name of this {@code SimulationTemplate}
         * @param space the {@code TiledSpace} for this {@code SimulationTemplate}
         */
        public Builder(String name, WalledTileSpace<Agent> space) {
            this.space = checkNotNull(space);
            this.name = checkNotNull(name);
        }

        /**
         * Create and createChildNode a {@link Agent} of the given {@code prototype}
         * represented by the given {@code object2D} to the {@code SimulationTemplate}.
         * @param prototype the prototype
         * @param object2D an {@code Object2D} to be placed in the {@code TiledSpace} defined in the constructor of this {@code Builder}
         * @return this {@code Builder} object
         */
        public Builder addAgent(final Agent prototype, Object2D object2D) {
            checkNotNull(prototype);
            checkNotNull(object2D);
            final Point2D anchorPoint = object2D.getAnchorPoint();
            checkArgument(space.contains(anchorPoint.getX(), anchorPoint.getY()), "object2D " + object2D + " is out of the space range " + space);
            projections.add(Tuple2.of(prototype, object2D));
            return this;
        }

        public Builder addAgents(Iterable<? extends Tuple2<? extends Agent, ? extends Object2D>> agents) {
            checkNotNull(agents);
            Iterables.addAll(projections, agents);
            return this;
        }

        /**
         * Returns a newly-created {@code SimulationTemplate} based on the contents of the {@code Builder}.
         */
        @Override
        public BasicSimulationTemplate build() {
            checkState(Iterables.all(projections, new Predicate<Product2<? extends Agent, ? extends Object2D>>() {
                @Override
                public boolean apply(Product2<? extends Agent, ? extends Object2D> object2D) {
                    final Object2D projection = object2D._2();
                    final Point2D anchorPoint = projection.getAnchorPoint();
                    return space.contains(anchorPoint.getX(), anchorPoint.getY());
                }
            }), "SimulationTemplate cannot be built: Projection of at least one agent is out of the space range");
            return new BasicSimulationTemplate(this);
        }
    }

    @Override
    public String toString() {
        return "SimulationTemplate['" + name + "']";
    }
}
