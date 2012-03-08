package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.MutableChromosome;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.simpleframework.xml.Element;

public class MutableAgent extends AbstractAgent {

    protected MutableAgent(MutableAgent mutableAgent, DeepCloner map) {
        super(mutableAgent, map);
    }

    protected MutableAgent(AbstractBuilder<?,?> builder) {
        super(new Body(),
                new MutableComponentList<GFProperty>(builder.properties),
                new MutableComponentList<GFAction>(builder.actions),
                new MutableChromosome<Gene<?>>(builder.genes));
        setPopulation(builder.population);
    }

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private MutableAgent(@Element(name = "body") Body body,
                           @Element(name = "properties") ComponentList<GFProperty> properties,
                           @Element(name = "actions") ComponentList<GFAction> actions,
                           @Element(name = "chromosome") Chromosome<Gene<?>> chromosome) {
        super(body, properties, actions, chromosome);
    }

    public MutableAgent(Agent agent) {
        super(new Body(agent.getBody()),
                new MutableComponentList<GFProperty>(agent.getProperties()),
                new MutableComponentList<GFAction>(agent.getActions()),
                new MutableChromosome<Gene<?>>(agent.getChromosome()));
        setPopulation(agent.getPopulation());
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
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MutableAgent(this, cloner);
    }

    public static Builder of(Population population) {
        return new Builder(population);
    }

    public static final class Builder extends AbstractBuilder<MutableAgent, Builder> {
        protected Builder(Population population) {
            super(population);
        }

        @Override
        protected MutableAgent checkedBuild() {
            return new MutableAgent(this);
        }
        @Override
        protected Builder self() {
            return this;
        }
    }
}
