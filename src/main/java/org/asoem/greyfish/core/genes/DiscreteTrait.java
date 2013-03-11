package org.asoem.greyfish.core.genes;

import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.math.RandomUtils;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.*;
import static org.asoem.greyfish.utils.math.RandomUtils.sample;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 11:28
 */
@Tagged("traits")
public class DiscreteTrait<A extends Agent<A, ?>> extends AbstractTrait<A, String> implements Serializable {

    private Table<String, String, Callback<? super DiscreteTrait<A>, Double>> mutationTable;
    private Callback<? super DiscreteTrait<A>, String> initializationKernel;
    private Callback<? super DiscreteTrait<A>, String> segregationKernel;
    @Nullable
    private String state;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private DiscreteTrait() {}

    private DiscreteTrait(AbstractBuilder<A, ? extends DiscreteTrait<A>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
        this.mutationTable = ImmutableTable.copyOf(builder.mutationTable);
        this.initializationKernel = builder.initializationKernel;
        this.segregationKernel = builder.segregationKernel;
        this.state = builder.state;
    }

    private DiscreteTrait(DiscreteTrait<A> discreteTrait, DeepCloner cloner) {
        super(discreteTrait, cloner);
        this.mutationTable = discreteTrait.mutationTable;
        this.initializationKernel = discreteTrait.initializationKernel;
        this.segregationKernel = discreteTrait.segregationKernel;
        this.state = discreteTrait.state;
    }

    @Override
    public void setAllele(Object allele) {
        checkValidState(allele);
        state = (String) allele;
    }

    @Override
    public String mutate(String allele) {
        checkValidState(allele);

        final Map<String, Callback<? super DiscreteTrait<A>, Double>> row = mutationTable.row(allele);

        if (row.isEmpty()) {
            assert mutationTable.containsColumn(allele);
            return allele;
        }

        double sum = 0;
        double rand = RandomUtils.nextDouble();
        for (Map.Entry<String, Callback<? super DiscreteTrait<A>, Double>> cell : row.entrySet()) {
            final double transitionProbability = Callbacks.call(cell.getValue(), DiscreteTrait.this);
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

    private void checkValidState(Object allele) {
        checkArgument(mutationTable.containsRow(allele) || mutationTable.containsColumn(allele),
                "State '{}' does not match any of the defined states [{}]", allele, Joiner.on(", ").join(Sets.union(mutationTable.rowKeySet(), mutationTable.columnKeySet())));
    }

    @Override
    public String segregate(String allele1, String allele2) {
        return segregationKernel.apply(this, ArgumentMap.of("x", allele1, "y", allele2));
    }

    @Override
    public String createInitialValue() {
        return Callbacks.call(initializationKernel, DiscreteTrait.this);
    }

    @Override
    public Class<? super String> getValueClass() {
        return String.class;
    }

    @Override
    public DiscreteTrait<A> deepClone(DeepCloner cloner) {
        return new DiscreteTrait<A>(this, cloner);
    }

    @Override
    public String getValue() {
        checkState(state != null, "Allele has null state, trait was not initialized");
        return state;
    }

    @Override
    public void initialize() {
        super.initialize();
        setAllele(createInitialValue());
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Initial State", TypedValueModels.forField("initializationKernel", this, new TypeToken<Callback<? super DiscreteTrait<A>, String>>() {}));
        /*
        e.add("Transition Rules", new AbstractTypedValueModel<String>() {
            @Override
            protected void set(String arg0) {
                markovChain = EvaluatingMarkovChain.parse(arg0, EXPRESSION_FACTORY);
            }

            @Override
            public String get() {
                return markovChain == null ? "" : markovChain.toRule();
            }
        });
        */
    }

    public Table<String, String, Callback<? super DiscreteTrait<A>, Double>> getMarkovChain() {
        return mutationTable;
    }

    public Callback<? super DiscreteTrait<A>, ? extends String> getInitializationKernel() {
        return initializationKernel;
    }

    public Callback<? super DiscreteTrait<A>, String> getSegregationKernel() {
        return segregationKernel;
    }

    private Object writeReplace() {
        return new Builder<A>(this);
    }

    public Set<String> getStates() {
        return Sets.union(mutationTable.columnKeySet(), mutationTable.rowKeySet());
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static <A extends Agent<A, ?>> Builder<A> builder() {
        return new Builder<A>();
    }

    public static class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, DiscreteTrait<A>, Builder<A>> implements Serializable {
        private Builder() {}

        private Builder(DiscreteTrait<A> discreteTrait) {
            super(discreteTrait);
        }

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected DiscreteTrait<A> checkedBuild() {
            return new DiscreteTrait<A>(this);
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

    protected abstract static class AbstractBuilder<A extends Agent<A, ?>, T extends DiscreteTrait<A>, B extends AbstractBuilder<A, T, B>> extends AbstractAgentComponent.AbstractBuilder<A, T, B> implements Serializable {

        private final Table<String, String, Callback<? super DiscreteTrait<A>, Double>> mutationTable;
        private Callback<? super DiscreteTrait<A>, String> initializationKernel;
        private Callback<? super DiscreteTrait<A>, String> segregationKernel;
        private String state;

        protected AbstractBuilder() {
            this.mutationTable = HashBasedTable.create();
        }

        protected AbstractBuilder(DiscreteTrait<A> discreteTrait) {
            super(discreteTrait);
            this.mutationTable = HashBasedTable.create(discreteTrait.mutationTable);
            this.segregationKernel = discreteTrait.segregationKernel;
            this.initializationKernel = discreteTrait.initializationKernel;
            this.state = discreteTrait.state;
        }

        public B addMutation(String state1, String state2, Callback<? super DiscreteTrait<A>, Double> transitionCallback) {
            mutationTable.put(state1, state2, transitionCallback);
            return self();
        }

        public B addMutation(String state1, String state2, double p) {
            addMutation(state1, state2, Callbacks.constant(p));
            return self();
        }

        public B initialization(Callback<? super DiscreteTrait<A>, String> callback) {
            this.initializationKernel = checkNotNull(callback);
            return self();
        }

        public B segregation(Callback<? super DiscreteTrait<A>, String> callback) {
            this.segregationKernel = checkNotNull(callback);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            if (initializationKernel == null)
                throw new IllegalStateException();
            if (segregationKernel == null)
                segregationKernel = new Callback<DiscreteTrait<A>, String>() {
                    @Override
                    public String apply(DiscreteTrait<A> caller, Arguments args) {
                        return (String) sample(args.get("x"), args.get("y"));
                    }
                };
        }
    }
}