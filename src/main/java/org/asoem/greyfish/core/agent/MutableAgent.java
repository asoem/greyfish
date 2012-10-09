package org.asoem.greyfish.core.agent;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.genes.GeneComponentList;
import org.asoem.greyfish.core.genes.MutableGeneComponentList;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.simpleframework.xml.Element;

import java.util.List;

public class MutableAgent extends AbstractAgent {

    protected MutableAgent(MutableAgent mutableAgent, DeepCloner map) {
        super(mutableAgent, map);
    }

    protected MutableAgent(AbstractBuilder<?,?> builder) {
        super(new Body(),
                new MutableComponentList<AgentProperty<?>>(builder.properties),
                new MutableComponentList<AgentAction>(builder.actions),
                new MutableGeneComponentList<AgentTrait<?>>(builder.traits), null);
        setPopulation(builder.population);
    }

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private MutableAgent(@Element(name = "body") Body body,
                           @Element(name = "properties") ComponentList<AgentProperty<?>> properties,
                           @Element(name = "actions") ComponentList<AgentAction> actions,
                           @Element(name = "agentTraitList") GeneComponentList<AgentTrait<?>> agentTraitList) {
        super(body, properties, actions, agentTraitList, createDefaultActionExecutionStrategyFactory());
    }

    public MutableAgent(Agent agent) {
        super(new Body(agent.getBody()),
                new MutableComponentList<AgentProperty<?>>(agent.getProperties()),
                new MutableComponentList<AgentAction>(agent.getActions()),
                new MutableGeneComponentList<AgentTrait<?>>(agent.getTraits()), null);
        setPopulation(agent.getPopulation());
    }

    private static ActionExecutionStrategyFactory createDefaultActionExecutionStrategyFactory() {
        return new ActionExecutionStrategyFactory() {
            @Override
            public ActionExecutionStrategy createStrategy(List<? extends AgentAction> actions) {
                return new DefaultActionExecutionStrategy(actions);
            }
        };
    }

    @Override
    public String toString() {
        return "MutableAgent[" + population + "]";
    }

    @Override
    public void changeActionExecutionOrder(final AgentAction object, final AgentAction object2) {
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
