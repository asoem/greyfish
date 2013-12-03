package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

@Tagged("conditions")
public class NoneCondition<A extends Agent<A, ?>> extends BranchCondition<A> {

    private NoneCondition(final NoneCondition<A> condition, final DeepCloner map) {
        super(condition, map);
    }

    private NoneCondition(final Builder<A> builder) {
        super(builder);
    }

    @Override
    public boolean evaluate() {
        for (final ActionCondition<A> condition : getChildConditions()) {
            if (condition.evaluate()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NoneCondition<A> deepClone(final DeepCloner cloner) {
        return new NoneCondition<A>(this, cloner);
    }

    private Object writeReplace() {
        return new Builder<A>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static <A extends Agent<A, ?>> NoneCondition<A> evaluates(final ActionCondition<A> condition) {
        return new Builder<A>().add(condition).build();
    }

    public static <A extends Agent<A, ?>> NoneCondition<A> evaluates(final ActionCondition<A>... conditions) {
        return new Builder<A>().add(conditions).build();
    }

    public static <A extends Agent<A, ?>> Builder<A> builder() {
        return new Builder<A>();
    }

    public static final class Builder<A extends Agent<A, ?>> extends BranchCondition.AbstractBuilder<A, NoneCondition<A>, Builder<A>> implements Serializable {
        private Builder() {
        }

        private Builder(final NoneCondition<A> noneCondition) {
            super(noneCondition);
        }

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        public NoneCondition<A> checkedBuild() {
            return new NoneCondition<A>(this);
        }

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
