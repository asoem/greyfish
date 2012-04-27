package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.GeneSnapshot;
import org.asoem.greyfish.core.genes.GeneSnapshotVector;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.actions.utils.ActionState.ABORTED;
import static org.asoem.greyfish.core.actions.utils.ActionState.SUCCESS;

@ClassGroup(tags="actions")
public class SexualReproductionAction extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SexualReproductionAction.class);

    @Element(name = "spermStorage")
    private EvaluatedGenomeStorage spermStorage;

    @Element(name = "clutchSize")
    private GreyfishExpression clutchSize;

    private ElementSelectionStrategy<GeneSnapshotVector> spermSelectionStrategy;

    private GreyfishExpression spermFitnessEvaluator = GreyfishExpressionFactoryHolder.compile("0"); // TODO: make field configurable

    private final BiMap<String, ElementSelectionStrategy<GeneSnapshotVector>> strategies =
            ImmutableBiMap.of(
                    "Random", ElementSelectionStrategies.<GeneSnapshotVector>randomSelection(),
                    "Roulette Wheel", ElementSelectionStrategies.<GeneSnapshotVector>rouletteWheelSelection(new Function<GeneSnapshotVector, Double>() {
                @Override
                public Double apply(@Nullable GeneSnapshotVector genes) {
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
        if (spermStorage.isEmpty())
            return ABORTED;

        LOGGER.debug("Producing {} offspring ", clutchSize);

        GeneSnapshotVector egg = new GeneSnapshotVector(agent().getId(), Iterables.transform(agent().getChromosome(), new Function<Gene<?>, GeneSnapshot<?>>() {
            @Override
            public GeneSnapshot<?> apply(@Nullable Gene<?> gene) {
                assert gene != null;
                return new GeneSnapshot<Object>(gene.get(), gene.getRecombinationProbability());
            }
        }));

        final int eggCount = clutchSize.evaluateForContext(this).asInt();

        for (GeneSnapshotVector sperm : spermSelectionStrategy.pick(spermStorage.get(), eggCount)) {

            Agent child = simulation.createAgent(agent().getPopulation());
            child.updateChromosome(egg.recombined(sperm));
            simulation.activateAgent(child, agent().getProjection());

            agent().logEvent(this, "offspringProduced", "");
        }

        offspringCount += eggCount;

        return SUCCESS;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Number of offspring", TypedValueModels.forField("clutchSize", this, GreyfishExpression.class));

        e.add("SpermPool", new SetAdaptor<EvaluatedGenomeStorage>(EvaluatedGenomeStorage.class) {
            @Override
            protected void set(EvaluatedGenomeStorage arg0) {
                spermStorage = checkNotNull(arg0);
            }

            @Override
            public EvaluatedGenomeStorage get() {
                return spermStorage;
            }

            @Override
            public Iterable<EvaluatedGenomeStorage> values() {
                return Iterables.filter(agent().getProperties(), EvaluatedGenomeStorage.class);
            }
        });
        
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
        this.spermStorage = map.cloneField(cloneable.spermStorage, EvaluatedGenomeStorage.class);
        this.clutchSize = cloneable.clutchSize;
        this.spermSelectionStrategy = cloneable.spermSelectionStrategy;
    }

    protected SexualReproductionAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.spermStorage = builder.spermStorage;
        this.clutchSize = builder.clutchSize;
        this.spermSelectionStrategy = builder.spermSelectionStrategy;
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

    public EvaluatedGenomeStorage getSpermStorage() {
        return spermStorage;
    }

    public static final class Builder extends AbstractBuilder<SexualReproductionAction, Builder> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override protected SexualReproductionAction checkedBuild() { return new SexualReproductionAction(this); }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends SexualReproductionAction, T extends AbstractBuilder<E,T>> extends AbstractGFAction.AbstractBuilder<E,T> {
        private EvaluatedGenomeStorage spermStorage;
        private GreyfishExpression clutchSize = GreyfishExpressionFactoryHolder.compile("1");
        public ElementSelectionStrategy<GeneSnapshotVector> spermSelectionStrategy = ElementSelectionStrategies.randomSelection();

        public T spermStorage(EvaluatedGenomeStorage spermStorage) { this.spermStorage = checkNotNull(spermStorage); return self(); }
        public T clutchSize(GreyfishExpression nOffspring) { this.clutchSize = nOffspring; return self(); }
        public T spermSelectionStrategy(ElementSelectionStrategy<GeneSnapshotVector> selectionStrategy) { this.spermSelectionStrategy = checkNotNull(selectionStrategy); return self(); }
    }
}
