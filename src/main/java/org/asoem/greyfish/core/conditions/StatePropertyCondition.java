package org.asoem.greyfish.core.conditions;

import com.google.common.base.Objects;
import org.asoem.greyfish.core.properties.FiniteSetProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import java.util.Map;

public class StatePropertyCondition extends LeafCondition {

    @Element(name="property",required=false)
    private FiniteSetProperty<?> parameterStateProperty;

    @Element(name="state",required=false)
    private Object parameterState;

    @Override
    public boolean evaluate(Simulation simulation) {
        return parameterStateProperty != null &&
                Objects.equal(parameterStateProperty.getValue(), parameterState);
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    @Override
    public void export(Exporter e) {
        e.addField(new ValueSelectionAdaptor<FiniteSetProperty>("", FiniteSetProperty.class, parameterStateProperty, getComponentOwner().getProperties(FiniteSetProperty.class)) {
            @Override
            protected void writeThrough(FiniteSetProperty arg0) {
                StatePropertyCondition.this.parameterStateProperty = arg0;
            }
        });
        e.addField(new ValueSelectionAdaptor<Object>("has state", Object.class, parameterState, (parameterStateProperty == null) ? new Object[0] : parameterStateProperty.getSet()) {
            @Override
            protected void writeThrough(Object arg0) {
                StatePropertyCondition.this.parameterState = arg0;
            }
        });
    }

    protected StatePropertyCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.parameterState = builder.parameterState;
        this.parameterStateProperty = builder.parameterStateProperty;
    }

    public static final class Builder extends AbstractBuilder<Builder> {
        @Override protected Builder self() { return this; }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LeafCondition.AbstractBuilder<T> {
        private FiniteSetProperty<?> parameterStateProperty;
        private Object parameterState;

        public T parameterStateProperty(FiniteSetProperty<?> parameterStateProperty) { this.parameterStateProperty = parameterStateProperty; return self(); }
        public T parameterState(Object parameterState) { this.parameterState = parameterState; return self(); }

        protected T fromClone(StatePropertyCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict).
                    parameterState(component.parameterState).
                    parameterStateProperty(deepClone(component.parameterStateProperty, mapDict));
            return self();
        }

        public StatePropertyCondition build() { return new StatePropertyCondition(this); }
    }
}
