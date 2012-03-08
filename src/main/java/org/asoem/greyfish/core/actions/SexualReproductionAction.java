package org.asoem.greyfish.core.actions;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.genes.EvaluatedChromosome;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.ImmutableChromosome;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.ElementSelectionStrategies;
import org.asoem.greyfish.utils.collect.ElementSelectionStrategy;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.space.Location2D;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

@ClassGroup(tags="actions")
public class SexualReproductionAction extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SexualReproductionAction.class);

    @Element(name = "spermStorage")
    private EvaluatedGenomeStorage spermStorage;

    @Element(name = "clutchSize")
    private GreyfishExpression clutchSize;

    private ElementSelectionStrategy<EvaluatedChromosome<?>> spermSelectionStrategy;

    private final static BiMap<String, ElementSelectionStrategy<EvaluatedChromosome<?>>> strategies =
            ImmutableBiMap.of(
                    "Random", ElementSelectionStrategies.<EvaluatedChromosome<?>>randomSelection(),
                    "Roulette Wheel", ElementSelectionStrategies.<EvaluatedChromosome<?>>rouletteWheelSelection(),
                    "Best", ElementSelectionStrategies.<EvaluatedChromosome<?>>bestSelection());

    private int offspringCount = 0;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public SexualReproductionAction() {
        this(new Builder());
    }

    @Override
    protected ActionState executeUnconditioned(Simulation simulation) {
        if (spermStorage.isEmpty())
            return ActionState.END_FAILED;

        LOGGER.debug("Producing {} offspring ", clutchSize);

        final Population population = agent().getPopulation();
        final Location2D locatable = agent().getProjection();

        final int eggCount = clutchSize.evaluateForContext(this).asInt();
        for (EvaluatedChromosome<?> spermCandidate : spermSelectionStrategy.pick(spermStorage.get(), eggCount)) {
            final ImmutableChromosome<Gene<?>> gamete = ImmutableChromosome.mutatedCopyOf(ImmutableChromosome.recombined(agent().getChromosome(), spermCandidate));

            simulation.createAgent(population, gamete, locatable);

            agent().logEvent(this, "offspringProduced", "");
        }

        offspringCount += eggCount;

        return ActionState.END_SUCCESS;
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
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
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
        public ElementSelectionStrategy<EvaluatedChromosome<?>> spermSelectionStrategy = ElementSelectionStrategies.randomSelection();

        public T spermStorage(EvaluatedGenomeStorage spermStorage) { this.spermStorage = checkNotNull(spermStorage); return self(); }
        public T clutchSize(GreyfishExpression nOffspring) { this.clutchSize = nOffspring; return self(); }
        public T spermSelectionStrategy(ElementSelectionStrategy<EvaluatedChromosome<?>> selectionStrategy) { this.spermSelectionStrategy = checkNotNull(selectionStrategy); return self(); }
    }
}
