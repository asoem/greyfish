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
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical AND operator.
 * @author christoph
 *
 */
@Tagged("conditions")
public class AllCondition<A extends Agent<A, ?>> extends BranchCondition<A> {

    private AllCondition(AllCondition<A> cloneable, CloneMap map) {
        super(cloneable, map);
    }

    private AllCondition(Builder<A> builder) {
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
    public AllCondition<A> deepClone(CloneMap cloneMap) {
        return new AllCondition<A>(this, cloneMap);
    }

    private Object writeReplace() {
        return new Builder<A>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static <A extends Agent<A, ?>> AllCondition<A> evaluates(ActionCondition<A> condition1, ActionCondition<A> condition2) {
        return new Builder<A>().add(condition1).add(condition2).build();
    }

    public static <A extends Agent<A, ?>> AllCondition<A> evaluates(ActionCondition<A> condition1, ActionCondition<A> condition2, ActionCondition<A> condition3) {
        return new Builder<A>().add(condition1).add(condition2).add(condition3).build();
    }

    public static <A extends Agent<A, ?>> AllCondition<A> evaluates(ActionCondition<A>... conditions) {
        return new Builder<A>().add(conditions).build();
    }

    public static <A extends Agent<A, ?>> Builder<A> builder() {
        return new Builder<A>();
    }

    private static final class Builder<A extends Agent<A, ?>> extends BranchCondition.AbstractBuilder<A, AllCondition<A>, Builder<A>> implements Serializable {
        private Builder() {
        }

        private Builder(AllCondition<A> allCondition) {
            super(allCondition);
        }

        @Override protected Builder<A> self() { return this; }
        @Override protected AllCondition<A> checkedBuild() { return new AllCondition<A>(this); }

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
