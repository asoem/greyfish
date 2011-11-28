package org.asoem.greyfish.core.conditions;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

public class LastExecutedActionCondition extends LeafCondition {

    @Element(name="actions", required=false)
    private GFAction action;

    protected LastExecutedActionCondition(LastExecutedActionCondition condition, DeepCloner map) {
        super(condition, map);
        this.action = map.cloneField(condition.action, GFAction.class);
    }

    @Override
    public boolean apply(Simulation simulation) {
        return isSameAction(action, agent().getLastExecutedAction());
    }

    private static boolean isSameAction(GFAction a1, GFAction a2) {
        return Objects.equal(a1, a2);
//        return Equivalences.equals().equivalent(a1, a2);  // TODO: Make comparison more strict if we can guaranty theAction to be not null
    }

    @Override
    public void configure(ConfigurationHandler e) {
        e.add("Action", new SetAdaptor<GFAction>(GFAction.class) {
            @Override
            protected void set(GFAction arg0) {
                action = checkNotNull(arg0);
            }

            @Override
            public GFAction get() {
                return action;
            }

            @Override
            public Iterable<GFAction> values() {
                return Iterables.filter(agent().getProperties(), GFAction.class);
            }
        });
    }

    @Override
    public LastExecutedActionCondition deepClone(DeepCloner cloner) {
        return new LastExecutedActionCondition(this, cloner);
    }

    private LastExecutedActionCondition() {
        this(new Builder());
    }

    protected LastExecutedActionCondition(AbstractBuilder<?,?> builder) {
        super(builder);
        this.action = builder.action;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<LastExecutedActionCondition, Builder> {
        @Override protected Builder self() { return this; }
        @Override public LastExecutedActionCondition checkedBuild() { return new LastExecutedActionCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends LastExecutedActionCondition,T extends AbstractBuilder<E,T>> extends LeafCondition.AbstractBuilder<E,T> {
        private GFAction action;

        public T theLastExecutedActionWas(GFAction action) { this.action = action; return self(); }
    }
}
