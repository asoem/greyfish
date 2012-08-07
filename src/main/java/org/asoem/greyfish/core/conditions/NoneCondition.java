package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.GFAction;
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
    public boolean apply(GFAction action) {
        for (GFCondition condition : conditions)
            if (condition.apply(action))
                return false;
        return true;
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
