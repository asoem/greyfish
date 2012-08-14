package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.genes.*;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.collect.ElementSelectionStrategies;
import org.asoem.greyfish.utils.collect.ElementSelectionStrategy;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.actions.utils.ActionState.ABORTED;
import static org.asoem.greyfish.core.actions.utils.ActionState.COMPLETED;
import static org.asoem.greyfish.utils.base.Callbacks.call;

@ClassGroup(tags = "actions")
public class SexualReproduction extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SexualReproduction.class);

    @Element(name = "spermList")
    private Callback<? super SexualReproduction, List<? extends Chromosome>> spermList;

    @Element(name = "clutchSize")
    private Callback<? super SexualReproduction, Integer> clutchSize;

    private ElementSelectionStrategy<Chromosome> spermSelectionStrategy;

    private Callback<? super SexualReproduction, Double> spermFitnessEvaluator;

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

    private int offspringCount = 0;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public SexualReproduction() {
        this(new Builder());
    }

    @Override
    protected ActionState proceed(Simulation simulation) {
        final List<? extends Chromosome> chromosomes = call(spermList, this);

        if (chromosomes == null)
            throw new AssertionError("chromosomes is null");

        if (chromosomes.isEmpty())
            return ABORTED;

        final int eggCount = call(clutchSize, this);
        LOGGER.info("{}: Producing {} offspring ", agent(), eggCount);

        for (Chromosome sperm : spermSelectionStrategy.pick(chromosomes, eggCount)) {
            final Agent offspring = simulation.createAgent(agent().getPopulation());

            final ChromosomalHistory spermHistory = sperm.getHistory();
            if ( ! (spermHistory instanceof UniparentalChromosomalHistory))
                throw new AssertionError("Sperm must have an uniparental history");
            final Chromosome blend = blend(agent().getGeneComponentList(), sperm, agent().getId(), Iterables.getOnlyElement(spermHistory.getParents()));
            offspring.updateGeneComponents(blend);

            simulation.activateAgent(offspring, agent().getProjection());
            agent().logEvent(this, "offspringProduced", "");
        }

        offspringCount += eggCount;

        return COMPLETED;
    }

    private static Chromosome blend(GeneComponentList<GeneComponent<?>> egg, Chromosome sperm, int femaleID, int maleID) {

        // zip chromosomes
        final Tuple2.Zipped<GeneComponent<?>, Gene<?>> zip
                = Tuple2.Zipped.of(egg, sperm.getGenes());

        // segregate and mutate
        final Iterable<Gene<Object>> genes = Iterables.transform(zip, new Function<Product2<GeneComponent<?>, Gene<?>>, Gene<Object>>() {
            @Override
            public Gene<Object> apply(Product2<GeneComponent<?>, Gene<?>> tuple) {
                final Object segregationProduct = GenesComponents.segregate(tuple._1(), tuple._1().getAllele(), tuple._2().getAllele());
                return new Gene<Object>(segregationProduct, tuple._1().getRecombinationProbability());
            }
        });

        final ChromosomalHistory chromosomalHistory = new BiparentalChromosomalHistory(femaleID, maleID);

        return new Chromosome(chromosomalHistory, genes);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Source for sperm chromosomes", TypedValueModels.forField("spermList", this, new TypeToken<Callback<? super SexualReproduction, List<? extends Chromosome>>>() {
        }));
        e.add("Number of offspring", TypedValueModels.forField("clutchSize", this, new TypeToken<Callback<? super SexualReproduction, Integer>>() {
        }));
        e.add("Sperm selection strategy", new SetAdaptor<String>(String.class) {

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
        this.spermList = cloneable.spermList;
        this.clutchSize = cloneable.clutchSize;
        this.spermSelectionStrategy = cloneable.spermSelectionStrategy;
        this.spermFitnessEvaluator = cloneable.spermFitnessEvaluator;
    }

    protected SexualReproduction(AbstractBuilder<?, ?> builder) {
        super(builder);
        this.spermList = builder.spermStorage;
        this.clutchSize = builder.clutchSize;
        this.spermSelectionStrategy = builder.spermSelectionStrategy;
        this.spermFitnessEvaluator = builder.spermFitnessEvaluator;
    }

    @Override
    public void initialize() {
        super.initialize();
        offspringCount = 0;
    }

    public static Builder with() {
        return new Builder();
    }

    public int getOffspringCount() {
        return offspringCount;
    }

    public Callback<? super SexualReproduction, Integer> getClutchSize() {
        return clutchSize;
    }

    public static final class Builder extends AbstractBuilder<SexualReproduction, Builder> {
        private Builder() {
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected SexualReproduction checkedBuild() {
            return new SexualReproduction(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends SexualReproduction, T extends AbstractBuilder<E, T>> extends AbstractActionBuilder<E, T> {
        private Callback<? super SexualReproduction, List<? extends Chromosome>> spermStorage;
        private Callback<? super SexualReproduction, Integer> clutchSize = Callbacks.constant(1);
        private ElementSelectionStrategy<Chromosome> spermSelectionStrategy = ElementSelectionStrategies.randomSelection();
        private Callback<? super SexualReproduction, Double> spermFitnessEvaluator = Callbacks.constant(1.0);

        public T spermSupplier(Callback<? super SexualReproduction, List<? extends Chromosome>> spermStorage) {
            this.spermStorage = checkNotNull(spermStorage);
            return self();
        }

        public T clutchSize(Callback<? super SexualReproduction, Integer> nOffspring) {
            this.clutchSize = nOffspring;
            return self();
        }

        public T spermSelectionStrategy(ElementSelectionStrategy<Chromosome> selectionStrategy) {
            this.spermSelectionStrategy = checkNotNull(selectionStrategy);
            return self();
        }
    }
}
