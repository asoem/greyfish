package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.genes.MutableGenome;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;


/**
 * User: christoph
 * Date: 19.09.11
 * Time: 10:42
 */
public class ImmutableAgent extends AbstractAgent {

    private ImmutableAgent(Agent agent, Simulation simulation) {
        super(agent);
    }

    private ImmutableAgent(Population population, Iterable<? extends GFProperty> properties, Iterable<? extends GFAction> actions, Iterable<? extends Gene<?>> genes, Body body, Simulation simulation) {
        super(body, ImmutableComponentList.copyOf(properties), ImmutableComponentList.copyOf(actions), new MutableGenome(genes));
        setPopulation(population);
        this.simulationContext = new SimulationContext(simulation, this);
    }

    @SuppressWarnings("unchecked")
    protected ImmutableAgent(ImmutableAgent agent, CloneMap map) {
        super(agent, map);
    }

    @Override
    protected SimulationContext getSimulationContext() {
        return simulationContext;
    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public ComponentList<GFAction> getActions() {
        return actions;
    }

    @Override
    public ComponentList<GFProperty> getProperties() {
        return properties;
    }

    @Override
    public Genome getGenome() {
        return genome;
    }

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new ImmutableAgent(this, map);
    }
}
