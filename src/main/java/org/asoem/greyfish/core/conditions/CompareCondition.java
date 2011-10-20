package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.Comparator;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
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
    public boolean apply(Simulation simulation) {
        return comparator.compare(getCompareValue(simulation), value);
    }

    protected abstract T getCompareValue(Simulation simulation);

    @Override
    public void configure(ConfigurationHandler e) {
        e.add(new SetAdaptor<Comparator>("", Comparator.class) {
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

    protected CompareCondition(AbstractBuilder<?,?,T> builder) {
        super(builder);
        this.comparator = builder.comparator;
        this.value = builder.value;
    }

    protected static abstract class AbstractBuilder<C extends CompareCondition<?>, T extends AbstractBuilder<C, T, E>, E extends Comparable<E>> extends LeafCondition.AbstractBuilder<C,T> {
        private Comparator comparator;
        private E value;

        public T is(Comparator comparator) { this.comparator = checkNotNull(comparator); return self(); }
        public T to(E value) { this.value = checkNotNull(value); return self(); }
    }
}
