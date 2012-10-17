package org.asoem.greyfish.core.genes;

import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
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

import static com.google.common.base.Preconditions.*;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 11:28
 */
@Tagged("traits")
public class DiscreteTrait extends AbstractTrait<String> implements Serializable {

    private Table<String, String, Callback<? super DiscreteTrait, Double>> mutationTable;
    private Callback<? super DiscreteTrait, String> initializationKernel;
    private Callback<? super DiscreteTrait, String> segregationKernel;
    @Nullable
    private String currentState;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private DiscreteTrait() {}

    private DiscreteTrait(AbstractBuilder<? extends DiscreteTrait, ? extends AbstractBuilder> builder) {
        super(builder);
        this.mutationTable = builder.mutationTable;
        this.initializationKernel = builder.initializationKernel;
        this.segregationKernel = builder.segregationKernel;
    }

    private DiscreteTrait(DiscreteTrait markovGene, DeepCloner cloner) {
        super(markovGene, cloner);
        this.mutationTable = markovGene.mutationTable;
        this.initializationKernel = markovGene.initializationKernel;
        this.segregationKernel = markovGene.segregationKernel;
    }

    @Override
    public void setAllele(Object allele) {
        checkValidState(allele);
        currentState = (String) allele;
    }

    @Override
    public String mutate(String allele) {
        checkValidState(allele);

        final Map<String, Callback<? super DiscreteTrait, Double>> row = mutationTable.row(allele);

        if (row.isEmpty()) {
            assert mutationTable.containsColumn(allele);
            return allele;
        }

        double sum = 0;
        double rand = RandomUtils.nextDouble();
        for (Map.Entry<String, Callback<? super DiscreteTrait, Double>> cell : row.entrySet()) {
            sum += Callbacks.call(cell.getValue(), DiscreteTrait.this);
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
    public Class<String> getAlleleClass() {
        return String.class;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new DiscreteTrait(this, cloner);
    }

    @Override
    public String getAllele() {
        checkState(currentState != null, "Allele has null state, trait was not initialized");
        return currentState;
    }

    @Override
    public void initialize() {
        super.initialize();
        setAllele(createInitialValue());
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Initial State", TypedValueModels.forField("initializationKernel", this, new TypeToken<Callback<? super DiscreteTrait, String>>() {}));
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

    public Table<String, String, Callback<? super DiscreteTrait, Double>> getMarkovChain() {
        return mutationTable;
    }

    public Callback<? super DiscreteTrait, ? extends String> getInitializationKernel() {
        return initializationKernel;
    }

    public Callback<? super DiscreteTrait, String> getSegregationKernel() {
        return segregationKernel;
    }

    private Object writeReplace() {
        return builder()
                .initialization(initializationKernel)
                .segregation(segregationKernel)
                .mutation(mutationTable)
                .name(getName());
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiscreteTrait)) return false;
        if (!super.equals(o)) return false;

        DiscreteTrait that = (DiscreteTrait) o;

        if (currentState != null ? !currentState.equals(that.currentState) : that.currentState != null) return false;
        if (initializationKernel != null ? !initializationKernel.equals(that.initializationKernel) : that.initializationKernel != null)
            return false;
        if (mutationTable != null ? !mutationTable.equals(that.mutationTable) : that.mutationTable != null) return false;
        if (segregationKernel != null ? !segregationKernel.equals(that.segregationKernel) : that.segregationKernel != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mutationTable != null ? mutationTable.hashCode() : 0);
        result = 31 * result + (initializationKernel != null ? initializationKernel.hashCode() : 0);
        result = 31 * result + (segregationKernel != null ? segregationKernel.hashCode() : 0);
        result = 31 * result + (currentState != null ? currentState.hashCode() : 0);
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<DiscreteTrait, Builder> implements Serializable {
        private Builder() {}

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected DiscreteTrait checkedBuild() {
            return new DiscreteTrait(this);
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

    protected abstract static class AbstractBuilder<T extends DiscreteTrait, B extends AbstractBuilder<T, B>> extends AbstractAgentComponent.AbstractBuilder<T, B> implements Serializable {

        private Table<String, String, Callback<? super DiscreteTrait, Double>> mutationTable = HashBasedTable.create();
        private Callback<? super DiscreteTrait, String> initializationKernel = Callbacks.willThrow(new UnsupportedOperationException());
        private Callback<? super DiscreteTrait, String> segregationKernel = Callbacks.willThrow(new UnsupportedOperationException());

        public B addMutation(String state1, String state2, Callback<? super DiscreteTrait, Double> transitionCallback) {
            mutationTable.put(state1, state2, transitionCallback);
            return self();
        }

        public B initialization(Callback<? super DiscreteTrait, String> callback) {
            this.initializationKernel = checkNotNull(callback);
            return self();
        }

        public B segregation(Callback<? super DiscreteTrait, String> callback) {
            this.segregationKernel = checkNotNull(callback);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            this.mutationTable = ImmutableTable.copyOf(mutationTable);
        }

        public B mutation(Table<String, String, Callback<? super DiscreteTrait, Double>> table) {
            mutationTable.putAll(table);
            return self();
        }
    }
}