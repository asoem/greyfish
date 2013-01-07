package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.genes.*;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.collect.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.SetAdaptor;
import org.asoem.greyfish.utils.gui.TypedValueModels;
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
public class SexualReproduction<A extends Agent<A, ?>> extends AbstractAgentAction<A> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SexualReproduction.class);

    private Callback<? super SexualReproduction<A>, ? extends List<? extends Chromosome>> spermSupplier;
    private Callback<? super SexualReproduction<A>, Integer> clutchSize;
    private ElementSelectionStrategy<Chromosome> spermSelectionStrategy;
    private Callback<? super SexualReproduction<A>, Double> spermFitnessEvaluator;
    private Callback<? super SexualReproduction<A>, Void> offspringInitializer;
    private int offspringCount;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public SexualReproduction() {
        this(new Builder<A>());
    }

    private SexualReproduction(SexualReproduction<A> cloneable, CloneMap map) {
        super(cloneable, map);
        this.spermSupplier = cloneable.spermSupplier;
        this.clutchSize = cloneable.clutchSize;
        this.spermSelectionStrategy = cloneable.spermSelectionStrategy;
        this.spermFitnessEvaluator = cloneable.spermFitnessEvaluator;
        this.offspringInitializer = cloneable.offspringInitializer;
    }

    protected SexualReproduction(AbstractBuilder<A, ? extends SexualReproduction<A>, ? extends AbstractBuilder<A , ?, ?>> builder) {
        super(builder);
        this.spermSupplier = builder.spermStorage;
        this.clutchSize = builder.clutchSize;
        this.spermSelectionStrategy = builder.spermSelectionStrategy;
        this.spermFitnessEvaluator = builder.spermFitnessEvaluator;
        this.offspringInitializer = builder.offspringInitializer;
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

            agent().reproduce(new Initializer<Agent<A, ?>>() {
                @Override
                public void initialize(Agent<A, ?> agent) {
                    //agent.setProjection(offspringInitializer.apply(SexualReproduction.this, ArgumentMap.of()));
                    /*MotionObject2DImpl.reorientated(agent().getProjection())*/
                    offspringInitializer.apply(SexualReproduction.this, ArgumentMap.of("offspring", agent));
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
        e.add("Source for sperm chromosomes", TypedValueModels.forField("spermSupplier", this, new TypeToken<Callback<? super SexualReproduction<A>, List<? extends Chromosome>>>() {
        }));
        e.add("Number of offspring", TypedValueModels.forField("clutchSize", this, new TypeToken<Callback<? super SexualReproduction<A>, Integer>>() {
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
    public SexualReproduction<A> deepClone(CloneMap cloneMap) {
        return new SexualReproduction<A>(this, cloneMap);
    }

    @Override
    public void initialize() {
        super.initialize();
        offspringCount = 0;
    }

    public static <A extends Agent<A, ?>> Builder<A> builder() {
        return new Builder<A>();
    }

    public int getOffspringCount() {
        return offspringCount;
    }

    public Callback<? super SexualReproduction<A>, Integer> getClutchSize() {
        return clutchSize;
    }

    private Object writeReplace() {
        return new Builder<A>()
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

    public static final class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, SexualReproduction<A>, Builder<A>> implements Serializable {
        private Builder() {}

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected SexualReproduction<A> checkedBuild() {
            return new SexualReproduction<A>(this);
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
    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends SexualReproduction<A>, B extends AbstractBuilder<A, C, B>> extends AbstractAgentAction.AbstractBuilder<A, C, B> implements Serializable {
        private Callback<? super SexualReproduction<A>, ? extends List<? extends Chromosome>> spermStorage;
        private Callback<? super SexualReproduction<A>, Integer> clutchSize = Callbacks.constant(1);
        private ElementSelectionStrategy<Chromosome> spermSelectionStrategy = ElementSelectionStrategies.randomSelection();
        private Callback<? super SexualReproduction<A>, Double> spermFitnessEvaluator = Callbacks.constant(1.0);
        private Callback<? super SexualReproduction<A>, Void> offspringInitializer = Callbacks.emptyCallback();

        public B spermSupplier(Callback<? super SexualReproduction<A>, ? extends List<? extends Chromosome>> spermStorage) {
            this.spermStorage = checkNotNull(spermStorage);
            return self();
        }

        public B clutchSize(Callback<? super SexualReproduction<A>, Integer> nOffspring) {
            this.clutchSize = nOffspring;
            return self();
        }

        public B spermSelectionStrategy(ElementSelectionStrategy<Chromosome> selectionStrategy) {
            this.spermSelectionStrategy = checkNotNull(selectionStrategy);
            return self();
        }

        public B spermFitnessCallback(Callback<? super SexualReproduction<A>, Double> callback) {
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
        }
    }
}
