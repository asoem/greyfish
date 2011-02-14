package org.asoem.greyfish.core.conditions;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.properties.FiniteSetProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public class StatePropertyCondition extends LeafCondition {

    @Element(name="property",required=false)
    private FiniteSetProperty stateProperty;

    @Element(name="state",required=false)
    private Object state;

    public StatePropertyCondition(StatePropertyCondition condition, CloneMap map) {
        super(condition, map);
        this.stateProperty = map.clone(condition.stateProperty, FiniteSetProperty.class);
        this.state = condition.state;
    }

    @Override
    public boolean evaluate(Simulation simulation) {
        return stateProperty != null &&
                Objects.equal(stateProperty.get(), state);
    }

    @Override
    public StatePropertyCondition deepCloneHelper(CloneMap map) {
        return new StatePropertyCondition(this, map);
    }

    @Override
    public void export(Exporter e) {
        final ValueSelectionAdaptor<FiniteSetProperty> statesAdaptor = new ValueSelectionAdaptor<FiniteSetProperty>(
                "Property", FiniteSetProperty.class) {
            @Override protected void set(FiniteSetProperty arg0) { stateProperty = checkFrozen(checkNotNull(arg0)); }
            @Override public FiniteSetProperty get() { return stateProperty; }

            @Override
            public Iterable<FiniteSetProperty> values() {
                return Iterables.filter(getComponentOwner().getProperties(), FiniteSetProperty.class);
            }
        };
        e.add(statesAdaptor);

        final ValueSelectionAdaptor<Object> stateAdaptor = new ValueSelectionAdaptor<Object>( "has state", Object.class) {
            @Override protected void set(Object arg0) { state = checkFrozen(checkNotNull(arg0)); }
            @Override public Iterable<Object> get() { return Arrays.asList((stateProperty == null) ? new Object[0] : stateProperty.getSet()); }
            @Override public Iterable<Object> values() {
                return Arrays.asList((stateProperty == null) ? new Object[0] : stateProperty.getSet());
            }
        };
        statesAdaptor.addValueChangeListener(stateAdaptor);
        e.add(stateAdaptor);
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) {
        super.checkConsistency(components);
        Preconditions.checkState(Iterables.contains(components, stateProperty));
    }

    private StatePropertyCondition() {
        this(new Builder());
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
    }
}
