/**
 * 
 */
package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.CloneMap;
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
public class AnyCondition<A extends Agent<A, ?>> extends BranchCondition<A> {

    private AnyCondition(Builder<A> builder) {
        super(builder);
    }

    private AnyCondition(AnyCondition<A> condition, CloneMap map) {
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
    public AnyCondition<A> deepClone(CloneMap cloneMap) {
        return new AnyCondition<A>(this, cloneMap);
    }

    private Object writeReplace() {
        return new Builder<A>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static <A extends Agent<A, ?>> Builder<A> builder() {
        return new Builder<A>();
    }

    public static <A extends Agent<A, ?>> AnyCondition<A> evaluates(ActionCondition<A> ... conditions) {
        return new Builder<A>().add(conditions).build();
    }

    private static final class Builder<A extends Agent<A, ?>> extends BranchCondition.AbstractBuilder<A, AnyCondition<A>,Builder<A>> implements Serializable {
        private Builder() {
        }

        private Builder(AnyCondition<A> anyCondition) {
            super(anyCondition);
        }

        @Override protected Builder<A> self() { return this; }
        @Override public AnyCondition<A> checkedBuild() { return new AnyCondition<A>(this); }

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
