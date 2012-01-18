package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.base.DeepCloner;

public class NoneCondition extends BranchCondition {

    public NoneCondition(NoneCondition condition, DeepCloner map) {
        super(condition, map);
    }

    @Override
    public boolean apply(final Simulation simulation) {
        switch (conditions.size()) {
            case 0 : return true;
            case 1 : return ! conditions.get(0).apply(simulation);
            case 2 : return ! conditions.get(0).apply(simulation) && ! conditions.get(1).apply(simulation);
            default : return ! Iterables.any(conditions, new Predicate<GFCondition>() {
                @Override
                public boolean apply(GFCondition condition) {
                    return condition.apply(simulation);
                }
            });
        }
    }

    protected NoneCondition(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    public static Builder trueIf() { return new Builder(); }

    @Override
    public NoneCondition deepClone(DeepCloner cloner) {
        return new NoneCondition(this, cloner);
    }

    @SimpleXMLConstructor
    private NoneCondition() {
        this(new Builder());
    }

    public static final class Builder extends AbstractBuilder<NoneCondition, Builder> {
        @Override protected Builder self() { return this; }
        @Override public NoneCondition checkedBuild() { return new NoneCondition(this); }
        public Builder none(GFCondition ... conditions) { return super.add(conditions); }
    }
}
