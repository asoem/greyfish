/**
 * 
 */
package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.Map;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical OR operator.
 * @author christoph
 *
 */
public class OrCondition extends LogicalOperatorCondition {

	/* (non-Javadoc)
	 * @see org.asoem.greyfish.actions.conditions.Condition#evaluate(org.asoem.greyfish.competitors.Individual)
	 */
	@Override
	public boolean evaluate(Simulation simulation) {
		boolean ret = false;
		for (GFCondition condition : this.conditions) {
			ret = ret || condition.evaluate(null);
		}
		return ret;
	}

    @Override
    public AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    protected OrCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() { return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LogicalOperatorCondition.AbstractBuilder<T> {
        protected T fromClone(OrCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict);
            return self();
        }

        public AbstractDeepCloneable build() { return new OrCondition(this); }
    }
}
