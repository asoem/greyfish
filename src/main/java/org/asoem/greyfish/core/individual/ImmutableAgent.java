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

    private final Population population;

    private final ComponentList<GFProperty> properties;

    private final ComponentList<GFAction> actions;

    private final MutableGenome genome;

    private final Body body;

    private final SimulationContext simulationContext;

    private ImmutableAgent(Agent agent, Simulation simulation) {
        super(body, properties, actions, genome);
        Agent clone = agent.deepClone(Agent.class);

        this.population = clone.getPopulation();
        this.properties = ImmutableComponentList.copyOf(clone.getProperties(), this);
        this.actions = ImmutableComponentList.copyOf(clone.getActions(), this);
        this.genome = new MutableGenome(clone.getGenome(), this);
        this.body = clone.getBody();
        this.simulationContext = new SimulationContext(simulation, this);
    }


    private ImmutableAgent(Population population, Iterable<? extends GFProperty> properties, Iterable<? extends GFAction> actions, Iterable<? extends Gene<?>> genes, Body body, Simulation simulation) {
        super(body, new ImmutableComponentList<GFProperty>() {
            @Override
            protected Agent getAgent() {
                return ImmutableAgent.this;
            }
        });
        setPopulation(population);
        this.population = population;
        this.properties = ImmutableComponentList.copyOf(properties, this);
        this.actions = ImmutableComponentList.copyOf(actions, this);
        this.genome = new MutableGenome(genes, this);
        this.body = body;
        this.simulationContext = new SimulationContext(simulation, this);
    }

    @SuppressWarnings("unchecked")
    protected ImmutableAgent(ImmutableAgent agent, CloneMap map) {
        super(body, properties, actions, genome);
        this(
                agent.population,
                map.cloneAll(agent.getProperties(), GFProperty.class),
                map.cloneAll(agent.getActions(), GFAction.class),
                (Iterable<Gene<?>>) (Iterable<?>) map.cloneAll(agent.getGenome(), Gene.class),
                map.clone(agent.getBody(), Body.class),
                agent.getSimulation()
        );
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
