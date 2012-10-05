package org.asoem.greyfish.core.simulation;

import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.space.Tile;
import org.asoem.greyfish.core.space.WalledTileSpace;
import org.asoem.greyfish.utils.space.Object2D;

import java.util.Set;

/**
 * User: christoph
 * Date: 07.10.11
 * Time: 12:41
 */
public abstract class ForwardingSimulationTemplate extends ForwardingObject implements SimulationTemplate {

    @Override
    protected abstract SimulationTemplate delegate();

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
    public <T extends Simulation> T createSimulation(SimulationFactory<T> simulationFactory) {
        return delegate().createSimulation(simulationFactory);
    }

    @Override
    public WalledTileSpace<Agent> getSpace() {
        return delegate().getSpace();
    }
}
