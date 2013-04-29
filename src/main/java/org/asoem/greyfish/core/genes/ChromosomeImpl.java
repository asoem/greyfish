package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.asoem.greyfish.utils.collect.Product2;
import org.asoem.greyfish.utils.collect.Products;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 25.04.12
 * Time: 15:27
 */
public class ChromosomeImpl implements Chromosome {

    private static final Predicate<AgentTrait<? extends Agent<?,?>,?>> IS_HERITABLE = new Predicate<AgentTrait<? extends Agent<?, ?>, ?>>() {
        @Override
        public boolean apply(AgentTrait<? extends Agent<?, ?>, ?> input) {
            return input.isHeritable();
        }
    };
    private final List<TraitVector<?>> traitVectors;
    private final Set<Integer> parents;

    public ChromosomeImpl(Iterable<? extends TraitVector<?>> genes, Set<Integer> parents) {
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
    public <A extends Agent<A, ?>> void updateAgent(Agent<A, ?> agent) {
        checkNotNull(agent, "Agent is null");
        final FunctionalList<AgentTrait<A, ?>> traits = agent.getTraits();
        final Iterable<AgentTrait<A, ?>> filter = traits.filter(IS_HERITABLE);
        final List<TraitVector<?>> traitVectors1 = getTraitVectors();
        final Iterable<Product2<AgentTrait<A, ?>, TraitVector<?>>> zip = Products.zip(filter, traitVectors1);

        for (Product2<AgentTrait<A, ?>, TraitVector<?>> tuple2 : zip) {
            final AgentTrait<?, ?> trait = tuple2._1();
            final TraitVector<?> supplier = tuple2._2();
            assert trait.getName().equals(supplier.getName());
            trait.copyFrom(supplier);
        }
        agent.setParents(getParents());
    }

    public ChromosomeImpl recombined(Chromosome other) {
        checkNotNull(other);
        return new ChromosomeImpl(
                Genes.recombine(this.traitVectors, other.getTraitVectors()), Sets.union(parents, other.getParents()));
    }

    public static ChromosomeImpl forAgent(Agent<?, ?> agent) {
        checkNotNull(agent, "Agent is null");
        final Iterable<? extends AgentTrait<? extends Agent<?, ?>, ?>> traits = agent.getTraits().filter(IS_HERITABLE);
        return new ChromosomeImpl(
                Iterables.transform(traits, new Function<AgentTrait<? extends Agent<?, ?>, ?>, TraitVector<?>>() {
                    @Override
                    public TraitVector<?> apply(AgentTrait<? extends Agent<?, ?>, ?> input) {
                        return TraitVector.copyOf(input);
                    }
                }), Sets.newHashSet(agent.getId()));
    }
}
