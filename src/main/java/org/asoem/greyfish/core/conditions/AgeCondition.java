package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;

@ClassGroup(tags="condition")
public final class AgeCondition extends IntCompareCondition {

    @SimpleXMLConstructor
    public AgeCondition() {}

    public AgeCondition(AgeCondition condition, DeepCloner map) {
        super(condition, map);
    }

    private AgeCondition(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    @Override
    public boolean apply(Simulation simulation) {
        return false;
    }

    @Override
    public AbstractAgentComponent deepClone(DeepCloner cloner) {
        return new AgeCondition(this, cloner);
    }

    @Override
    protected Integer getCompareValue(Simulation simulation) {
        return agent().getAge();
    }

    public static final class Builder extends AbstractBuilder<AgeCondition, Builder> {
        @Override protected Builder self() { return this; }
        @Override public AgeCondition checkedBuild() { return new AgeCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends AgeCondition, T extends AbstractBuilder<E,T>> extends IntCompareCondition.AbstractBuilder<E,T> {}
}
