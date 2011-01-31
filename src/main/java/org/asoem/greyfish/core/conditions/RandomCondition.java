package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkArgument;

public class RandomCondition extends LeafCondition {

    @Element(name="probability")
    private double probability;

    public RandomCondition(RandomCondition condition, CloneMap map) {
        super(condition, map);
        this.probability = condition.probability;
    }

    @Override
    public boolean evaluate(Simulation simulation) {
        return Math.random() < probability;
    }

    @Override
    public RandomCondition deepCloneHelper(CloneMap map) {
        return new RandomCondition(this, map);
    }

    @Override
    public void export(Exporter e) {
        e.addField( new ValueAdaptor<Double>("", Double.class, probability) {
            @Override
            protected void writeThrough(Double arg0) {
                probability = arg0;
            }
        });
    }

    private RandomCondition() {
        this(new Builder());
    }

    protected RandomCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<RandomCondition> {
        @Override protected Builder self() { return this; }
        public RandomCondition build() { return new RandomCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LeafCondition.AbstractBuilder<T> {
        private double probability;

        public T probability(double probability) { checkArgument(probability >= 0 && probability <= 1); this.probability = probability; return self(); }
    }
}
