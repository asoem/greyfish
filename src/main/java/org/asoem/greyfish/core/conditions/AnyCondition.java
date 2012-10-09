/**
 * 
 */
package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical OR operator.
 * @author christoph
 *
 */
@Tagged("conditions")
public class AnyCondition extends BranchCondition {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public AnyCondition() {
        this(new Builder());
    }

    protected AnyCondition(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    public AnyCondition(AnyCondition condition, DeepCloner map) {
        super(condition, map);
    }

    @Override
    public boolean apply(AgentAction action) {
        for (ActionCondition condition : conditions)
            if (condition.apply(action))
                return true;
        return false;
    }

    @Override
    public AnyCondition deepClone(DeepCloner cloner) {
        return new AnyCondition(this, cloner);
    }

    public static final class Builder extends AbstractBuilder<AnyCondition,Builder> {
        @Override protected Builder self() { return this; }
        @Override public AnyCondition checkedBuild() { return new AnyCondition(this); }
    }
}
