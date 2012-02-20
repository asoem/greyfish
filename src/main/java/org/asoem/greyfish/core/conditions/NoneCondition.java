package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;

@ClassGroup(tags="conditions")
public class NoneCondition extends BranchCondition {

    @SimpleXMLConstructor
    public NoneCondition() {
        this(new Builder());
    }

    protected NoneCondition(NoneCondition condition, DeepCloner map) {
        super(condition, map);
    }

    protected NoneCondition(AbstractBuilder<?,?> builder) {
        super(builder);
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

    @Override
    public NoneCondition deepClone(DeepCloner cloner) {
        return new NoneCondition(this, cloner);
    }

    public static NoneCondition evaluates(GFCondition... conditions) { return new Builder().add(conditions).build(); }

    public static final class Builder extends AbstractBuilder<NoneCondition, Builder> {
        @Override protected Builder self() { return this; }
        @Override public NoneCondition checkedBuild() { return new NoneCondition(this); }
    }
}
