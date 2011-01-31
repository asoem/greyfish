/**
 * 
 */
package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;

/**
 * This class can be used to prefix one <code>Condition</code> with a logical NOT operator.
 * @author christoph
 *
 */
public class NandCondition extends AndCondition {

    protected NandCondition(NandCondition condition, CloneMap map) {
        super(condition, map);
    }

    /* (non-Javadoc)
      * @see org.asoem.greyfish.actions.conditions.Condition#evaluate(org.asoem.greyfish.competitors.Individual)
      */
	@Override
	public boolean evaluate(Simulation simulation) {
		return ! super.evaluate(simulation);
	}

    @Override
    public AbstractGFComponent deepCloneHelper(CloneMap map) {
        return new NandCondition(this, map);
    }

    private NandCondition() {
        this(new Builder());
    }

    protected NandCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<NandCondition> {
        @Override protected Builder self() { return this; }
        public NandCondition build() { return new NandCondition(this); }
        public Builder notAll(GFCondition ... conditions) { return super.addConditions(conditions); }
    }
}
