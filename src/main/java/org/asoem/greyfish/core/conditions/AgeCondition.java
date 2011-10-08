package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.DeepCloner;

@ClassGroup(tags="condition")
public final class AgeCondition extends IntCompareCondition {

    public AgeCondition(AgeCondition condition, DeepCloner map) {
        super(condition, map);
    }

    @Override
	public boolean evaluate(ParallelizedSimulation simulation) {
		return false;
	}

    @Override
    public AbstractAgentComponent deepClone(DeepCloner cloner) {
        return new AgeCondition(this, cloner);
    }

    @Override
	protected Integer getCompareValue(ParallelizedSimulation simulation) {
		return agent.get().getAge();
	}

    @SimpleXMLConstructor
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
