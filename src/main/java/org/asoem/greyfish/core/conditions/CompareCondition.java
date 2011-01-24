package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class CompareCondition<T extends Comparable<T>> extends LeafCondition {

    @Attribute(name="comparator")
    protected Comparator comparator = Comparator.EQ;

    @Element(name="value")
    protected T value;

    @Override
    public boolean evaluate(Simulation simulation) {
        return comparator.compare(getCompareValue(simulation), value);
    }

    protected abstract T getCompareValue(Simulation simulation);

    @Override
    public void export(Exporter e) {
        e.addField( new ValueSelectionAdaptor<Comparator>("", Comparator.class, comparator, Comparator.values()) {
            @Override protected void writeThrough(Comparator arg0) { comparator = checkFrozen(checkNotNull(arg0)); }
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

        protected T fromClone(CompareCondition<E> component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict);
            return self();
        }
    }
}
