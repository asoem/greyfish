/**
 * 
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical OR operator.
 * @author christoph
 *
 */
public class OrCondition extends LogicalOperatorCondition {

    public OrCondition(OrCondition condition, CloneMap map) {
        super(condition, map);
    }

    /* (non-Javadoc)
      * @see org.asoem.greyfish.actions.conditions.Condition#evaluate(org.asoem.greyfish.competitors.Individual)
      */
	@Override
	public boolean evaluate(final Simulation simulation) {
		return Iterables.any(conditions, new Predicate<GFCondition>() {
            @Override
            public boolean apply(GFCondition gfCondition) {
                return gfCondition.evaluate(simulation);
            }
        });
	}

    @Override
    public OrCondition deepCloneHelper(CloneMap map) {
        return new OrCondition(this, map);
    }

    private OrCondition() {
        this(new Builder());
    }

    protected OrCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<OrCondition> {
        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override public OrCondition build() { return new OrCondition(this); }
        public Builder any(GFCondition ... conditions) { return super.addConditions(conditions); }
    }
}
