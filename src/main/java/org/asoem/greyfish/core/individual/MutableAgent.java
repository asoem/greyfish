package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.MutableGenome;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.DeepCloner;
import org.simpleframework.xml.Root;

@Root
public class MutableAgent extends AbstractAgent {

    public MutableAgent() {
        this(new Builder());
    }

    protected MutableAgent(MutableAgent mutableAgent, DeepCloner map) {
        super(mutableAgent, map);
    }

    protected MutableAgent(AbstractBuilder<?> builder) {
        super(new Body(),
                new MutableComponentList<GFProperty>(builder.properties),
                new MutableComponentList<GFAction>(builder.actions),
                new MutableGenome(builder.genes));
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

    public static Builder with() {
        return new Builder();
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<MutableAgent> {
        @Override
        public MutableAgent build() {
            return new MutableAgent(checkedSelf());
        }
        @Override
        protected Builder self() {
            return this;
        }
    }
}
