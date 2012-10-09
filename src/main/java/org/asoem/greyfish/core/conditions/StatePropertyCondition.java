package org.asoem.greyfish.core.conditions;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.properties.FiniteStateProperty;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.simpleframework.xml.Element;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@Tagged("conditions")
public class StatePropertyCondition extends LeafCondition {

    @Element(name="property",required=false)
    private FiniteStateProperty<?> stateProperty;
    // TODO: The stateProperty might get modified during construction phase. Observe this!

    @Element(name="state",required=false)
    private Object state;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public StatePropertyCondition() {
        this(new Builder());
    }

    protected StatePropertyCondition(AbstractBuilder<?,?> builder) {
        super(builder);
        this.state = builder.state;
        this.stateProperty = builder.property;
    }

    protected StatePropertyCondition(StatePropertyCondition condition, DeepCloner map) {
        super(condition, map);
        this.stateProperty = map.getClone(condition.stateProperty, FiniteStateProperty.class);
        this.state = condition.state;
    }

    @Override
    public boolean apply(AgentAction action) {
        return stateProperty != null &&
                Objects.equal(stateProperty.getValue(), state);
    }

    @Override
    public StatePropertyCondition deepClone(DeepCloner cloner) {
        return new StatePropertyCondition(this, cloner);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        final SetAdaptor<FiniteStateProperty> statesAdaptor = new SetAdaptor<FiniteStateProperty>(
                FiniteStateProperty.class) {
            @Override protected void set(FiniteStateProperty arg0) { stateProperty = checkNotNull(arg0); }
            @Override public FiniteStateProperty get() { return stateProperty; }

            @Override
            public Iterable<FiniteStateProperty> values() {
                return Iterables.filter(agent().getProperties(), FiniteStateProperty.class);
            }
        };
        e.add("Property", statesAdaptor);

        final SetAdaptor<?> stateAdaptor = new SetAdaptor<Object>(Object.class) {
            @Override protected void set(Object arg0) { state = checkNotNull(arg0); }
            @Override public Object get() { return state; }
            @SuppressWarnings({"unchecked"}) // cast from Set<?> to Set<Object> is safe
            @Override public Iterable<Object> values() {
                return (stateProperty == null) ? ImmutableList.of() : (Set<Object>) stateProperty.getStates();
            }
        };
        statesAdaptor.addValueChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                stateAdaptor.update();
            }
        });
        e.add("has state", stateAdaptor);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<StatePropertyCondition,Builder> {
        @Override protected Builder self() { return this; }
        @Override protected StatePropertyCondition checkedBuild() { return new StatePropertyCondition(this); }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends StatePropertyCondition,T extends AbstractBuilder<E,T>> extends LeafCondition.AbstractBuilder<E,T> {
        private FiniteStateProperty<?> property;
        private Object state;

        public T property(FiniteStateProperty<?> property) { this.property = checkNotNull(property); return self(); }
        public T hasState(Object state) { this.state = checkNotNull(state); return self(); }
    }
}
