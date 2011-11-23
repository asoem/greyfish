package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkArgument;

public class RandomCondition extends LeafCondition {

    @Element(name="probability")
    private double probability;

    public RandomCondition(RandomCondition condition, DeepCloner map) {
        super(condition, map);
        this.probability = condition.probability;
    }

    @Override
    public boolean apply(Simulation simulation) {
        return Math.random() < probability;
    }

    @Override
    public RandomCondition deepClone(DeepCloner cloner) {
        return new RandomCondition(this, cloner);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        e.add("", new TypedValueAdaptor<Double>() {
            @Override
            protected void set(Double arg0) {
                probability = arg0;
            }

            @Override
            public Double get() {
                return probability;
            }
        });
    }

    private RandomCondition() {
        this(new Builder());
    }

    protected RandomCondition(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<RandomCondition, Builder> {
        @Override protected Builder self() { return this; }
        public RandomCondition checkedBuild() { return new RandomCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends RandomCondition, T extends AbstractBuilder<E,T>> extends LeafCondition.AbstractBuilder<E,T> {
        private double probability;

        public T probability(double probability) { checkArgument(probability >= 0 && probability <= 1); this.probability = probability; return self(); }
    }
}
