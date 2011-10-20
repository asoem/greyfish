package org.asoem.greyfish.core.scenario;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Placeholder;
import org.asoem.greyfish.core.space.TileLocation;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Set;

/**
 * User: christoph
 * Date: 07.10.11
 * Time: 12:41
 */
public abstract class ScenarioAdaptor implements Scenario {

    protected abstract Scenario delegate();

    @Override
    public boolean addAgent(Agent prototype, Object2D location) {
        return delegate().addAgent(prototype, location);
    }

    @Override
    public boolean removePlaceholder(Placeholder ph) {
        return delegate().removePlaceholder(ph);
    }

    @Override
    public Set<Agent> getPrototypes() {
        return delegate().getPrototypes();
    }

    @Override
    public Iterable<Placeholder> getPlaceholder() {
        return delegate().getPlaceholder();
    }

    @Override
    public Iterable<Placeholder> getPlaceholder(TileLocation location) {
        return delegate().getPlaceholder(location);
    }

    @Override
    public Iterable<Placeholder> getPlaceholder(Iterable<? extends TileLocation> locations) {
        return delegate().getPlaceholder(locations);
    }

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public TiledSpace getSpace() {
        return delegate().getSpace();
    }
}
