/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.CreateRecombinedChromosome;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.Tagged;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.asoem.greyfish.utils.math.statistics.Sampling;
import org.asoem.greyfish.utils.math.statistics.Samplings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.*;
import static org.asoem.greyfish.core.actions.utils.ActionState.ABORTED;
import static org.asoem.greyfish.core.actions.utils.ActionState.COMPLETED;
import static org.asoem.greyfish.utils.base.Callbacks.call;

@Tagged("actions")
public abstract class SexualReproduction<A extends Agent<?>> extends BaseAgentAction<A, AgentContext<A>> {

    private static final Logger logger = LoggerFactory.getLogger(SexualReproduction.class);

    private Callback<? super SexualReproduction<A>, ? extends List<? extends Chromosome>> spermSupplier;
    private Callback<? super SexualReproduction<A>, Integer> clutchSize;
    private Sampling<? super Chromosome> spermSelectionStrategy;
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
    protected ActionState proceed(final AgentContext<A> context) {
        final List<? extends Chromosome> chromosomes = call(spermSupplier, this);

        if (chromosomes == null) {
            throw new AssertionError("chromosomes is null");
        }

        if (chromosomes.isEmpty()) {
            return ABORTED;
        }

        final int eggCount = call(clutchSize, this);
        logger.info("{}: Producing {} offspring ", context.agent(), eggCount);

        for (final Chromosome sperm : spermSelectionStrategy.sample(chromosomes, eggCount)) {

            /*
            final Set<Integer> parents = sperm.getParents();
            if (parents.size() != 1) {
                throw new AssertionError("Sperm must have an uniparental history");
            }
            */

            final Chromosome chromosome = context.agent().ask(new CreateRecombinedChromosome(sperm), Chromosome.class);
                        /*
            final int agentId = context.getAgentId();
            final int parentId = Iterables.getOnlyElement(parents);
            final Chromosome chromosome = blend(traits, sperm, agentId, parentId, context);
              */

            addAgent(chromosome);

            //agent().get().logEvent(this, "offspringProduced", "");
        }

        offspringCount += eggCount;

        return COMPLETED;
    }

    protected abstract void addAgent(final Chromosome chromosome);

    /*
    private static <A extends Agent<A, ?>> Chromosome blend(
            final FunctionalList<AgentTrait<?, ?>> egg,
            final Chromosome sperm,
            final int femaleID,
            final int maleID, final AgentContext<A, ?> context) {

        // zip chromosomes
        final Iterable<Product2<AgentTrait<?, ?>, TraitVector<?>>> zipped = Products.zip(egg, sperm.getTraitVectors());

        // segregate
        final Iterable<TraitVector<?>> genes = Iterables.transform(zipped, new Function<Product2<AgentTrait<?, ?>, TraitVector<?>>, TraitVector<?>>() {
            @Override
            public TraitVector<?> apply(final Product2<AgentTrait<?, ?>, TraitVector<?>> tuple) {
                final AgentTrait<?, ?> trait = tuple._1();
                final TraitVector<?> traitVector = tuple._2();
                return combine(trait, traitVector, context);
            }
        });

        return new HeritableTraitsChromosome(genes, Sets.newHashSet(femaleID, maleID));
    }

    @SuppressWarnings("unchecked")
    private static <T, A extends Agent<A, ?>> TraitVector<?> combine(final AgentTrait<?, T> trait, final TypedSupplier<?> supplier, final AgentContext<A, ?> context) {
        return TraitVector.of(
                trait.getName(), trait.transform(context, trait.transform(context, (T) trait.value(context), (T) supplier.get())._1())
        );
    }
    */

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
    protected abstract static class AbstractBuilder<
            A extends Agent<?>, C extends SexualReproduction<A>,
            B extends AbstractBuilder<A, C, B>>
            extends BaseAgentAction.AbstractBuilder<A, C, B, AgentContext<A>>
            implements Serializable {
        private Callback<? super SexualReproduction<A>, ? extends List<? extends Chromosome>> spermStorage;
        private Callback<? super SexualReproduction<A>, Integer> clutchSize =
                Callbacks.constant(1);
        private Sampling<? super Chromosome> spermSelectionStrategy =
                Samplings.random(RandomGenerators.rng()).withReplacement();
        private Callback<? super SexualReproduction<A>, Double> spermFitnessEvaluator = Callbacks.constant(1.0);

        protected AbstractBuilder() {
            addVerification(new Verification() {
                @Override
                protected void verify() {
                    checkState(spermStorage != null);
                    checkState(clutchSize != null);
                    checkState(spermSelectionStrategy != null);
                    checkState(spermFitnessEvaluator != null);
                }
            });
        }

        public final B spermSupplier(
                final Callback<? super SexualReproduction<A>, ? extends List<? extends Chromosome>> spermStorage) {
            this.spermStorage = checkNotNull(spermStorage);
            return self();
        }

        public final B clutchSize(final int nOffspring) {
            checkArgument(nOffspring >= 0);
            return clutchSize(Callbacks.constant(nOffspring));
        }

        public final B clutchSize(final Callback<? super SexualReproduction<A>, Integer> nOffspring) {
            this.clutchSize = checkNotNull(nOffspring);
            return self();
        }

        public final B spermSelectionStrategy(final Sampling<? super Chromosome> selectionStrategy) {
            this.spermSelectionStrategy = checkNotNull(selectionStrategy);
            return self();
        }

        public final B spermFitnessCallback(final Callback<? super SexualReproduction<A>, Double> callback) {
            this.spermFitnessEvaluator = checkNotNull(callback);
            return self();
        }
    }
}
