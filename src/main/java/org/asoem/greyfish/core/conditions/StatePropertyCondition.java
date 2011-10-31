package org.asoem.greyfish.core.conditions;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.properties.FiniteStateProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.simpleframework.xml.Element;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class StatePropertyCondition extends LeafCondition {

    @Element(name="property",required=false)
    private FiniteStateProperty<?> stateProperty;
    // TODO: The stateProperty might get modified during construction phase. Observe this!

    @Element(name="state",required=false)
    private Object state;

    public StatePropertyCondition(StatePropertyCondition condition, DeepCloner map) {
        super(condition, map);
        this.stateProperty = map.cloneField(condition.stateProperty, FiniteStateProperty.class);
        this.state = condition.state;
    }

    @Override
    public boolean apply(Simulation simulation) {
        return stateProperty != null &&
                Objects.equal(stateProperty.get(), state);
    }

    @Override
    public StatePropertyCondition deepClone(DeepCloner cloner) {
        return new StatePropertyCondition(this, cloner);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        final SetAdaptor<FiniteStateProperty> statesAdaptor = new SetAdaptor<FiniteStateProperty>(
                "Property", FiniteStateProperty.class) {
            @Override protected void set(FiniteStateProperty arg0) { stateProperty = checkNotNull(arg0); }
            @Override public FiniteStateProperty get() { return stateProperty; }

            @Override
            public Iterable<FiniteStateProperty> values() {
                return Iterables.filter(agent().getProperties(), FiniteStateProperty.class);
            }
        };
        e.add(statesAdaptor);

        final SetAdaptor<?> stateAdaptor = new SetAdaptor<Object>( "has state", Object.class) {
            @Override protected void set(Object arg0) { state = checkNotNull(arg0); }
            @Override public Object get() { return state; }
            @Override public Iterable<Object> values() {
                // cast from Set<?> to Set<Object> is safe
                // noinspection unchecked
                return (stateProperty == null) ? ImmutableList.of() : (Set<Object>) stateProperty.getStates();
            }
        };
        statesAdaptor.addValueChangeListener(stateAdaptor);
        e.add(stateAdaptor);
    }

    private StatePropertyCondition() {
        this(new Builder());
    }

    protected StatePropertyCondition(AbstractBuilder<?,?> builder) {
        super(builder);
        this.state = builder.state;
        this.stateProperty = builder.property;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<StatePropertyCondition,Builder> {
        @Override protected Builder self() { return this; }
        @Override public StatePropertyCondition checkedBuild() { return new StatePropertyCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends StatePropertyCondition,T extends AbstractBuilder<E,T>> extends LeafCondition.AbstractBuilder<E,T> {
        private FiniteStateProperty<?> property;
        private Object state;

        public T property(FiniteStateProperty<?> property) { this.property = checkNotNull(property); return self(); }
        public T hasState(Object state) { this.state = checkNotNull(state); return self(); }
    }
}
