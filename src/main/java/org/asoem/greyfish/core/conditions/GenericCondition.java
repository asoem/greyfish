package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.ArgumentMap;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: christoph
 * Date: 04.05.12
 * Time: 11:47
 */
public class GenericCondition<A extends Agent<A, ?>> extends LeafCondition<A> implements Serializable {

    private final Callback<? super GenericCondition<A>, Boolean> callback;

    private GenericCondition(GenericCondition<A> genericCondition, DeepCloner cloner) {
        super(genericCondition, cloner);
        this.callback = genericCondition.callback;
    }

    private GenericCondition(Builder<A> builder) {
        super(builder);
        this.callback = builder.callback;
    }

    @Override
    public boolean evaluate() {
        return callback.apply(this, ArgumentMap.of());
    }

    @Override
    public GenericCondition<A> deepClone(DeepCloner cloner) {
        return new GenericCondition<A>(this, cloner);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    public Callback<? super GenericCondition, Boolean> getCallback() {
        return callback;
    }

    private Object writeReplace() {
        return new Builder<A>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static <A extends Agent<A, ?>> GenericCondition<A> evaluate(Callback<? super GenericCondition<A>, Boolean> callback) {
        return new Builder<A>().callback(callback).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static final class Builder<A extends Agent<A, ?>> extends LeafCondition.AbstractBuilder<A, GenericCondition<A>, Builder<A>> implements Serializable {
        public Callback<? super GenericCondition<A>, Boolean> callback;

        private Builder() {
        }

        private Builder(GenericCondition<A> genericCondition) {
            super(genericCondition);
            this.callback = genericCondition.callback;
        }

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected GenericCondition<A> checkedBuild() {
            checkState(callback != null, "Cannot build without a callback");
            return new GenericCondition<A>(this);
        }

        public Builder<A> callback(Callback<? super GenericCondition, Boolean> callback) {
            this.callback = checkNotNull(callback);
            return self();
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
