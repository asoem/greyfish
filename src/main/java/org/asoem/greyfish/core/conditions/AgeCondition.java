package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.Map;

@ClassGroup(tags="condition")
public class AgeCondition extends IntCompareCondition {

	@Override
	public boolean evaluate(Simulation simulation) {
		return false;
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new Builder().fromClone(this, mapDict).build();
	}

	@Override
	protected Integer getCompareValue(Simulation simulation) {
		return simulation.getSteps() - componentOwner.getTimeOfBirth();
	}

    protected AgeCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() { return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends IntCompareCondition.AbstractBuilder<T> {
        protected T fromClone(AgeCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict);
            return self();
        }

        public AbstractDeepCloneable build() { return new AgeCondition(this); }
    }
}
