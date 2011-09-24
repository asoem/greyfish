package org.asoem.greyfish.core.conditions;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.FiniteSetValueAdaptor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

public class LastExecutedActionCondition extends LeafCondition {

    @Element(name="actions", required=false)
    private GFAction action;

    protected LastExecutedActionCondition(LastExecutedActionCondition condition, DeepCloner map) {
        super(condition, map);
        this.action = map.continueWith(condition.action, GFAction.class);
    }

    @Override
    public boolean evaluate(Simulation simulation) {
        return isSameAction(action, agent.getLastExecutedAction());
    }

    private static boolean isSameAction(GFAction a1, GFAction a2) {
        return Objects.equal(a1, a2);
//        return Equivalences.equals().equivalent(a1, a2);  // TODO: Make comparison more strict if we can guaranty theAction to be not null
    }

    @Override
    public void configure(ConfigurationHandler e) {
        e.add(new FiniteSetValueAdaptor<GFAction>("Action", GFAction.class) {
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
                return Iterables.filter(getAllComponents(), GFAction.class);
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

    protected LastExecutedActionCondition(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.action = builder.action;
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<LastExecutedActionCondition> {
        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override public LastExecutedActionCondition build() { return new LastExecutedActionCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LeafCondition.AbstractBuilder<T> {
        private GFAction action;

        public T theLastExecutedActionWas(GFAction action) { this.action = action; return self(); }
    }
}
