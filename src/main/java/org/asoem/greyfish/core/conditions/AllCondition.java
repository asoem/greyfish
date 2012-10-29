/**
 *
 */
package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical AND operator.
 * @author christoph
 *
 */
@Tagged("conditions")
public class AllCondition extends BranchCondition {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public AllCondition() {}

    protected AllCondition(AllCondition cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    protected AllCondition(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    @Override
    public boolean apply(AgentAction action) {
        for (ActionCondition condition : this)
            if (!condition.apply(action))
                return false;
        return true;
    }

    @Override
    public AllCondition deepClone(DeepCloner cloner) {
        return new AllCondition(this, cloner);
    }

    public static AllCondition evaluates(ActionCondition... conditions) {
        return new Builder().add(conditions).build(); }

    private static final class Builder extends AbstractBuilder<AllCondition, Builder> {

        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override protected AllCondition checkedBuild() { return new AllCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends AllCondition, T extends AbstractBuilder<E,T>> extends BranchCondition.AbstractBuilder<E,T> {
    }
}
