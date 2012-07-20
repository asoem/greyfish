package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.simulation.SimulationFactory;
import org.asoem.greyfish.core.space.Tile;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.space.Object2D;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * User: christoph
 * Date: 07.10.11
 * Time: 12:39
 */
public interface SimulationTemplate {

    /**
     * Add a {@code Placeholder} for the given {@code Agent} and place it at the given {@code projection}
     * @param prototype the agent the created {@code Placeholder} will delegate to
     * @param projection the projection at which the created {@code Placeholder} will be located at
     * @return {@code true} if the {@code Placeholder} could be created, {@code false} otherwise
     */
    boolean addAgent(Agent prototype, Object2D projection);

    /**
     * Remove the given {@code Placeholder} from this scenario
     *
     * @param placeholder the {@code Placeholder} to remove
     * @return {@code true} if the {@code Placeholder} could be removed, {@code false} otherwise
     */
    boolean removePlaceholder(Agent placeholder);

    /**
     * @return an unmodifiable view of evaluates prototype agents added to this scenario so far.
     * All elements are guaranteed to be non null;
     */
    Set<Agent> getPrototypes();

    /**
     * Get the {@code Agent} which acts as the prototype for the given {@code Population}
     * @param population the population of the prototype
     * @return the prototype for for the given {@code Population}
     */
    @Nullable
    Agent getPrototype(Population population);

    /**
     * @return an unmodifiable view of the (non-null) {@link org.asoem.greyfish.core.individual.Agent} agents
     * created by this {@code SimulationTemplate}.
     */
    Iterable<Agent> getPlaceholder();

    /**
     * Get evaluates placeholders which have been placed inside the given {@code location}
     *
     * @param location the location to which the search will be restricted to
     * @return evaluates {@code Placeholder} for given {@code location}
     */
    Iterable<Agent> getPlaceholder(Tile location);

    /**
     * Get evaluates placeholders which have been placed inside the given {@code locations}
     *
     * @param locations the locations to which the search will be restricted to
     * @return evaluates {@code Placeholder} for given {@code locations}
     */
    Iterable<Agent> getPlaceholder(Iterable<? extends Tile> locations);

    /**
     * Get the name of this scenario
     * @return the name of this scenario
     */
    String getName();

    /**
     * Get the {@code TiledSpace} associated with this scenario
     * @return the {@code TiledSpace} associated with this scenario
     */
    TiledSpace<Agent> getSpace();

    /**
     * Change the name of this scenario
     * @param name the new name of this scenario
     */
    void setName(String name);

    <T extends Simulation> T createSimulation(SimulationFactory<T> simulationFactory);
}
