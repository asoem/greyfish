package org.asoem.greyfish.core.conditions;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

@Tagged(tags = "conditions")
public class LastExecutedActionCondition extends LeafCondition {

    @Element(name="actions", required=false)
    private AgentAction action;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public LastExecutedActionCondition() {
        this(new Builder());
    }

    protected LastExecutedActionCondition(AbstractBuilder<?,?> builder) {
        super(builder);
        this.action = builder.action;
    }
    protected LastExecutedActionCondition(LastExecutedActionCondition condition, DeepCloner map) {
        super(condition, map);
        this.action = map.getClone(condition.action, AgentAction.class);
    }

    @Override
    public boolean apply(AgentAction action) {
        return isSameAction(this.action, agent().getLastExecutedAction());
    }

    private static boolean isSameAction(AgentAction a1, AgentAction a2) {
        return Objects.equal(a1, a2);
//        return Equivalences.equals().equivalent(a1, a2);  // TODO: Make comparison more strict if we can guaranty theAction to be not null
    }

    @Override
    public void configure(ConfigurationHandler e) {
        e.add("Action", new SetAdaptor<AgentAction>(AgentAction.class) {
            @Override
            protected void set(AgentAction arg0) {
                action = checkNotNull(arg0);
            }

            @Override
            public AgentAction get() {
                return action;
            }

            @Override
            public Iterable<AgentAction> values() {
                return Iterables.filter(agent().getProperties(), AgentAction.class);
            }
        });
    }

    @Override
    public LastExecutedActionCondition deepClone(DeepCloner cloner) {
        return new LastExecutedActionCondition(this, cloner);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<LastExecutedActionCondition, Builder> {
        @Override protected Builder self() { return this; }
        @Override protected LastExecutedActionCondition checkedBuild() { return new LastExecutedActionCondition(this); }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends LastExecutedActionCondition,T extends AbstractBuilder<E,T>> extends LeafCondition.AbstractBuilder<E,T> {
        private AgentAction action;

        public T theLastExecutedActionWas(AgentAction action) { this.action = action; return self(); }
    }
}
