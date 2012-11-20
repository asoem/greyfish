package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.base.ArgumentMap;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.space.Object2D;

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
public class GenericCondition<A extends Agent<S, A, P>, S extends Simulation<S, A, Z, P>, Z extends Space2D<A, P>, P extends Object2D> extends LeafCondition<A,S,Z,P> implements Serializable {

    private final Callback<? super GenericCondition<A,S,Z,P>, Boolean> callback;

    private GenericCondition(GenericCondition<A,S,Z,P> genericCondition, DeepCloner cloner) {
        super(genericCondition, cloner);
        this.callback = genericCondition.callback;
    }

    private GenericCondition(Builder builder) {
        super(builder);
        this.callback = builder.callback;
    }

    @Override
    public boolean evaluate() {
        return callback.apply(this, ArgumentMap.of());
    }

    @Override
    public GenericCondition deepClone(DeepCloner cloner) {
        return new GenericCondition(this, cloner);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    public Callback<? super GenericCondition, Boolean> getCallback() {
        return callback;
    }

    private Object writeReplace() {
        return new Builder(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static GenericCondition evaluate(Callback<? super GenericCondition, Boolean> callback) {
        return builder().callback(callback).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static final class Builder extends LeafCondition.AbstractBuilder<GenericCondition, Builder> implements Serializable {
        public Callback<? super GenericCondition, Boolean> callback;

        private Builder() {
        }

        private Builder(GenericCondition genericCondition) {
            super(genericCondition);
            this.callback = genericCondition.callback;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected GenericCondition checkedBuild() {
            checkState(callback != null, "Cannot build without a callback");
            return new GenericCondition(this);
        }

        public Builder callback(Callback<? super GenericCondition, Boolean> callback) {
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
