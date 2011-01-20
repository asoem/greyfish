/**
 *
 */
package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.simulation.Simulation;
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
    public boolean evaluate(Simulation simulation) {
        boolean ret = true;
        for (GFCondition condition : conditions) {
            ret = ret && condition.evaluate(simulation);
        }
        return ret;
    }

    @Override
    public AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    protected AndCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() { return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LogicalOperatorCondition.AbstractBuilder<T> {
        protected T fromClone(AndCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict);
            return self();
        }

        public AndCondition build() { return new AndCondition(this); }
    }
}
