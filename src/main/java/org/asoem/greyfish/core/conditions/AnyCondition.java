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
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical OR operator.
 * @author christoph
 *
 */
@Tagged("conditions")
public class AnyCondition extends BranchCondition<A> {

    private AnyCondition(Builder builder) {
        super(builder);
    }

    private AnyCondition(AnyCondition condition, DeepCloner map) {
        super(condition, map);
    }

    @Override
    public boolean evaluate() {
        for (ActionCondition condition : getChildConditions())
            if (condition.evaluate())
                return true;
        return false;
    }

    @Override
    public AnyCondition deepClone(DeepCloner cloner) {
        return new AnyCondition(this, cloner);
    }

    private Object writeReplace() {
        return new Builder(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static AnyCondition evaluates(ActionCondition... conditions) {
        return new Builder().add(conditions).build();
    }

    private static final class Builder extends BranchCondition.AbstractBuilder<A, AnyCondition,Builder> implements Serializable {
        private Builder() {
        }

        private Builder(AnyCondition anyCondition) {
            super(anyCondition);
        }

        @Override protected Builder self() { return this; }
        @Override public AnyCondition checkedBuild() { return new AnyCondition(this); }

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
