package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.DeepCloner;
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
    protected State executeUnconditioned(Simulation simulation) {
        if (parameterSource != null && parameterTarget != null) {
            double add_amount = Math.min(parameterSource.get(), parameterMax) * parameterFactor;

            if (parameterTarget.get() + add_amount > parameterTarget.getUpperBound()) {
                add_amount = parameterTarget.getUpperBound() - parameterTarget.get();
            }

            parameterTarget.setValue(parameterTarget.get() + add_amount);
            parameterSource.setValue(parameterSource.get() - add_amount / parameterFactor );
        }
        return State.END_SUCCESS;
    }

    @Override
    public AbstractGFComponent deepClone(DeepCloner cloner) {
        return new ConvertQuantityAction(this, cloner);
    }

    public ConvertQuantityAction(ConvertQuantityAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.parameterSource = map.continueWith(cloneable.parameterSource, DoubleProperty.class);
        this.parameterTarget = map.continueWith(cloneable.parameterTarget, DoubleProperty.class);
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

    public static Builder with() { return new Builder(); }
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
