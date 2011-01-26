package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Element;

import java.util.Map;

public class RandomCondition extends LeafCondition {

    @Element(name="propability")
    private double parameterTruePropability;

    @Override
    public boolean evaluate(Simulation simulation) {
        return Math.random() < parameterTruePropability;
    }

    @Override
    protected AbstractGFComponent deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    @Override
    public void export(Exporter e) {
        e.addField( new ValueAdaptor<Double>("", Double.class, parameterTruePropability) {
            @Override
            protected void writeThrough(Double arg0) {
                parameterTruePropability = arg0;
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
        private double parameterTruePropability;

        public T parameterTruePropability(double parameterTruePropability) { this.parameterTruePropability = parameterTruePropability; return self(); }

        protected T fromClone(RandomCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict);
            return self();
        }
    }
}
