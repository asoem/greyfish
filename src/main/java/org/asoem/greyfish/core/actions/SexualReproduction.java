package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.genes.*;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.collect.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.space.Object2D;
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
import static com.google.common.base.Preconditions.checkState;
import static org.asoem.greyfish.core.actions.utils.ActionState.ABORTED;
import static org.asoem.greyfish.core.actions.utils.ActionState.COMPLETED;
import static org.asoem.greyfish.utils.base.Callbacks.call;

@Tagged("actions")
public class SexualReproduction<A extends SpatialAgent<A, ?, P>, P extends Object2D> extends AbstractAgentAction<A> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SexualReproduction.class);

    private Callback<? super SexualReproduction<A, P>, ? extends List<? extends Chromosome>> spermSupplier;
    private Callback<? super SexualReproduction<A, P>, Integer> clutchSize;
    private ElementSelectionStrategy<Chromosome> spermSelectionStrategy;
    private Callback<? super SexualReproduction<A, P>, Double> spermFitnessEvaluator;
    private Callback<? super SexualReproduction<A, P>, P> projectionFactory;
    private int offspringCount;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public SexualReproduction() {
        this(new Builder<A, P>());
    }

    private SexualReproduction(SexualReproduction<A, P> cloneable, DeepCloner map) {
        super(cloneable, map);
        this.spermSupplier = cloneable.spermSupplier;
        this.clutchSize = cloneable.clutchSize;
        this.spermSelectionStrategy = cloneable.spermSelectionStrategy;
        this.spermFitnessEvaluator = cloneable.spermFitnessEvaluator;
        this.projectionFactory = cloneable.projectionFactory;
    }

    protected SexualReproduction(AbstractBuilder<A, P, ? extends SexualReproduction<A, P>, ? extends AbstractBuilder<A ,P, ?, ?>> builder) {
        super(builder);
        this.spermSupplier = builder.spermStorage;
        this.clutchSize = builder.clutchSize;
        this.spermSelectionStrategy = builder.spermSelectionStrategy;
        this.spermFitnessEvaluator = builder.spermFitnessEvaluator;
        this.projectionFactory = builder.projectionFactory;
    }

    @Override
    protected ActionState proceed() {
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

            agent().reproduce(new Initializer<SpatialAgent<A, ?, P>>() {
                @Override
                public void initialize(SpatialAgent<A, ?, P> agent) {
                    agent.setProjection(projectionFactory.apply(SexualReproduction.this, ArgumentMap.of()));
                    /*MotionObject2DImpl.reorientated(agent().getProjection())*/
                    agent.updateGeneComponents(blend);
                }
            });

            agent().logEvent(this, "offspringProduced", "");
        }

        offspringCount += eggCount;

        return COMPLETED;
    }

    private static <A extends Agent<A, ?>> Chromosome blend(SearchableList<AgentTrait<A, ?>> egg, Chromosome sperm, int femaleID, int maleID) {

        // zip chromosomes
        final Tuple2.Zipped<AgentTrait<A, ?>, Gene<?>> zip
                = Tuple2.Zipped.of(egg, sperm.getGenes());

        // segregate and mutate
        final Iterable<Gene<Object>> genes = Iterables.transform(zip, new Function<Product2<AgentTrait<A, ?>, Gene<?>>, Gene<Object>>() {
            @Override
            public Gene<Object> apply(Product2<AgentTrait<A, ?>, Gene<?>> tuple) {
                final Object segregationProduct = AgentTraits.segregate(tuple._1(), tuple._1().getAllele(), tuple._2().getAllele());
                return new Gene<Object>(segregationProduct, tuple._1().getRecombinationProbability());
            }
        });

        return new ChromosomeImpl(genes, Sets.newHashSet(femaleID, maleID));
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Source for sperm chromosomes", TypedValueModels.forField("spermSupplier", this, new TypeToken<Callback<? super SexualReproduction<A, P>, List<? extends Chromosome>>>() {
        }));
        e.add("Number of offspring", TypedValueModels.forField("clutchSize", this, new TypeToken<Callback<? super SexualReproduction<A, P>, Integer>>() {
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
    public SexualReproduction<A, P> deepClone(DeepCloner cloner) {
        return new SexualReproduction<A, P>(this, cloner);
    }

    @Override
    public void initialize() {
        super.initialize();
        offspringCount = 0;
    }

    public static <A extends SpatialAgent<A, ?, P>, P extends Object2D> Builder<A, P> builder() {
        return new Builder<A, P>();
    }

    public int getOffspringCount() {
        return offspringCount;
    }

    public Callback<? super SexualReproduction<A, P>, Integer> getClutchSize() {
        return clutchSize;
    }

    private Object writeReplace() {
        return new Builder<A, P>()
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

    public static final class Builder<A extends SpatialAgent<A, ?, P>, P extends Object2D> extends AbstractBuilder<A, P, SexualReproduction<A,P>, Builder<A,P>> implements Serializable {
        private Builder() {}

        @Override
        protected Builder<A, P> self() {
            return this;
        }

        @Override
        protected SexualReproduction<A,P> checkedBuild() {
            return new SexualReproduction<A,P>(this);
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
    protected static abstract class AbstractBuilder<A extends SpatialAgent<A, ?, P>, P extends Object2D, C extends SexualReproduction<A, P>, B extends AbstractBuilder<A, P, C, B>> extends AbstractAgentAction.AbstractBuilder<A, C, B> implements Serializable {
        private Callback<? super SexualReproduction<A, P>, ? extends List<? extends Chromosome>> spermStorage;
        private Callback<? super SexualReproduction<A, P>, Integer> clutchSize = Callbacks.constant(1);
        private ElementSelectionStrategy<Chromosome> spermSelectionStrategy = ElementSelectionStrategies.randomSelection();
        private Callback<? super SexualReproduction<A, P>, Double> spermFitnessEvaluator = Callbacks.constant(1.0);
        private Callback<? super SexualReproduction<A, P>, P> projectionFactory;

        public B spermSupplier(Callback<? super SexualReproduction<A, P>, ? extends List<? extends Chromosome>> spermStorage) {
            this.spermStorage = checkNotNull(spermStorage);
            return self();
        }

        public B clutchSize(Callback<? super SexualReproduction<A, P>, Integer> nOffspring) {
            this.clutchSize = nOffspring;
            return self();
        }

        public B spermSelectionStrategy(ElementSelectionStrategy<Chromosome> selectionStrategy) {
            this.spermSelectionStrategy = checkNotNull(selectionStrategy);
            return self();
        }

        public B spermFitnessCallback(Callback<? super SexualReproduction<A,P>, Double> callback) {
            this.spermFitnessEvaluator = checkNotNull(callback);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            checkState(spermStorage != null);
            checkState(clutchSize != null);
            checkState(spermSelectionStrategy != null);
            checkState(spermFitnessEvaluator != null);
            checkState(projectionFactory != null);
        }
    }
}
