package org.asoem.greyfish.core.conditions;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.properties.FiniteSetProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class StatePropertyCondition extends LeafCondition {

    @Element(name="property",required=false)
    private FiniteSetProperty<?> stateProperty;

    @Element(name="state",required=false)
    private Object state;

    @Override
    public boolean evaluate(Simulation simulation) {
        return stateProperty != null &&
                Objects.equal(stateProperty.getValue(), state);
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
        e.addField(new ValueSelectionAdaptor<FiniteSetProperty>("", FiniteSetProperty.class, stateProperty, getComponentOwner().getProperties(FiniteSetProperty.class)) {
            @Override
            protected void writeThrough(FiniteSetProperty arg0) {
                stateProperty = checkFrozen(checkNotNull(arg0));
            }
        });
        e.addField(new ValueSelectionAdaptor<Object>("has state", Object.class, state, (stateProperty == null) ? new Object[0] : stateProperty.getSet()) {
            @Override
            protected void writeThrough(Object arg0) {
                state = checkFrozen(checkNotNull(arg0));
            }
        });
    }

    @Override
    public void checkIfFreezable(Iterable<? extends GFComponent> components) {
        super.checkIfFreezable(components);
        Preconditions.checkState(Iterables.contains(components, stateProperty));
    }

    protected StatePropertyCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.state = builder.state;
        this.stateProperty = builder.property;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<StatePropertyCondition> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public StatePropertyCondition build() { return new StatePropertyCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LeafCondition.AbstractBuilder<T> {
        private FiniteSetProperty<?> property;
        private Object state;

        public T property(FiniteSetProperty<?> property) { this.property = checkNotNull(property); return self(); }
        public T hasState(Object state) { this.state = checkNotNull(state); return self(); }

        protected T fromClone(StatePropertyCondition component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(component, mapDict).
                    hasState(component.state).
                    property(deepClone(component.stateProperty, mapDict));
            return self();
        }
    }
}
