package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.genes.*;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Callback;
import org.asoem.greyfish.core.individual.Callbacks;
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
import static org.asoem.greyfish.core.individual.Callbacks.call;

@ClassGroup(tags = "actions")
public class SexualReproductionAction extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SexualReproductionAction.class);

    @Element(name = "spermList")
    private Callback<? super SexualReproductionAction, List<? extends Chromosome>> spermList;

    @Element(name = "clutchSize")
    private Callback<? super SexualReproductionAction, Integer> clutchSize;

    private ElementSelectionStrategy<Chromosome> spermSelectionStrategy;

    private Callback<? super SexualReproductionAction, Double> spermFitnessEvaluator;

    private final BiMap<String, ElementSelectionStrategy<Chromosome>> strategies =
            ImmutableBiMap.of(
                    "Random", ElementSelectionStrategies.<Chromosome>randomSelection(),
                    "Roulette Wheel", ElementSelectionStrategies.<Chromosome>rouletteWheelSelection(new Function<Chromosome, Double>() {
                @Override
                public Double apply(@Nullable Chromosome genes) {
                    assert genes != null;
                    return call(spermFitnessEvaluator, SexualReproductionAction.this);
                }
            }));

    private int offspringCount = 0;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public SexualReproductionAction() {
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
            final Product2<Chromosome, Chromosome> recombinedChromosomes = recombine(agent().getGeneComponentList(), sperm, agent().getId(), Iterables.getOnlyElement(spermHistory.getParents()));
            offspring.updateGeneComponents(recombinedChromosomes._1());

            simulation.activateAgent(offspring, agent().getProjection());
            agent().logEvent(this, "offspringProduced", "");
        }

        offspringCount += eggCount;

        return COMPLETED;
    }

    private static Product2<Chromosome, Chromosome> recombine(GeneComponentList<GeneComponent<?>> egg, Chromosome sperm, int femaleID, int maleID) {
        final Iterable<GeneController<?>> geneControllers = Iterables.transform(egg, new Function<GeneComponent<?>, GeneController<?>>() {
            @Override
            public GeneController<?> apply(GeneComponent<?> geneComponent) {
                return geneComponent.getGeneController();
            }
        });

        // zip chromosomes and recombination operator
        final Tuple3.Zipped<GeneComponent<?>, Gene<?>, GeneController<?>> zip
                = Tuple3.Zipped.of(egg, sperm.getGenes(), geneControllers);

        // recombine and mutate
        final Iterable<Product2<Gene<Object>, Gene<Object>>> genes = Iterables.transform(zip, new Function<Product3<GeneComponent<?>, Gene<?>, GeneController<?>>, Product2<Gene<Object>, Gene<Object>>>() {
            @Override
            public Product2<Gene<Object>, Gene<Object>> apply(Product3<GeneComponent<?>, Gene<?>, GeneController<?>> tuple) {
                final Object mutatedAndRecombinedAllele
                        = tuple._3().mutate(tuple._3().recombine(tuple._1().getAllele(), tuple._2().getAllele())._1());

                return Tuple2.of(
                        new Gene<Object>(mutatedAndRecombinedAllele, tuple._1().getRecombinationProbability()),
                        new Gene<Object>(mutatedAndRecombinedAllele, tuple._2().getRecombinationProbability())
                );
            }
        });

        final Tuple2<Iterable<Gene<Object>>, Iterable<Gene<Object>>> unzipped = Tuple2.unzipped(genes);
        final ChromosomalHistory chromosomalHistory = new BiparentalChromosomalHistory(femaleID, maleID);

        return Tuple2.of(
                new Chromosome(chromosomalHistory, unzipped._1()),
                new Chromosome(chromosomalHistory, unzipped._2())
        );
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Source for sperm chromosomes", TypedValueModels.forField("spermList", this, new TypeToken<Callback<? super SexualReproductionAction, List<? extends Chromosome>>>() {
        }));
        e.add("Number of offspring", TypedValueModels.forField("clutchSize", this, new TypeToken<Callback<? super SexualReproductionAction, Integer>>() {
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
    public SexualReproductionAction deepClone(DeepCloner cloner) {
        return new SexualReproductionAction(this, cloner);
    }

    private SexualReproductionAction(SexualReproductionAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.spermList = cloneable.spermList;
        this.clutchSize = cloneable.clutchSize;
        this.spermSelectionStrategy = cloneable.spermSelectionStrategy;
        this.spermFitnessEvaluator = cloneable.spermFitnessEvaluator;
    }

    protected SexualReproductionAction(AbstractBuilder<?, ?> builder) {
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

    public Callback<? super SexualReproductionAction, Integer> getClutchSize() {
        return clutchSize;
    }

    public static final class Builder extends AbstractBuilder<SexualReproductionAction, Builder> {
        private Builder() {
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected SexualReproductionAction checkedBuild() {
            return new SexualReproductionAction(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends SexualReproductionAction, T extends AbstractBuilder<E, T>> extends AbstractActionBuilder<E, T> {
        private Callback<? super SexualReproductionAction, List<? extends Chromosome>> spermStorage;
        private Callback<? super SexualReproductionAction, Integer> clutchSize = Callbacks.constant(1);
        private ElementSelectionStrategy<Chromosome> spermSelectionStrategy = ElementSelectionStrategies.randomSelection();
        private Callback<? super SexualReproductionAction, Double> spermFitnessEvaluator = Callbacks.constant(1.0);

        public T spermSupplier(Callback<? super SexualReproductionAction, List<? extends Chromosome>> spermStorage) {
            this.spermStorage = checkNotNull(spermStorage);
            return self();
        }

        public T clutchSize(Callback<? super SexualReproductionAction, Integer> nOffspring) {
            this.clutchSize = nOffspring;
            return self();
        }

        public T spermSelectionStrategy(ElementSelectionStrategy<Chromosome> selectionStrategy) {
            this.spermSelectionStrategy = checkNotNull(selectionStrategy);
            return self();
        }
    }
}
