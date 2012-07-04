package org.asoem.greyfish.core.scenario;

import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.space.Tile;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Set;

/**
 * User: christoph
 * Date: 07.10.11
 * Time: 12:41
 */
public abstract class ForwardingScenario extends ForwardingObject implements Scenario {

    @Override
    protected abstract Scenario delegate();

    @Override
    public boolean addAgent(Agent prototype, Object2D projection) {
        return delegate().addAgent(prototype, projection);
    }

    @Override
    public boolean removePlaceholder(Agent placeholder) {
        return delegate().removePlaceholder(placeholder);
    }

    @Override
    public Set<Agent> getPrototypes() {
        return delegate().getPrototypes();
    }

    @Override
    public Iterable<Agent> getPlaceholder() {
        return delegate().getPlaceholder();
    }

    @Override
    public Iterable<Agent> getPlaceholder(Tile location) {
        return delegate().getPlaceholder(location);
    }

    @Override
    public Iterable<Agent> getPlaceholder(Iterable<? extends Tile> locations) {
        return delegate().getPlaceholder(locations);
    }

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public void setName(String name) {
        delegate().setName(name);
    }

    @Override
    public TiledSpace<Agent> getSpace() {
        return delegate().getSpace();
    }
}
