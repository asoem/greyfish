package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.core.traits.HeritableTraitsChromosome;
import org.asoem.greyfish.core.traits.TraitVector;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.Tagged;
import org.asoem.greyfish.utils.base.TypedSupplier;
import org.asoem.greyfish.utils.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.*;
import static org.asoem.greyfish.core.actions.utils.ActionState.ABORTED;
import static org.asoem.greyfish.core.actions.utils.ActionState.COMPLETED;
import static org.asoem.greyfish.utils.base.Callbacks.call;

@Tagged("actions")
public abstract class SexualReproduction<A extends Agent<A, ? extends BasicSimulationContext<?, A>>> extends BaseAgentAction<A> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SexualReproduction.class);

    private Callback<? super SexualReproduction<A>, ? extends List<? extends Chromosome>> spermSupplier;
    private Callback<? super SexualReproduction<A>, Integer> clutchSize;
    private ElementSelectionStrategy<Chromosome> spermSelectionStrategy;
    private Callback<? super SexualReproduction<A>, Double> spermFitnessEvaluator;
    private int offspringCount;

    protected SexualReproduction(final AbstractBuilder<A, ? extends SexualReproduction<A>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
        this.spermSupplier = builder.spermStorage;
        this.clutchSize = builder.clutchSize;
        this.spermSelectionStrategy = builder.spermSelectionStrategy;
        this.spermFitnessEvaluator = builder.spermFitnessEvaluator;
    }

    @Override
    protected ActionState proceed(final ExecutionContext<A> context) {
        final List<? extends Chromosome> chromosomes = call(spermSupplier, this);

        if (chromosomes == null) {
            throw new AssertionError("chromosomes is null");
        }

        if (chromosomes.isEmpty()) {
            return ABORTED;
        }

        final int eggCount = call(clutchSize, this);
        LOGGER.info("{}: Producing {} offspring ", context.agent(), eggCount);

        for (final Chromosome sperm : spermSelectionStrategy.pick(chromosomes, eggCount)) {

            final Set<Integer> parents = sperm.getParents();
            if (parents.size() != 1) {
                throw new AssertionError("Sperm must have an uniparental history");
            }

            final BasicSimulationContext<? extends DiscreteTimeSimulation<A>, A> simulationContext = context.agent().getContext().get();
            final Chromosome chromosome = blend(context.agent().getTraits(), sperm, simulationContext.getAgentId(), Iterables.getOnlyElement(parents));

            addAgent(chromosome);

            //agent().get().logEvent(this, "offspringProduced", "");
        }

        offspringCount += eggCount;

        return COMPLETED;
    }

    protected abstract void addAgent(final Chromosome chromosome);

    private static <A extends Agent<A, ? extends BasicSimulationContext<?, A>>> Chromosome blend(
            final FunctionalList<AgentTrait<A, ?>> egg,
            final Chromosome sperm,
            final int femaleID,
            final int maleID) {

        // zip chromosomes
        final Iterable<Product2<AgentTrait<A, ?>, TraitVector<?>>> zipped = Products.zip(egg, sperm.getTraitVectors());

        // segregate
        final Iterable<TraitVector<?>> genes = Iterables.transform(zipped, new Function<Product2<AgentTrait<A, ?>, TraitVector<?>>, TraitVector<?>>() {
            @Override
            public TraitVector<?> apply(final Product2<AgentTrait<A, ?>, TraitVector<?>> tuple) {
                final AgentTrait<A, ?> trait = tuple._1();
                final TraitVector<?> traitVector = tuple._2();
                return combine(trait, traitVector);
            }
        });

        return new HeritableTraitsChromosome(genes, Sets.newHashSet(femaleID, maleID));
    }

    @SuppressWarnings("unchecked")
    private static <T> TraitVector<?> combine(final AgentTrait<?, T> trait, final TypedSupplier<?> supplier) {
        checkArgument(trait.getValueType().equals(supplier.getValueType()));
        return TraitVector.create(
                trait.transform(trait.transform(trait.get(), (T) supplier.get())),
                trait.getValueType(),
                trait.getName());
    }

    /*
     * @Override public SexualReproduction<A> deepClone(final DeepCloner cloner) { return new
     * SexualReproduction<A>(this, cloner); }
     */

    @Override
    public void initialize() {
        super.initialize();
        offspringCount = 0;
    }

    /*
     * public static <A extends Agent<A, ? extends BasicSimulationContext<?, A>>> Builder<A> builder() { return new
     * Builder<A>(); }
     */

    public int getOffspringCount() {
        return offspringCount;
    }

    public Callback<? super SexualReproduction<A>, Integer> getClutchSize() {
        return clutchSize;
    }

    /*
     * private Object writeReplace() { return new Builder<A>() .clutchSize(clutchSize) .spermSupplier(spermSupplier)
     * .spermSelectionStrategy(spermSelectionStrategy) .spermFitnessCallback(spermFitnessEvaluator)
     * .executedIf(getCondition()) .name(getName()); }
     * <p/>
     * private void readObject(final ObjectInputStream stream) throws InvalidObjectException { throw new
     * InvalidObjectException("Builder required"); }
     * <p/>
     * public static final class Builder<A extends Agent<A, ? extends BasicSimulationContext<?, A>>> extends
     * AbstractBuilder<A, SexualReproduction<A>, Builder<A>> implements Serializable { private Builder() { }
     *
     * @Override protected Builder<A> self() { return this; }
     * @Override protected SexualReproduction<A> checkedBuild() { return new SexualReproduction<A>(this); }
     * <p/>
     * private Object readResolve() throws ObjectStreamException { try { return build(); } catch (IllegalStateException
     * e) { throw new InvalidObjectException("Build failed with: " + e.getMessage()); } }
     * <p/>
     * private static final long serialVersionUID = 0; }
     */
    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends Agent<A, ? extends BasicSimulationContext<?, A>>, C extends SexualReproduction<A>, B extends AbstractBuilder<A, C, B>> extends BaseAgentAction.AbstractBuilder<A, C, B> implements Serializable {
        private Callback<? super SexualReproduction<A>, ? extends List<? extends Chromosome>> spermStorage;
        private Callback<? super SexualReproduction<A>, Integer> clutchSize = Callbacks.constant(1);
        private ElementSelectionStrategy<Chromosome> spermSelectionStrategy = ElementSelectionStrategies.randomSelection();
        private Callback<? super SexualReproduction<A>, Double> spermFitnessEvaluator = Callbacks.constant(1.0);

        public B spermSupplier(final Callback<? super SexualReproduction<A>, ? extends List<? extends Chromosome>> spermStorage) {
            this.spermStorage = checkNotNull(spermStorage);
            return self();
        }

        public B clutchSize(final int nOffspring) {
            checkArgument(nOffspring >= 0);
            return clutchSize(Callbacks.constant(nOffspring));
        }

        public B clutchSize(final Callback<? super SexualReproduction<A>, Integer> nOffspring) {
            this.clutchSize = checkNotNull(nOffspring);
            return self();
        }

        public B spermSelectionStrategy(final ElementSelectionStrategy<Chromosome> selectionStrategy) {
            this.spermSelectionStrategy = checkNotNull(selectionStrategy);
            return self();
        }

        public B spermFitnessCallback(final Callback<? super SexualReproduction<A>, Double> callback) {
            this.spermFitnessEvaluator = checkNotNull(callback);
            return self();
        }

        @Override
        protected void checkBuilder() {
            super.checkBuilder();
            checkState(spermStorage != null);
            checkState(clutchSize != null);
            checkState(spermSelectionStrategy != null);
            checkState(spermFitnessEvaluator != null);
        }
    }
}
