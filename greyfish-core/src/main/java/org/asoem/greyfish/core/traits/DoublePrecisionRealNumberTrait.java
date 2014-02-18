package org.asoem.greyfish.core.traits;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.properties.AbstractAgentProperty;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.Tagged;
import org.asoem.greyfish.utils.collect.Product2;
import org.asoem.greyfish.utils.collect.Tuple2;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

@Tagged("traits")
public class DoublePrecisionRealNumberTrait<A extends Agent<?>, C extends AgentContext<A>>
        extends AbstractAgentTrait<C, Double>
        implements AgentTrait<C, Double> {

    private static final TypeToken<Double> DOUBLE_TYPE_TOKEN = TypeToken.of(Double.class);

    private final Callback<? super DoublePrecisionRealNumberTrait<A, C>, Double> initializationKernel;

    private final Callback<? super DoublePrecisionRealNumberTrait<A, C>, Double> mutationKernel;

    private final Callback<? super DoublePrecisionRealNumberTrait<A, C>, Double> segregationKernel;

    private double value = 0.0;
    @Nullable
    private A agent;

    private DoublePrecisionRealNumberTrait(final AbstractBuilder<A, DoublePrecisionRealNumberTrait<A, C>, ?, C> builder) {
        super(builder);
        this.initializationKernel = builder.initializationKernel;
        this.mutationKernel = builder.mutationKernel;
        this.segregationKernel = builder.segregationKernel;
        this.value = builder.value;
    }

    public Callback<? super DoublePrecisionRealNumberTrait<A, C>, Double> getInitializationKernel() {
        return initializationKernel;
    }

    public Callback<? super DoublePrecisionRealNumberTrait<A, C>, Double> getMutationKernel() {
        return mutationKernel;
    }

    @Override
    public Double transform(final C context, final Double value) {
        checkNotNull(value);
        return mutationKernel.apply(this, ImmutableMap.of("x", value));
    }

    @Override
    public Product2<Double, Double> transform(final C context, final Double allele1, final Double allele2) {
        Double apply = segregationKernel.apply(this, ImmutableMap.of("x", allele1, "y", allele2));
        return Tuple2.of(apply, apply);
    }

    public Double value(final C context) {
        return value;
    }

    public Callback<? super DoublePrecisionRealNumberTrait<A, C>, Double> getSegregationKernel() {
        return segregationKernel;
    }

    public static <A extends Agent<?>, C extends AgentContext<A>> Builder<A, C> builder() {
        return new Builder<A, C>();
    }

    private Object writeReplace() {
        return new Builder<A, C>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    /**
     * @return this components optional {@code Agent}
     */
    public Optional<A> agent() {
        return Optional.fromNullable(agent);
    }

    public final void setAgent(@Nullable final A agent) {
        this.agent = agent;
    }

    public static class Builder<
            A extends Agent<?>,
            C extends AgentContext<A>>
            extends AbstractBuilder<A, DoublePrecisionRealNumberTrait<A, C>, Builder<A, C>, C>
            implements Serializable {
        private Builder() {
        }

        private Builder(final DoublePrecisionRealNumberTrait<A, C> quantitativeTrait) {
            super(quantitativeTrait);
        }

        @Override
        protected Builder<A, C> self() {
            return this;
        }

        @Override
        protected DoublePrecisionRealNumberTrait<A, C> checkedBuild() {
            return new DoublePrecisionRealNumberTrait<A, C>(this);
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

    protected abstract static class AbstractBuilder<
            A extends Agent<?>,
            C extends DoublePrecisionRealNumberTrait<A, AC>,
            B extends AbstractBuilder<A, C, B, AC>,
            AC extends AgentContext<A>>
            extends AbstractAgentProperty.AbstractBuilder<C, B>
            implements Serializable {

        private static final Callback<Object, Double> DEFAULT_INITIALIZATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());
        private static final Callback<Object, Double> DEFAULT_MUTATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());
        private static final Callback<Object, Double> DEFAULT_SEGREGATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());

        private Callback<? super DoublePrecisionRealNumberTrait<A, AC>, Double> initializationKernel = DEFAULT_INITIALIZATION_KERNEL;
        private Callback<? super DoublePrecisionRealNumberTrait<A, AC>, Double> mutationKernel = DEFAULT_MUTATION_KERNEL;
        private Callback<? super DoublePrecisionRealNumberTrait<A, AC>, Double> segregationKernel = DEFAULT_SEGREGATION_KERNEL;
        private double value;

        protected AbstractBuilder(final DoublePrecisionRealNumberTrait<A, AC> quantitativeTrait) {
            this.initializationKernel = quantitativeTrait.initializationKernel;
            this.mutationKernel = quantitativeTrait.mutationKernel;
            this.segregationKernel = quantitativeTrait.segregationKernel;
            this.value = quantitativeTrait.value;
        }

        protected AbstractBuilder() {
        }

        public B initialization(final Callback<? super DoublePrecisionRealNumberTrait<A, AC>, Double> callback) {
            this.initializationKernel = checkNotNull(callback);
            return self();
        }

        public B mutation(final Callback<? super DoublePrecisionRealNumberTrait<A, AC>, Double> callback) {
            this.mutationKernel = checkNotNull(callback);
            return self();
        }

        public B segregation(final Callback<? super DoublePrecisionRealNumberTrait<A, AC>, Double> callback) {
            this.segregationKernel = checkNotNull(callback);
            return self();
        }

        // only used internally for serialization
        protected B value(final double value) {
            this.value = value;
            return self();
        }
    }
}
