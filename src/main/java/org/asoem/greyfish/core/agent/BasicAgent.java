package org.asoem.greyfish.core.agent;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.AugmentedLists;
import org.asoem.greyfish.utils.collect.SearchableList;

import java.util.List;

public class BasicAgent extends AbstractAgent {

    private BasicAgent(BasicAgent basicAgent, DeepCloner map) {
        super(basicAgent, map, INITIALIZATION_FACTORY);
    }

    private BasicAgent(Builder builder) {
        super(builder, INITIALIZATION_FACTORY);
    }

    @Override
    public String toString() {
        return "BasicAgent[" + getPopulation() + "]";
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
        return new BasicAgent(this, cloner);
    }

    public static Builder builder(Population population) {
        return new Builder(population);
    }

    public static class Builder extends AbstractBuilder<BasicAgent, Builder> {
        private Builder(Population population) {
            super(population);
        }

        @Override
        protected BasicAgent checkedBuild() {
            return new BasicAgent(this);
        }
        @Override
        protected Builder self() {
            return this;
        }
    }

    private static final AgentInitializationFactory INITIALIZATION_FACTORY = new AgentInitializationFactory() {
        @Override
        public ActionExecutionStrategy createStrategy(List<? extends AgentAction> actions) {
            return new DefaultActionExecutionStrategy(actions);
        }

        @Override
        public <T extends AgentComponent> SearchableList<T> newSearchableList(Iterable<T> elements) {
            return AugmentedLists.newAugmentedArrayList(elements);
        }

        @Override
        public AgentMessageBox createMessageBox() {
            return new FixedSizeMessageBox();
        }
    };
}
