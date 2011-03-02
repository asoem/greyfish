/**
 *
 */
package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical AND operator.
 * @author christoph
 *
 */
public class AndCondition extends LogicalOperatorCondition {

    /* (non-Javadoc)
      * @see org.asoem.greyfish.actions.conditions.Condition#evaluateConditions(org.asoem.greyfish.competitors.Individual)
      */
    @Override
    public boolean evaluate(final Simulation simulation) {
        for (GFCondition condition : conditions) {
            if (!condition.evaluate(simulation))
                return false;
        }
        return true;
    }

    @Override
    public AbstractGFComponent deepCloneHelper(CloneMap map) {
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
