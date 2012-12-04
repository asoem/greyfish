package org.asoem.greyfish.core.conditions;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.agent.Agent;
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
public class StatePropertyCondition<A extends Agent<A, ?>> extends LeafCondition<A> {

    @Element(name="property",required=false)
    private FiniteStateProperty<?, A> stateProperty;
    // TODO: The stateProperty might get modified during construction phase. Observe this!

    @Element(name="state",required=false)
    private Object state;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public StatePropertyCondition() {
        this(new Builder<A>());
    }

    private StatePropertyCondition(AbstractBuilder<A, ?, ?> builder) {
        super(builder);
        this.state = builder.state;
        this.stateProperty = builder.property;
    }

    @SuppressWarnings("unchecked") // casting a clone is safe
    private StatePropertyCondition(StatePropertyCondition<A> condition, DeepCloner cloner) {
        super(condition, cloner);
        this.stateProperty = (FiniteStateProperty<?, A>) cloner.getClone(condition.stateProperty);
        this.state = condition.state;
    }

    @Override
    public boolean evaluate() {
        return stateProperty != null &&
                Objects.equal(stateProperty.getValue(), state);
    }

    @Override
    public StatePropertyCondition<A> deepClone(DeepCloner cloner) {
        return new StatePropertyCondition<A>(this, cloner);
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

    public static <A extends Agent<A, ?>> Builder<A> builder() { return new Builder<A>(); }

    public static final class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, StatePropertyCondition<A>, Builder<A>> {
        @Override protected Builder<A> self() { return this; }
        @Override protected StatePropertyCondition<A> checkedBuild() { return new StatePropertyCondition<A>(this); }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, E extends StatePropertyCondition<A>,T extends AbstractBuilder<A, E,T>> extends LeafCondition.AbstractBuilder<A, E, T> {
        private FiniteStateProperty<?, A> property;
        private Object state;

        public T property(FiniteStateProperty<?, A> property) { this.property = checkNotNull(property); return self(); }
        public T hasState(Object state) { this.state = checkNotNull(state); return self(); }
    }
}
