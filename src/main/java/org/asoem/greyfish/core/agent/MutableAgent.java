package org.asoem.greyfish.core.agent;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.List;

public class MutableAgent extends AbstractAgent {

    private MutableAgent(MutableAgent mutableAgent, DeepCloner map) {
        super(mutableAgent, map);
    }

    private MutableAgent(Builder builder) {
        super(builder);
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
            super(population, createDefaultInitializationFactory());
        }

        @Override
        protected MutableAgent checkedBuild() {
            return new MutableAgent(this);
        }
        @Override
        protected Builder self() {
            return this;
        }

        private static AgentInitializationFactory createDefaultInitializationFactory() {
            return new AgentInitializationFactory() {
                @Override
                public ActionExecutionStrategy createStrategy(List<? extends AgentAction> actions) {
                    return new DefaultActionExecutionStrategy(actions);
                }

                @Override
                public <T extends AgentComponent> ComponentList<T> createComponentList(Iterable<T> elements) {
                    return MutableComponentList.copyOf(elements);
                }

                @Override
                public AgentMessageBox createMessageBox() {
                    return new FixedSizeMessageBox();
                }
            };
        }
    }
}
