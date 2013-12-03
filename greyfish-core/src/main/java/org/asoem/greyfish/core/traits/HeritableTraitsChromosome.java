package org.asoem.greyfish.core.traits;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.asoem.greyfish.utils.collect.Product2;
import org.asoem.greyfish.utils.collect.Products;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@code Chromosome} implementation which stores and injects only heritable traits.
 */
public final class HeritableTraitsChromosome implements Chromosome {

    private static final Predicate<AgentTrait<? extends Agent<?, ?>, ?>> IS_HERITABLE =
            new Predicate<AgentTrait<? extends Agent<?, ?>, ?>>() {
                @Override
                public boolean apply(final AgentTrait<? extends Agent<?, ?>, ?> input) {
                    return input.isHeritable();
                }
            };
    private final List<TraitVector<?>> traitVectors;
    private final Set<Integer> parents;

    public HeritableTraitsChromosome(final Iterable<? extends TraitVector<?>> genes, final Set<Integer> parents) {
        this.traitVectors = ImmutableList.copyOf(genes);
        this.parents = checkNotNull(parents);
    }

    @Override
    public List<TraitVector<?>> getTraitVectors() {
        return traitVectors;
    }

    @Override
    public Set<Integer> getParents() {
        return parents;
    }

    @Override
    public int size() {
        return traitVectors.size();
    }

    @Override
    public <A extends Agent<A, ?>> void updateAgent(final A agent) {
        checkNotNull(agent, "Agent is null");

        final FunctionalList<AgentTrait<A, ?>> traits = agent.getTraits();
        final Iterable<AgentTrait<A, ?>> filter = traits.filter(IS_HERITABLE);
        final List<TraitVector<?>> traitVectors1 = getTraitVectors();
        final Iterable<Product2<AgentTrait<A, ?>, TraitVector<?>>> zip = Products.zip(filter, traitVectors1);

        for (final Product2<AgentTrait<A, ?>, TraitVector<?>> tuple2 : zip) {
            final AgentTrait<A, ?> trait = tuple2._1();
            final TraitVector<?> supplier = tuple2._2();
            assert trait.getName().equals(supplier.getName());
            trait.copyFrom(supplier);
        }

        agent.setParents(getParents());
    }

    /**
     * Create a new {@code HeritableTraitsChromosome} from given {@code agent} by creating {@code TraitVector}s of all
     * it's heritable {@link Trait}s.
     *
     * @param agent the {@code Agent} to get the traits from.
     * @return a new {@code HeritableTraitsChromosome} object
     */
    public static HeritableTraitsChromosome copyFromAgent(final Agent<?, ?> agent) {
        checkNotNull(agent, "Agent is null");
        final Iterable<? extends AgentTrait<? extends Agent<?, ?>, ?>> traits = agent.getTraits().filter(IS_HERITABLE);
        final Iterable<TraitVector<?>> traitVectors = Iterables.transform(
                traits,
                new Function<AgentTrait<? extends Agent<?, ?>, ?>, TraitVector<?>>() {
                    @Override
                    public TraitVector<?> apply(final AgentTrait<? extends Agent<?, ?>, ?> input) {
                        return TraitVector.copyOf(input);
                    }
                });
        return new HeritableTraitsChromosome(traitVectors, Sets.newHashSet(agent.getContext().get().getAgentId()));
    }

    /**
     * Create a new {@code HeritableTraitsChromosome} from given {@code agent} by creating {@code TraitVector}s of all
     * it's heritable {@link Trait}s.
     *
     * @param agent the {@code Agent} to get the traits from.
     * @return a new {@code HeritableTraitsChromosome} object
     */
    public static HeritableTraitsChromosome initializeFromAgent(final Agent<?, ?> agent) {
        checkNotNull(agent, "Agent is null");

        final FunctionalList<? extends AgentTrait<? extends Agent<?, ?>, ?>> traits = agent.getTraits();
        checkNotNull(traits, "Agent#getTraits() returned null");

        final Iterable<? extends AgentTrait<? extends Agent<?, ?>, ?>> filter = traits.filter(IS_HERITABLE);
        final Iterable<TraitVector<?>> traitVectors = Iterables.transform(
                filter,
                new Function<AgentTrait<?, ?>, TraitVector<?>>() {
                    @Nullable
                    @Override
                    public TraitVector<?> apply(final AgentTrait<?, ?> input) {
                        return createInitialValueTraitVector(input);
                    }
                });

        return new HeritableTraitsChromosome(traitVectors, Sets.newHashSet(agent.getContext().get().getAgentId()));
    }

    private static <T> TraitVector<T> createInitialValueTraitVector(final AgentTrait<?, T> input) {
        return TraitVector.create(
                input.createInitialValue(),
                input.getRecombinationProbability(),
                input.getValueType(),
                input.getName());
    }
}
