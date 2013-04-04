package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;

import static com.google.common.base.Preconditions.checkArgument;

@Tagged("conditions")
public class RandomCondition<A extends Agent<A, ?>> extends LeafCondition<A> {

    private double probability;

    private RandomCondition(RandomCondition<A> condition, DeepCloner map) {
        super(condition, map);
        this.probability = condition.probability;
    }

    @Override
    public boolean evaluate() {
        return Math.random() < probability;
    }

    @Override
    public RandomCondition<A> deepClone(DeepCloner cloner) {
        return new RandomCondition<A>(this, cloner);
    }

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public RandomCondition() {
        this(new Builder<A>());
    }

    private RandomCondition(AbstractBuilder<A, ?, ?> builder) {
        super(builder);
    }

    public static final class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, RandomCondition<A>, Builder<A>> {
        @Override protected Builder<A> self() { return this; }
        public RandomCondition<A> checkedBuild() { return new RandomCondition<A>(this); }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, E extends RandomCondition<A>, T extends AbstractBuilder<A, E,T>> extends LeafCondition.AbstractBuilder<A, E, T> {
        private double probability;

        public T probability(double probability) {
            checkArgument(probability >= 0 && probability <= 1, "Value is not in open interval [0,1]: " + probability);
            this.probability = probability;
            return self();
        }
    }
}
