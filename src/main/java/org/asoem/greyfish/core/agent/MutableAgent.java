package org.asoem.greyfish.core.agent;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.simpleframework.xml.Element;

public class MutableAgent extends AbstractAgent {

    private MutableAgent(MutableAgent mutableAgent, DeepCloner map) {
        super(mutableAgent, map);
    }

    private MutableAgent(Builder builder) {
        super(
                MutableComponentList.copyOf(builder.properties),
                MutableComponentList.copyOf(builder.actions),
                MutableComponentList.copyOf(builder.traits),
                builder.agentInitializationFactory);
        setPopulation(builder.population);
    }

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private MutableAgent(@Element(name = "properties") ComponentList<AgentProperty<?>> properties,
                         @Element(name = "actions") ComponentList<AgentAction> actions,
                         @Element(name = "traits") ComponentList<AgentTrait<?>> agentTraitList,
                         AgentInitializationFactory factory) {
        super(properties, actions, agentTraitList, factory);
    }

    @Override
    public String toString() {
        return "MutableAgent[" + getPopulation() + "]";
    }

    @Override
    public void changeActionExecutionOrder(final AgentAction object, final AgentAction object2) {
        Preconditions.checkNotNull(object);
        Preconditions.checkNotNull(object2);
        if ( ! getActions().contains(object) || ! getActions().contains(object2))
            throw new IllegalArgumentException();
        int index1 = getActions().indexOf(object);
        int index2 = getActions().indexOf(object2);
        getActions().add(index2, getActions().remove(index1));
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MutableAgent(this, cloner);
    }

    public static Builder builder(Population population) {
        return new Builder(population);
    }

    public static class Builder extends AbstractBuilder<MutableAgent, Builder> {
        private Builder(Population population) {
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
