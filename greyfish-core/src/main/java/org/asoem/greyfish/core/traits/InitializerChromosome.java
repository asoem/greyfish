package org.asoem.greyfish.core.traits;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.asoem.greyfish.utils.collect.Product2;
import org.asoem.greyfish.utils.collect.Products;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: christoph
 * Date: 21.06.13
 * Time: 11:56
 */
public enum InitializerChromosome implements Chromosome {
    INSTANCE;

    private static final Predicate<AgentTrait<? extends Agent<?,?>,?>> IS_HERITABLE = new Predicate<AgentTrait<? extends Agent<?, ?>, ?>>() {
        @Override
        public boolean apply(final AgentTrait<? extends Agent<?, ?>, ?> input) {
            return input.isHeritable();
        }
    };

    @Override
    public List<TraitVector<?>> getTraitVectors() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Integer> getParents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A extends Agent<A, ?>> void updateAgent(final Agent<A, ?> agent) {
        checkNotNull(agent, "Agent is null");
        final FunctionalList<AgentTrait<A, ?>> traits = agent.getTraits();
        checkState(traits != null, "Agent#getTraits() returned null");

        final Iterable<AgentTrait<A, ?>> filter = traits.filter(IS_HERITABLE);
        final Iterable<Product2<AgentTrait<A, ?>, TraitVector<?>>> zip = Products.zip(filter, Iterables.transform(filter, new Function<AgentTrait<A, ?>, TraitVector<?>>() {
            @Nullable
            @Override
            public TraitVector<?> apply(final AgentTrait<A, ?> input) {
                return createInitialValueTraitVector(input);
            }
        }));

        for (final Product2<AgentTrait<A, ?>, TraitVector<?>> tuple2 : zip) {
            final AgentTrait<?, ?> trait = tuple2._1();
            final TraitVector<?> supplier = tuple2._2();
            assert trait.getName().equals(supplier.getName());
            trait.copyFrom(supplier);
        }
    }

    private static <T> TraitVector<T> createInitialValueTraitVector(final AgentTrait<?, T> input) {
        return TraitVector.create(input.createInitialValue(), input.getRecombinationProbability(), input.getValueType(), input.getName());
    }
}
