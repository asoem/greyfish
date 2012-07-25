package org.asoem.greyfish.core.genes;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.simpleframework.xml.Element;

import java.util.Map;

import static com.google.common.base.Preconditions.*;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 11:28
 */
@ClassGroup(tags = {"genes"})
public class MarkovGeneComponent extends AbstractGeneComponent<String> {

    @Element(required = false)
    private Table<String, String, Callback<? super MarkovGeneComponent, Double>> markovMatrix;

    @Element(required = false)
    private Callback<? super MarkovGeneComponent, ? extends String> initialState;

    @Element(required = false)
    private String currentState;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private MarkovGeneComponent() {
    }

    public MarkovGeneComponent(Table<String, String, Callback<? super MarkovGeneComponent, Double>> chain, Callback<? super MarkovGeneComponent, ? extends String> initialState) {
        this.markovMatrix = checkNotNull(chain);
        this.initialState = checkNotNull(initialState);
    }

    public MarkovGeneComponent(AbstractMarkovGeneComponentBuilder<? extends MarkovGeneComponent, ? extends AbstractMarkovGeneComponentBuilder> builder) {
        super(builder);
        this.markovMatrix = builder.markovChain.build();
        this.initialState = builder.initialState;
    }

    @Override
    public void setAllele(Object allele) {
        checkArgument(allele instanceof String, "Expected allele of type String. Actual value: " + allele);
        currentState = (String) allele;
    }

    @Override
    public String mutate(String allele) {
        checkNotNull(allele, "State must not be null");

        if (!markovMatrix.containsRow(allele)) {
            if (markovMatrix.containsColumn(allele)) {
                return allele;
            } else
                throw new IllegalArgumentException("State '" + allele + "' does not match any of the defined states in set {" + Joiner.on(", ").join(Sets.union(markovMatrix.rowKeySet(), markovMatrix.columnKeySet())) + "}");
        }


        final Map<String, Callback<? super MarkovGeneComponent, Double>> row = markovMatrix.row(allele);

        if (row.isEmpty()) {
            return allele;
        }

        double sum = 0;
        double rand = RandomUtils.nextDouble();
        for (Map.Entry<String, Callback<? super MarkovGeneComponent, Double>> cell : row.entrySet()) {
            sum += Callbacks.call(cell.getValue(), MarkovGeneComponent.this);
            if (sum > rand) {
                return cell.getKey();
            }
        }

        return allele;
    }

    @Override
    public Product2<String, String> recombine(String allele1, String allele2) {
        return Tuple2.of(allele1, allele2);
    }

    @Override
    public String createInitialValue() {
        return Callbacks.call(initialState, MarkovGeneComponent.this);
    }

    private MarkovGeneComponent(MarkovGeneComponent markovGene, DeepCloner cloner) {
        super(markovGene, cloner);
        this.markovMatrix = markovGene.markovMatrix;
        this.initialState = markovGene.initialState;
    }

    @Override
    public Class<String> getAlleleClass() {
        return String.class;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MarkovGeneComponent(this, cloner);
    }

    @Override
    public String getAllele() {
        return currentState;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Initial State", TypedValueModels.forField("initialState", this, new TypeToken<Callback<? super MarkovGeneComponent, String>>() {}));
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

    public Table<String, String, Callback<? super MarkovGeneComponent, Double>> getMarkovChain() {
        return markovMatrix;
    }

    public Callback<? super MarkovGeneComponent, ? extends String> getInitialState() {
        return initialState;
    }

    public static MarkovGeneComponentBuilder builder() {
        return new MarkovGeneComponentBuilder();
    }

    public static class MarkovGeneComponentBuilder extends AbstractMarkovGeneComponentBuilder<MarkovGeneComponent, MarkovGeneComponentBuilder> {
        @Override
        protected MarkovGeneComponentBuilder self() {
            return this;
        }

        @Override
        protected MarkovGeneComponent checkedBuild() {
            return new MarkovGeneComponent(this);
        }
    }

    protected abstract static class AbstractMarkovGeneComponentBuilder<T extends MarkovGeneComponent, B extends AbstractComponentBuilder<T, B>> extends AbstractComponentBuilder<T, B> {

        private ImmutableTable.Builder<String, String, Callback<? super MarkovGeneComponent, Double>> markovChain = ImmutableTable.builder();
        private Callback<? super MarkovGeneComponent, ? extends String> initialState;

        public B put(String state1, String state2, Callback<? super MarkovGeneComponent, Double> transitionCallback) {
            markovChain.put(state1, state2, transitionCallback);
            return self();
        }

        public B initialState(Callback<? super MarkovGeneComponent, ? extends String> initialState) {
            this.initialState = checkNotNull(initialState);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            checkState(markovChain != null);
            checkState(initialState != null);
        }
    }
}
