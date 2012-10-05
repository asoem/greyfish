package org.asoem.greyfish.core.genes;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.google.common.base.Preconditions.*;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 11:28
 */
@ClassGroup(tags = {"traits"})
public class DiscreteTrait extends AbstractTrait<String> {

    @Element(required = false)
    private Table<String, String, Callback<? super DiscreteTrait, Double>> markovMatrix;

    @Element(required = false)
    private Callback<? super DiscreteTrait, String> initializationKernel;

    @Element
    private Callback<? super DiscreteTrait, String> segregationKernel;

    @Element(required = false)
    private String currentState;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private DiscreteTrait() {
    }

    public DiscreteTrait(Table<String, String, Callback<? super DiscreteTrait, Double>> chain, Callback<? super DiscreteTrait, String> initialState) {
        this.markovMatrix = checkNotNull(chain);
        this.initializationKernel = checkNotNull(initialState);
    }

    public DiscreteTrait(AbstractBuilder<? extends DiscreteTrait, ? extends AbstractBuilder> builder) {
        super(builder);
        this.markovMatrix = builder.markovChain.build();
        this.initializationKernel = builder.initializationKernel;
        this.segregationKernel = builder.segregationKernel;
    }

    private DiscreteTrait(DiscreteTrait markovGene, DeepCloner cloner) {
        super(markovGene, cloner);
        this.markovMatrix = markovGene.markovMatrix;
        this.initializationKernel = markovGene.initializationKernel;
        this.segregationKernel = markovGene.segregationKernel;
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


        final Map<String, Callback<? super DiscreteTrait, Double>> row = markovMatrix.row(allele);

        if (row.isEmpty()) {
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
        return currentState;
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
        return markovMatrix;
    }

    public Callback<? super DiscreteTrait, ? extends String> getInitializationKernel() {
        return initializationKernel;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<DiscreteTrait, Builder> {
        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected DiscreteTrait checkedBuild() {
            return new DiscreteTrait(this);
        }
    }

    protected abstract static class AbstractBuilder<T extends DiscreteTrait, B extends AbstractBuilder<T, B>> extends AbstractAgentComponent.AbstractBuilder<T, B> {

        private static final Callback<Object, String> DEFAULT_SEGREGATION_KERNEL = new Callback<Object, String>() {
            @Override
            public String apply(Object caller, Arguments arguments) {
                return (String) RandomUtils.sample(arguments.get("x"), arguments.get("y"));
            }
        };
        private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBuilder.class);

        private ImmutableTable.Builder<String, String, Callback<? super DiscreteTrait, Double>> markovChain = ImmutableTable.builder();
        private Callback<? super DiscreteTrait, String> initializationKernel;
        private Callback<? super DiscreteTrait, String> segregationKernel = DEFAULT_SEGREGATION_KERNEL;

        public B addMutation(String state1, String state2, Callback<? super DiscreteTrait, Double> transitionCallback) {
            markovChain.put(state1, state2, transitionCallback);
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
            checkState(markovChain != null);
            checkState(initializationKernel != null);
            if (segregationKernel == DEFAULT_SEGREGATION_KERNEL)
                LOGGER.warn("Builder uses default segregation kernel for {}: {}", name, DEFAULT_SEGREGATION_KERNEL);
        }
    }
}
