package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.SimulationContext;
import org.asoem.greyfish.utils.base.Tagged;

import static com.google.common.base.Preconditions.checkArgument;

@Tagged("conditions")
public class RandomCondition<A extends Agent<A, SimulationContext<?>>> extends LeafCondition<A> {

    private double probability;

    @Override
    public boolean evaluate() {
        return Math.random() < probability;
    }

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public RandomCondition() {
        this(new Builder<A>());
    }

    private RandomCondition(final AbstractBuilder<A, ?, ?> builder) {
        super(builder);
    }

    public static final class Builder<A extends Agent<A, SimulationContext<?>>> extends AbstractBuilder<A, RandomCondition<A>, Builder<A>> {
        @Override
        protected Builder<A> self() {
            return this;
        }

        public RandomCondition<A> checkedBuild() {
            return new RandomCondition<A>(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends Agent<A, SimulationContext<?>>, E extends RandomCondition<A>, T extends AbstractBuilder<A, E, T>> extends LeafCondition.AbstractBuilder<A, E, T> {
        private double probability;

        public T probability(final double probability) {
            checkArgument(probability >= 0 && probability <= 1, "Value is not in open interval [0,1]: " + probability);
            this.probability = probability;
            return self();
        }
    }
}
