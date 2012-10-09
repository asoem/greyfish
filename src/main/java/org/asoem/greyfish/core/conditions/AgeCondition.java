package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;

@Tagged(tags="conditions")
public class AgeCondition extends IntCompareCondition {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public AgeCondition() {}

    public AgeCondition(AgeCondition condition, DeepCloner map) {
        super(condition, map);
    }

    private AgeCondition(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    @Override
    public AbstractAgentComponent deepClone(DeepCloner cloner) {
        return new AgeCondition(this, cloner);
    }

    @Override
    protected Integer getCompareValue() {
        return agent().getAge();
    }

    public static final class Builder extends AbstractBuilder<AgeCondition, Builder> {
        @Override protected Builder self() { return this; }
        @Override protected AgeCondition checkedBuild() { return new AgeCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends AgeCondition, T extends AbstractBuilder<E,T>> extends IntCompareCondition.AbstractBuilder<E,T> {}
}
