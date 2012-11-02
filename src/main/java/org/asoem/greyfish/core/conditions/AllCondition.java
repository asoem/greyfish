/**
 *
 */
package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical AND operator.
 * @author christoph
 *
 */
@Tagged("conditions")
public class AllCondition extends BranchCondition {

    private AllCondition(AllCondition cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    private AllCondition(Builder builder) {
        super(builder);
    }

    @Override
    public boolean evaluate() {
        for (ActionCondition condition : getChildConditions())
            if (!condition.evaluate())
                return false;
        return true;
    }

    @Override
    public AllCondition deepClone(DeepCloner cloner) {
        return new AllCondition(this, cloner);
    }

    private Object writeReplace() {
        return new Builder(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static AllCondition evaluates(ActionCondition... conditions) {
        return builder().add(conditions).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static final class Builder extends BranchCondition.AbstractBuilder<AllCondition, Builder> implements Serializable {
        private Builder() {
        }

        private Builder(AllCondition allCondition) {
            super(allCondition);
        }

        @Override protected Builder self() { return this; }
        @Override protected AllCondition checkedBuild() { return new AllCondition(this); }

        private Object readResolve() throws ObjectStreamException {
            try {
                return build();
            } catch (IllegalStateException e) {
                throw new InvalidObjectException("Build failed with: " + e.getMessage());
            }
        }

        private static final long serialVersionUID = 0;
    }
}
