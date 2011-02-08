package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;

@ClassGroup(tags="condition")
public final class AgeCondition extends IntCompareCondition {

    public AgeCondition(AgeCondition condition, CloneMap map) {
        super(condition, map);
    }

    @Override
	public boolean evaluate(Simulation simulation) {
		return false;
	}

    @Override
    public AbstractGFComponent deepCloneHelper(CloneMap map) {
        return new AgeCondition(this, map);
    }

    @Override
	protected Integer getCompareValue(Simulation simulation) {
		return simulation.getSteps() - getComponentOwner().getTimeOfBirth();
	}

    private AgeCondition() {
        this(new Builder());
    }

    private AgeCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<AgeCondition> {
        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override public AgeCondition build() { return new AgeCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends IntCompareCondition.AbstractBuilder<T> {}
}
