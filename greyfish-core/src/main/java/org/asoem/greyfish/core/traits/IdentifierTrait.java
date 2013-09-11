package org.asoem.greyfish.core.traits;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.math.RandomGenerators;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.*;

/**
 * A Trait which holds "identifier" values
 */
public final class IdentifierTrait<A extends Agent<A, ?>>
        extends AbstractTrait<A, String>
        implements Serializable, AgentTrait<A, String> {

    private static final TypeToken<String> STRING_TYPE_TOKEN = TypeToken.of(String.class);
    private final Table<String, String, Callback<? super AgentTrait<A, String>, Double>> mutationTable;
    private final Callback<? super AgentTrait<A, String>, String> initializationKernel;
    private final Callback<? super AgentTrait<A, String>, String> segregationKernel;
    @Nullable
    private String state;

    private IdentifierTrait(final AbstractBuilder<A, ? extends AgentTrait<A, String>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
        this.mutationTable = ImmutableTable.copyOf(builder.mutationTable);
        this.initializationKernel = builder.initializationKernel;
        this.segregationKernel = builder.segregationKernel;
        this.state = builder.state;
    }

    private IdentifierTrait(final IdentifierTrait<A> discreteTrait, final DeepCloner cloner) {
        super(discreteTrait, cloner);
        this.mutationTable = discreteTrait.mutationTable;
        this.initializationKernel = discreteTrait.initializationKernel;
        this.segregationKernel = discreteTrait.segregationKernel;
        this.state = discreteTrait.state;
    }

    @Override
    public void set(final String value) {
        checkValidState(value);
        state = value;
    }

    @Override
    public String mutate(final String allele) {
        checkValidState(allele);

        final Map<String, Callback<? super AgentTrait<A, String>, Double>> row = mutationTable.row(allele);

        if (row.isEmpty()) {
            assert mutationTable.containsColumn(allele);
            return allele;
        }

        double sum = 0;
        final double rand = RandomGenerators.rng().nextDouble();
        for (final Map.Entry<String, Callback<? super AgentTrait<A, String>, Double>> cell : row.entrySet()) {
            final double transitionProbability = Callbacks.call(cell.getValue(), IdentifierTrait.this);
            if (transitionProbability < 0)
                throw new AssertionError("Every transition probability should be >= 0, was " + transitionProbability);

            sum += transitionProbability;

            if (sum > 1)
                throw new AssertionError("Sum of probabilities is expected to not exceed 1, was " + sum);
            if (sum > rand) {
                return cell.getKey();
            }
        }

        return allele;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private void checkValidState(final Object state) {
        checkArgument(getPossibleValues().contains(state),
                "State '{}' does not match any of the valid states [{}]",
                state, Joiner.on(", ").join(getPossibleValues()));
    }

    @Override
    public String segregate(final String allele1, final String allele2) {
        return segregationKernel.apply(this, ImmutableMap.of("x", allele1, "y", allele2));
    }

    @Override
    public String createInitialValue() {
        return Callbacks.call(initializationKernel, IdentifierTrait.this);
    }

    @Override
    public TypeToken<String> getValueType() {
        return STRING_TYPE_TOKEN;
    }

    @Override
    public AgentTrait<A, String> deepClone(final DeepCloner cloner) {
        return new IdentifierTrait<A>(this, cloner);
    }

    @Override
    public String get() {
        checkState(state != null, "Allele has null state, trait was not initialized");
        return state;
    }

    @Override
    public void initialize() {
        super.initialize();
        set(createInitialValue());
    }

    public Table<String, String, Callback<? super AgentTrait<A, String>, Double>> getMarkovChain() {
        return mutationTable;
    }

    public Callback<? super AgentTrait<A, String>, ? extends String> getInitializationKernel() {
        return initializationKernel;
    }

    public Callback<? super AgentTrait<A, String>, String> getSegregationKernel() {
        return segregationKernel;
    }

    private Object writeReplace() {
        return new Builder<A>(this);
    }

    public Set<String> getPossibleValues() {
        return Sets.union(mutationTable.columnKeySet(), mutationTable.rowKeySet());
    }

    @Override
    public boolean isHeritable() {
        return true;
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static <A extends Agent<A, ?>> Builder<A> builder() {
        return new Builder<A>();
    }

    public static class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, IdentifierTrait<A>, Builder<A>> implements Serializable {
        private Builder() {}

        private Builder(final IdentifierTrait<A> discreteTrait) {
            super(discreteTrait);
        }

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected IdentifierTrait<A> checkedBuild() {
            return new IdentifierTrait<A>(this);
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

    protected abstract static class AbstractBuilder<A extends Agent<A, ?>, T extends IdentifierTrait<A>, B extends AbstractBuilder<A, T, B>> extends AbstractAgentComponent.AbstractBuilder<A, T, B> implements Serializable {

        private final Table<String, String, Callback<? super AgentTrait<A, String>, Double>> mutationTable;
        private Callback<? super AgentTrait<A, String>, String> initializationKernel;
        private Callback<? super AgentTrait<A, String>, String> segregationKernel;
        private String state;

        protected AbstractBuilder() {
            this.mutationTable = HashBasedTable.create();
        }

        protected AbstractBuilder(final IdentifierTrait<A> discreteTrait) {
            super(discreteTrait);
            this.mutationTable = HashBasedTable.create(discreteTrait.mutationTable);
            this.segregationKernel = discreteTrait.segregationKernel;
            this.initializationKernel = discreteTrait.initializationKernel;
            this.state = discreteTrait.state;
        }

        public B addMutation(final String state1, final String state2, final Callback<? super AgentTrait<A, String>, Double> transitionCallback) {
            mutationTable.put(state1, state2, transitionCallback);
            return self();
        }

        public B addMutation(final String state1, final String state2, final double p) {
            addMutation(state1, state2, Callbacks.constant(p));
            return self();
        }

        public B initialization(final Callback<? super AgentTrait<A, String>, String> callback) {
            this.initializationKernel = checkNotNull(callback);
            return self();
        }

        public B segregation(final Callback<? super AgentTrait<A, String>, String> callback) {
            this.segregationKernel = checkNotNull(callback);
            return self();
        }

        @Override
        protected void checkBuilder() {
            super.checkBuilder();
            if (initializationKernel == null)
                throw new IllegalStateException();
            if (segregationKernel == null)
                segregationKernel = new Callback<AgentTrait<A, String>, String>() {
                    @Override
                    public String apply(final AgentTrait<A, String> caller, final Map<String, ?> args) {
                        return (String) RandomGenerators.sample(RandomGenerators.rng(), args.get("x"), args.get("y"));
                    }
                };
        }
    }
}