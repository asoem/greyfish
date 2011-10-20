package org.asoem.greyfish.core.scenario;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Placeholder;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.space.TileLocation;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Set;

/**
 * User: christoph
 * Date: 07.10.11
 * Time: 12:39
 */
public interface Scenario {
    /**
     *
     * @param prototype
     * @param location
     */
    boolean addAgent(Agent prototype, Object2D location);

    boolean removePlaceholder(Placeholder ph);

    /**
     * @return an unmodifiable view of all prototype agents added to this scenario so far.
     * All elements are guaranteed to be non null;
     */
    Set<Agent> getPrototypes();

    Agent getPrototype(Population population);

    /**
     * @return an unmodifiable view of the (non-null) {@link org.asoem.greyfish.core.individual.Placeholder} agents created by this {@code Scenario}.
     */
    Iterable<Placeholder> getPlaceholder();

    /**
     * @param location the {@code TileLocation} to query
     * @return an unmodifiable view of all {@link org.asoem.greyfish.core.individual.Placeholder} agents created by this {@code Scenario} whose
     *      {@link org.asoem.greyfish.core.individual.Agent#getCoordinates()} is covered by the given {@code location}.
     */
    Iterable<Placeholder> getPlaceholder(TileLocation location);

    Iterable<Placeholder> getPlaceholder(Iterable<? extends TileLocation> locations);

    String getName();

    TiledSpace getSpace();
}
