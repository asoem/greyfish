/**
 *
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;

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
    protected AbstractGFComponent deepCloneHelper(CloneMap map) {
        return new AndCondition(this, map);
    }

    private AndCondition() {
        this(new Builder());
    }
    
    protected AndCondition(AndCondition cloneable, CloneMap map) {
        super(cloneable, map);
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
        public T and(GFCondition ... conditions) { return addConditions(conditions); }
        public T all(Iterable<GFCondition> conditions) { return addConditions(conditions); }
    }
}
