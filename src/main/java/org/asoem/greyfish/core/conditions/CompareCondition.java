package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.util.Map;

public abstract class CompareCondition<T extends Comparable<T>> extends LeafCondition {

    @Attribute(name="comparator")
    protected Comparator parameterComparator = Comparator.EQ;

    @Element(name="value")
    protected T value;

    @Override
    public boolean evaluate(Simulation simulation) {
        return parameterComparator.compare(getCompareValue(simulation), value);
    }

    protected abstract T getCompareValue(Simulation simulation);

    @Override
    public void export(Exporter e) {
        e.addField( new ValueSelectionAdaptor<Comparator>("", Comparator.class, parameterComparator, Comparator.values()) {
            @Override
            protected void writeThrough(Comparator arg0) {
                CompareCondition.this.parameterComparator = arg0;
            }
        });
    }

    protected CompareCondition(AbstractBuilder<? extends AbstractBuilder, T> builder) {
        super(builder);
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T, E>, E extends Comparable<E>> extends LeafCondition.AbstractBuilder<T> {
        private Comparator parameterComparator;
        private E value;

        public T parameterComparator(Comparator parameterComparator) { this.parameterComparator = parameterComparator; return self(); }
        public T value(E value) { this.value = value; return self(); }

        protected T fromClone(CompareCondition<E> component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict);
            return self();
        }
    }
}
