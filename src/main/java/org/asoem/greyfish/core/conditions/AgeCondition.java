package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.utils.base.CloneMap;
import org.asoem.greyfish.utils.base.Tagged;

@Tagged("conditions")
public class AgeCondition extends IntCompareCondition<DefaultGreyfishAgent> {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public AgeCondition() {}

    private AgeCondition(AgeCondition condition, CloneMap map) {
        super(condition, map);
    }

    private AgeCondition(AbstractBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    public AgeCondition deepClone(CloneMap cloneMap) {
        return new AgeCondition(this, cloneMap);
    }

    @Override
    protected Integer getCompareValue() {
        return agent().getAge();
    }

    public static final class Builder extends AbstractBuilder<AgeCondition, Builder> {
        @Override protected Builder self() { return this; }
        @Override protected AgeCondition checkedBuild() { return new AgeCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends AgeCondition, T extends AbstractBuilder<E,T>> extends IntCompareCondition.AbstractBuilder<E,T, DefaultGreyfishAgent> {}
}
