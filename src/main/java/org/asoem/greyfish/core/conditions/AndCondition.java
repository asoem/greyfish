/**
 *
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.Map;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical AND operator.
 * @author christoph
 *
 */
public class AndCondition extends LogicalOperatorCondition {

    /* (non-Javadoc)
      * @see org.asoem.greyfish.actions.conditions.Condition#evaluate(org.asoem.greyfish.competitors.Individual)
      */
    @Override
    public boolean evaluate(final Simulation simulation) {
        return Iterables.all(conditions, new Predicate<GFCondition>() {
            @Override
            public boolean apply(GFCondition gfCondition) {
                return gfCondition.evaluate(simulation);
            }
        });
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    protected AndCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<AndCondition> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public AndCondition build() { return new AndCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LogicalOperatorCondition.AbstractBuilder<T> {
        public T and(GFCondition condition) { return addCondition(condition); }
        public T all(Iterable<GFCondition> conditions) { return addConditions(conditions); }

        protected T fromClone(AndCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict);
            return self();
        }
    }
}
