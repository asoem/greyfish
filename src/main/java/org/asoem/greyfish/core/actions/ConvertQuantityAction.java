package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.simpleframework.xml.Element;

@ClassGroup(tags="actions")
public class ConvertQuantityAction extends AbstractGFAction {

    @Element(name="source")
    private DoubleProperty parameterSource = null;

    @Element(name="target")
    private DoubleProperty parameterTarget = null;

    @Element(name="factor")
    private double parameterFactor = 0;

    @Element(name="max")
    private double parameterMax = 0;

    @Override
    protected void performAction(Simulation simulation) {
        if (parameterSource != null && parameterTarget != null) {
            double add_amount = Math.min(parameterSource.getValue(), parameterMax) * parameterFactor;

            if (parameterTarget.getValue() + add_amount > parameterTarget.getUpperBound()) {
                add_amount = parameterTarget.getUpperBound() - parameterTarget.getValue();
            }

            parameterTarget.setValue(parameterTarget.getValue() + add_amount);
            parameterSource.setValue(parameterSource.getValue() - add_amount / parameterFactor );
        }
    }

    @Override
    protected AbstractGFComponent deepCloneHelper(CloneMap cloneMap) {
        return new ConvertQuantityAction(this, cloneMap);
    }

    public ConvertQuantityAction(ConvertQuantityAction cloneable, CloneMap map) {
        super(cloneable, map);
        this.parameterSource = deepClone(cloneable.parameterSource, map);
        this.parameterTarget = deepClone(cloneable.parameterTarget, map);
        this.parameterFactor = cloneable.parameterFactor;
        this.parameterMax = cloneable.parameterMax;
    }

    protected ConvertQuantityAction(AbstractBuilder<?> builder) {
        super(builder);
        this.parameterTarget = builder.parameterTarget;
        this.parameterFactor = builder.parameterFactor;
        this.parameterSource = builder.parameterSource;
        this.parameterMax = builder.parameterMax;
    }

    public static final Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<ConvertQuantityAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public ConvertQuantityAction build() { return new ConvertQuantityAction(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFAction.AbstractBuilder<T> {
        private DoubleProperty parameterSource = null;
        private DoubleProperty parameterTarget = null;
        private double parameterFactor = 0;
        private double parameterMax = 0;

        public T parameterSource(DoubleProperty parameterSource) { this.parameterSource = parameterSource; return self(); }
        public T parameterTarget(DoubleProperty parameterTarget) { this.parameterTarget = parameterTarget; return self(); }
        public T parameterFactor(double parameterFactor) { this.parameterFactor = parameterFactor; return self(); }
        public T parameterMax(double parameterMax) { this.parameterMax = parameterMax; return self(); }
    }
}
