package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.GeneComponent;
import org.asoem.greyfish.core.genes.UniparentalChromosomalOrigin;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
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
import static org.asoem.greyfish.core.actions.utils.ActionState.SUCCESS;

@ClassGroup(tags="actions")
public class SexualReproductionAction extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SexualReproductionAction.class);

    @Element(name = "spermList")
    private GreyfishExpression spermList;

    @Element(name = "clutchSize")
    private GreyfishExpression clutchSize;

    private ElementSelectionStrategy<Chromosome> spermSelectionStrategy;

    private GreyfishExpression spermFitnessEvaluator;

    private final BiMap<String, ElementSelectionStrategy<Chromosome>> strategies =
            ImmutableBiMap.of(
                    "Random", ElementSelectionStrategies.<Chromosome>randomSelection(),
                    "Roulette Wheel", ElementSelectionStrategies.<Chromosome>rouletteWheelSelection(new Function<Chromosome, Double>() {
                @Override
                public Double apply(@Nullable Chromosome genes) {
                    assert genes != null;
                    return spermFitnessEvaluator.evaluateForContext(SexualReproductionAction.this).asDouble();
                }
            }));

    private int offspringCount = 0;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public SexualReproductionAction() {
        this(new Builder());
    }

    @Override
    protected ActionState proceed(Simulation simulation) {
        final List<Chromosome> chromosomes =
                (List<Chromosome>) spermList.evaluateForContext(this).as(List.class);

        if (chromosomes == null)
            throw new AssertionError("chromosomes is null");

        if (chromosomes.isEmpty())
            return ABORTED;

        final int eggCount = clutchSize.evaluateForContext(this).asInt();
        LOGGER.info("{}: Producing {} offspring ", agent(), eggCount);

        final Chromosome egg = new Chromosome(
                new UniparentalChromosomalOrigin(agent().getId()),
                Iterables.transform(agent().getGeneComponentList(), new Function<GeneComponent<?>, Gene<?>>() {
                    @Override
                    public Gene<?> apply(@Nullable GeneComponent<?> gene) {
                        assert gene != null;
                        return new Gene<Object>(gene.getValue(), gene.getRecombinationProbability());
                    }
                }));

        for (Chromosome sperm : spermSelectionStrategy.pick(chromosomes, eggCount)) {

            final Agent offspring = simulation.createAgent(agent().getPopulation());
            offspring.updateGeneComponents(egg.recombined(sperm));
            simulation.activateAgent(offspring, agent().getProjection());

            agent().logEvent(this, "offspringProduced", "");
        }

        offspringCount += eggCount;

        return SUCCESS;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Number of offspring", TypedValueModels.forField("clutchSize", this, GreyfishExpression.class));

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
        this.spermList =cloneable.spermList;
        this.clutchSize = cloneable.clutchSize;
        this.spermSelectionStrategy = cloneable.spermSelectionStrategy;
        this.spermFitnessEvaluator = cloneable.spermFitnessEvaluator;
    }

    protected SexualReproductionAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.spermList = builder.spermStorage;
        this.clutchSize = builder.clutchSize;
        this.spermSelectionStrategy = builder.spermSelectionStrategy;
        this.spermFitnessEvaluator = GreyfishExpressionFactoryHolder.compile("0");
    }

    @Override
    public void initialize() {
        super.initialize();
        offspringCount = 0;
    }

    public static Builder with() { return new Builder(); }

    public int getOffspringCount() {
        return offspringCount;
    }

    public GreyfishExpression getClutchSize() {
        return clutchSize;
    }

    public static final class Builder extends AbstractBuilder<SexualReproductionAction, Builder> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override protected SexualReproductionAction checkedBuild() { return new SexualReproductionAction(this); }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends SexualReproductionAction, T extends AbstractBuilder<E,T>> extends AbstractActionBuilder<E,T> {
        private GreyfishExpression spermStorage;
        private GreyfishExpression clutchSize = GreyfishExpressionFactoryHolder.compile("1");
        private ElementSelectionStrategy<Chromosome> spermSelectionStrategy = ElementSelectionStrategies.randomSelection();

        public T spermStorage(GreyfishExpression spermStorage) { this.spermStorage = checkNotNull(spermStorage); return self(); }
        public T clutchSize(GreyfishExpression nOffspring) { this.clutchSize = nOffspring; return self(); }
        public T spermSelectionStrategy(ElementSelectionStrategy<Chromosome> selectionStrategy) { this.spermSelectionStrategy = checkNotNull(selectionStrategy); return self(); }
    }
}
