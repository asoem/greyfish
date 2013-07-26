package org.asoem.greyfish.core.traits;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.*;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 11:28
 */
@Tagged("traits")
public class CustomTrait<A extends Agent<A, ?>, T> extends AbstractTrait<A, T> implements Serializable {

    private final Callback<? super CustomTrait<A, T>, T> initializationKernel;

    private final Callback<? super CustomTrait<A, T>, T> mutationKernel;

    private final Callback<? super CustomTrait<A, T>, T> segregationKernel;

    private final Callback<? super CustomTrait<A, T>, Boolean> valueConstraint;

    private final TypeToken<T> typeToken;

    private T value;

    private CustomTrait(final CustomTrait<A, T> customTrait, final DeepCloner cloner) {
        super(customTrait, cloner);
        this.initializationKernel = customTrait.initializationKernel;
        this.mutationKernel = customTrait.mutationKernel;
        this.segregationKernel = customTrait.segregationKernel;
        this.typeToken = customTrait.typeToken;
        this.valueConstraint = customTrait.valueConstraint;
        set(customTrait.value);
    }

    private CustomTrait(final AbstractBuilder<A, T, ? extends CustomTrait<A, T>, ? extends AbstractBuilder<A, T, ?, ?>> builder) {
        super(builder);
        this.initializationKernel = builder.initializationKernel;
        this.typeToken = builder.typeToken;

        if (builder.mutationKernel == null)
            this.mutationKernel = new Callback<CustomTrait<A, T>, T>() {
                @SuppressWarnings("unchecked")
                @Override
                public T apply(final CustomTrait<A, T> caller, final Map<String, ?> args) {
                    return (T) args.get("x");
                }
            };
        else
            this.mutationKernel = builder.mutationKernel;

        if (builder.segregationKernel == null)
            this.segregationKernel = new Callback<CustomTrait<A, T>, T>() {
                @SuppressWarnings("unchecked")
                @Override
                public T apply(final CustomTrait<A, T> caller, final Map<String, ?> args) {
                    return (T) args.get("x");
                }
            };
        else
            this.segregationKernel = builder.segregationKernel;

        if (builder.valueConstraint == null)
            this.valueConstraint = Callbacks.alwaysTrue();
        else
            this.valueConstraint = builder.valueConstraint;

        set(builder.value);
    }

    @Override
    public DeepCloneable deepClone(final DeepCloner cloner) {
        return new CustomTrait<A, T>(this, cloner);
    }

    @Override
    public TypeToken<T> getValueType() {
        return typeToken;
    }

    public Callback<? super CustomTrait<A, T>, T> getInitializationKernel() {
        return initializationKernel;
    }

    public Callback<? super CustomTrait<A, T>, T> getMutationKernel() {
        return mutationKernel;
    }

    public Callback<? super CustomTrait<A, T>, T> getSegregationKernel() {
        return segregationKernel;
    }

    @Override
    public void set(final T value) {
        checkArgument(Callbacks.call(valueConstraint, this));
        this.value = value;
    }

    @Override
    public T mutate(final T allele) {
        return mutationKernel.apply(this, ImmutableMap.of("x", allele));
    }

    @Override
    public T segregate(final T allele1, final T allele2) {
        return segregationKernel.apply(this, ImmutableMap.of("x", allele1, "y", allele2));
    }

    @Override
    public T createInitialValue() {
        return initializationKernel.apply(this, ImmutableMap.<String, Object>of());
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean isHeritable() {
        return true;
    }

    public static <A extends Agent<A, ?>, T> Builder<A, T> builder(final TypeToken<T> typeToken) {
        checkNotNull(typeToken);
        return new Builder<A, T>(typeToken);
    }

    private Object writeReplace() {
        return new Builder<A, T>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException{
        throw new InvalidObjectException("Builder required");
    }

    public static class Builder<A extends Agent<A, ?>, T> extends AbstractBuilder<A, T, CustomTrait<A, T>, Builder<A, T>> implements Serializable {
        private Builder(final TypeToken<T> typeToken) {
            super(typeToken);
        }

        private Builder(final CustomTrait<A, T> quantitativeTrait) {
            super(quantitativeTrait);
        }

        @Override
        protected Builder<A, T> self() {
            return this;
        }

        @Override
        protected CustomTrait<A, T> checkedBuild() {
            return new CustomTrait<A, T>(this);
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return build();
            } catch (IllegalStateException e) {
                throw new InvalidObjectException("Build failed: " + e);
            }
        }

        private static final long serialVersionUID = 0;
    }

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, T, C extends CustomTrait<A, T>, B extends AbstractBuilder<A, T, C, B>> extends AbstractAgentComponent.AbstractBuilder<A, C, B> implements Serializable {

        private final Callback<Object, T> UNSUPPORTED_OPERATION_EXCEPTION = Callbacks.willThrow(new UnsupportedOperationException());

        private final TypeToken<T> typeToken;
        private Callback<? super CustomTrait<A, T>, T> initializationKernel;
        private Callback<? super CustomTrait<A, T>, T> mutationKernel;
        private Callback<? super CustomTrait<A, T>, T> segregationKernel;
        private T value;
        private Callback<? super CustomTrait<A, T>, Boolean> valueConstraint;

        protected AbstractBuilder(final CustomTrait<A, T> customTrait) {
            super(customTrait);
            this.initializationKernel = customTrait.initializationKernel;
            this.mutationKernel = customTrait.mutationKernel;
            this.segregationKernel = customTrait.segregationKernel;
            this.value = customTrait.value;
            this.typeToken = customTrait.typeToken;
            this.valueConstraint = customTrait.valueConstraint;
        }

        protected AbstractBuilder(final TypeToken<T> typeToken) {
            this.typeToken = typeToken;
        }

        public B initialization(final Callback<? super CustomTrait<A, T>, T> callback) {
            this.initializationKernel = checkNotNull(callback);
            return self();
        }

        public B mutation(final Callback<? super CustomTrait<A, T>, T> callback) {
            this.mutationKernel = checkNotNull(callback);
            return self();
        }

        public B segregation(final Callback<? super CustomTrait<A, T>, T> callback) {
            this.segregationKernel = checkNotNull(callback);
            return self();
        }

        public B constraint(final Callback<? super CustomTrait<A, T>, Boolean> callback) {
            this.valueConstraint = checkNotNull(callback);
            return self();
        }

        @Override
        protected void checkBuilder() {
            super.checkBuilder();
            checkState(initializationKernel != null, "initializationKernel must not be null");
        }

        // only used internally for serialization
        protected B value(final T value) {
            this.value = value;
            return self();
        }
    }
}