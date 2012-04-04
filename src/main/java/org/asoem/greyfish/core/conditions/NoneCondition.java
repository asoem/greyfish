package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;

@ClassGroup(tags="conditions")
public class NoneCondition extends BranchCondition {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
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
    public boolean evaluate(final Simulation simulation) {
        switch (conditions.size()) {
            case 0 : return true;
            case 1 : return ! conditions.get(0).evaluate(simulation);
            case 2 : return ! conditions.get(0).evaluate(simulation) && ! conditions.get(1).evaluate(simulation);
            default : return ! Iterables.any(conditions, new Predicate<GFCondition>() {
                @Override
                public boolean apply(GFCondition condition) {
                    return condition.evaluate(simulation);
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
