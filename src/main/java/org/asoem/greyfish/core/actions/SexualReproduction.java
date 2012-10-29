package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import org.apache.commons.math3.util.MathUtils;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.ComponentList;
import org.asoem.greyfish.core.genes.*;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.collect.ElementSelectionStrategies;
import org.asoem.greyfish.utils.collect.ElementSelectionStrategy;
import org.asoem.greyfish.utils.collect.Product2;
import org.asoem.greyfish.utils.collect.Tuple2;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.space.MotionObject2DImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.actions.utils.ActionState.ABORTED;
import static org.asoem.greyfish.core.actions.utils.ActionState.COMPLETED;
import static org.asoem.greyfish.utils.base.Callbacks.call;

@Tagged("actions")
public class SexualReproduction extends AbstractAgentAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SexualReproduction.class);

    private Callback<? super SexualReproduction, ? extends List<? extends Chromosome>> spermSupplier;
    private Callback<? super SexualReproduction, Integer> clutchSize;
    private ElementSelectionStrategy<Chromosome> spermSelectionStrategy;
    private Callback<? super SexualReproduction, Double> spermFitnessEvaluator;
    private int offspringCount;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public SexualReproduction() {
        this(new Builder());
    }

    @Override
    protected ActionState proceed(Simulation simulation) {
        final List<? extends Chromosome> chromosomes = call(spermSupplier, this);

        if (chromosomes == null)
            throw new AssertionError("chromosomes is null");

        if (chromosomes.isEmpty())
            return ABORTED;

        final int eggCount = call(clutchSize, this);
        LOGGER.info("{}: Producing {} offspring ", agent(), eggCount);

        for (Chromosome sperm : spermSelectionStrategy.pick(chromosomes, eggCount)) {

            final Set<Integer> parents = sperm.getParents();
            if ( parents.size() != 1 )
                throw new AssertionError("Sperm must have an uniparental history");

            final Chromosome blend = blend(agent().getTraits(), sperm, agent().getId(), Iterables.getOnlyElement(parents));

            simulation().createAgent(agent().getPopulation(), new Initializer<Agent>() {
                @Override
                public void initialize(Agent agent) {
                    agent.setProjection(MotionObject2DImpl.reorientated(agent().getProjection(), RandomUtils.nextDouble(0, MathUtils.TWO_PI)));
                    agent.updateGeneComponents(blend);
                }
            });

            agent().logEvent(this, "offspringProduced", "");
        }

        offspringCount += eggCount;

        return COMPLETED;
    }

    private static Chromosome blend(ComponentList<AgentTrait<?>> egg, Chromosome sperm, int femaleID, int maleID) {

        // zip chromosomes
        final Tuple2.Zipped<AgentTrait<?>, Gene<?>> zip
                = Tuple2.Zipped.of(egg, sperm.getGenes());

        // segregate and mutate
        final Iterable<Gene<Object>> genes = Iterables.transform(zip, new Function<Product2<AgentTrait<?>, Gene<?>>, Gene<Object>>() {
            @Override
            public Gene<Object> apply(Product2<AgentTrait<?>, Gene<?>> tuple) {
                final Object segregationProduct = AgentTraits.segregate(tuple._1(), tuple._1().getAllele(), tuple._2().getAllele());
                return new Gene<Object>(segregationProduct, tuple._1().getRecombinationProbability());
            }
        });

        return new ChromosomeImpl(genes, Sets.newHashSet(femaleID, maleID));
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Source for sperm chromosomes", TypedValueModels.forField("spermSupplier", this, new TypeToken<Callback<? super SexualReproduction, List<? extends Chromosome>>>() {
        }));
        e.add("Number of offspring", TypedValueModels.forField("clutchSize", this, new TypeToken<Callback<? super SexualReproduction, Integer>>() {
        }));
        e.add("Sperm selection strategy", new SetAdaptor<String>(String.class) {

            private final BiMap<String, ElementSelectionStrategy<Chromosome>> strategies =
                    ImmutableBiMap.of(
                            "Random", ElementSelectionStrategies.<Chromosome>randomSelection(),
                            "Roulette Wheel", ElementSelectionStrategies.<Chromosome>rouletteWheelSelection(new Function<Chromosome, Double>() {
                        @Override
                        public Double apply(@Nullable Chromosome genes) {
                            assert genes != null;
                            return call(spermFitnessEvaluator, SexualReproduction.this);
                        }
                    }));

            @Override
            public Iterable<String> values() {
                return strategies.keySet();
            }

            @Override
            protected void set(String arg0) {
                spermSelectionStrategy = strategies.get(arg0);
            }

            @Override
            public String get() {
                return strategies.inverse().get(spermSelectionStrategy);
            }
        });
    }

    @Override
    public SexualReproduction deepClone(DeepCloner cloner) {
        return new SexualReproduction(this, cloner);
    }

    private SexualReproduction(SexualReproduction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.spermSupplier = cloneable.spermSupplier;
        this.clutchSize = cloneable.clutchSize;
        this.spermSelectionStrategy = cloneable.spermSelectionStrategy;
        this.spermFitnessEvaluator = cloneable.spermFitnessEvaluator;
    }

    protected SexualReproduction(AbstractBuilder<? extends SexualReproduction, ? extends AbstractBuilder> builder) {
        super(builder);
        this.spermSupplier = builder.spermStorage;
        this.clutchSize = builder.clutchSize;
        this.spermSelectionStrategy = builder.spermSelectionStrategy;
        this.spermFitnessEvaluator = builder.spermFitnessEvaluator;
    }

    @Override
    public void initialize() {
        super.initialize();
        offspringCount = 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getOffspringCount() {
        return offspringCount;
    }

    public Callback<? super SexualReproduction, Integer> getClutchSize() {
        return clutchSize;
    }

    private Object writeReplace() {
        return new Builder()
                .clutchSize(clutchSize)
                .spermSupplier(spermSupplier)
                .spermSelectionStrategy(spermSelectionStrategy)
                .spermFitnessCallback(spermFitnessEvaluator)
                .executedIf(getCondition())
                .name(getName());
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static final class Builder extends AbstractBuilder<SexualReproduction, Builder> implements Serializable {
        private Builder() {}

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected SexualReproduction checkedBuild() {
            return new SexualReproduction(this);
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

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<C extends SexualReproduction, B extends AbstractBuilder<C, B>> extends AbstractAgentAction.AbstractBuilder<C, B> implements Serializable {
        private Callback<? super SexualReproduction, ? extends List<? extends Chromosome>> spermStorage;
        private Callback<? super SexualReproduction, Integer> clutchSize = Callbacks.constant(1);
        private ElementSelectionStrategy<Chromosome> spermSelectionStrategy = ElementSelectionStrategies.randomSelection();
        private Callback<? super SexualReproduction, Double> spermFitnessEvaluator = Callbacks.constant(1.0);

        public B spermSupplier(Callback<? super SexualReproduction, ? extends List<? extends Chromosome>> spermStorage) {
            this.spermStorage = checkNotNull(spermStorage);
            return self();
        }

        public B clutchSize(Callback<? super SexualReproduction, Integer> nOffspring) {
            this.clutchSize = nOffspring;
            return self();
        }

        public B spermSelectionStrategy(ElementSelectionStrategy<Chromosome> selectionStrategy) {
            this.spermSelectionStrategy = checkNotNull(selectionStrategy);
            return self();
        }

        public B spermFitnessCallback(Callback<? super SexualReproduction, Double> callback) {
            this.spermFitnessEvaluator = checkNotNull(callback);
            return self();
        }
    }
}
