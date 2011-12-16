package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.ForwardingGene;
import org.asoem.greyfish.core.genes.MutableGenome;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.simpleframework.xml.Root;

@Root
public class MutableAgent extends AbstractAgent {

    public MutableAgent() {
        super(new Body(),
                new MutableComponentList<GFProperty>(),
                new MutableComponentList<GFAction>(),
                new MutableGenome<ForwardingGene<?>>());
    }

    protected MutableAgent(MutableAgent mutableAgent, DeepCloner map) {
        super(mutableAgent, map);
    }

    protected MutableAgent(AbstractBuilder<?,?> builder) {
        super(new Body(),
                new MutableComponentList<GFProperty>(builder.properties),
                new MutableComponentList<GFAction>(builder.actions),
                new MutableGenome<ForwardingGene<?>>(builder.genes));
        setPopulation(builder.population);
    }

    @Override
    public String toString() {
        return "MutableAgent[" + population + "]";
    }

    @Override
    public void changeActionExecutionOrder(final GFAction object, final GFAction object2) {
        Preconditions.checkNotNull(object);
        Preconditions.checkNotNull(object2);
        if ( ! actions.contains(object) || ! actions.contains(object2))
            throw new IllegalArgumentException();
        int index1 = actions.indexOf(object);
        int index2 = actions.indexOf(object2);
        actions.add(index2, actions.remove(index1));
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public void prepare(Simulation simulation) {
        Preconditions.checkNotNull(simulation);

        this.simulationContext = new SimulationContext(simulation, this);

        for (AgentComponent component : getComponents()) {
            component.prepare(simulation);
        }
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MutableAgent(this, cloner);
    }

    public static Builder with(Population population) {
        return new Builder(population);
    }

    public static final class Builder extends AbstractBuilder<MutableAgent, Builder> {
        protected Builder(Population population) {
            super(population);
        }

        @Override
        public MutableAgent checkedBuild() {
            return new MutableAgent(this);
        }
        @Override
        protected Builder self() {
            return this;
        }
    }
}
