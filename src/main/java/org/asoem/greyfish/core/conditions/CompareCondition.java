package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.lang.Comparator;
import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class CompareCondition<T extends Comparable<T>> extends LeafCondition {

    @Attribute(name="comparator")
    protected Comparator comparator = Comparator.EQ;

    @Element(name="value")
    protected T value;

    protected CompareCondition(CompareCondition<T> condition, DeepCloner map) {
        super(condition, map);
        this.comparator = condition.comparator;
        this.value = condition.value;
    }

    @Override
    public boolean evaluate(ParallelizedSimulation simulation) {
        return comparator.compare(getCompareValue(simulation), value);
    }

    protected abstract T getCompareValue(ParallelizedSimulation simulation);

    @Override
    public void configure(ConfigurationHandler e) {
        e.add(new FiniteSetValueAdaptor<Comparator>("", Comparator.class) {
            @Override
            protected void set(Comparator arg0) {
                comparator = checkNotNull(arg0);
            }

            @Override
            public Comparator get() {
                return comparator;
            }

            @Override
            public Iterable<Comparator> values() {
                return Arrays.asList(Comparator.values());
            }
        });
    }

    protected CompareCondition(AbstractBuilder<? extends AbstractBuilder, T> builder) {
        super(builder);
        this.comparator = builder.comparator;
        this.value = builder.value;
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T, E>, E extends Comparable<E>> extends LeafCondition.AbstractBuilder<T> {
        private Comparator comparator;
        private E value;

        public T is(Comparator comparator) { this.comparator = checkNotNull(comparator); return self(); }
        public T to(E value) { this.value = checkNotNull(value); return self(); }
    }
}
