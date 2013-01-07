package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.CloneMap;
import org.asoem.greyfish.utils.base.CompareOperator;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class CompareCondition<T extends Comparable<T>, A extends Agent<A, ?>> extends LeafCondition<A> {

    @Attribute(name="comparator")
    protected CompareOperator compareOperator = CompareOperator.EQUAL;

    @Element(name="value")
    protected T value;

    protected CompareCondition() {}

    protected CompareCondition(CompareCondition<T, A> condition, CloneMap map) {
        super(condition, map);
        this.compareOperator = condition.compareOperator;
        this.value = condition.value;
    }

    protected CompareCondition(AbstractBuilder<A, ?, ?, T> builder) {
        super(builder);
        this.compareOperator = builder.compareOperator;
        this.value = builder.value;
    }

    @Override
    public boolean evaluate() {
        return compareOperator.apply(getCompareValue(), value);
    }

    protected abstract T getCompareValue();

    @Override
    public void configure(ConfigurationHandler e) {
        e.add("", new SetAdaptor<CompareOperator>(CompareOperator.class) {
            @Override
            protected void set(CompareOperator arg0) {
                compareOperator = checkNotNull(arg0);
            }

            @Override
            public CompareOperator get() {
                return compareOperator;
            }

            @Override
            public Iterable<CompareOperator> values() {
                return Arrays.asList(CompareOperator.values());
            }
        });
    }

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends CompareCondition<?, A>, T extends AbstractBuilder<A, C, T, E>, E extends Comparable<E>> extends LeafCondition.AbstractBuilder<A, C, T> {
        private CompareOperator compareOperator;
        private E value;

        public T is(CompareOperator compareOperator) { this.compareOperator = checkNotNull(compareOperator); return self(); }
        public T to(E value) { this.value = checkNotNull(value); return self(); }
    }
}
