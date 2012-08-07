package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkArgument;

@ClassGroup(tags = "conditions")
public class RandomCondition extends LeafCondition {

    @Element(name="probability")
    private double probability;

    public RandomCondition(RandomCondition condition, DeepCloner map) {
        super(condition, map);
        this.probability = condition.probability;
    }

    @Override
    public boolean apply(GFAction action) {
        return Math.random() < probability;
    }

    @Override
    public RandomCondition deepClone(DeepCloner cloner) {
        return new RandomCondition(this, cloner);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        e.add("", new AbstractTypedValueModel<Double>() {
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

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public RandomCondition() {
        this(new Builder());
    }

    protected RandomCondition(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<RandomCondition, Builder> {
        @Override protected Builder self() { return this; }
        public RandomCondition checkedBuild() { return new RandomCondition(this); }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends RandomCondition, T extends AbstractBuilder<E,T>> extends LeafCondition.AbstractBuilder<E,T> {
        private double probability;

        public T probability(double probability) {
            checkArgument(probability >= 0 && probability <= 1, "Value is not in open interval [0,1]: " + probability);
            this.probability = probability;
            return self();
        }
    }
}
