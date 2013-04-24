package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.agent.Agent;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 25.04.12
 * Time: 15:27
 */
public class ChromosomeImpl implements Chromosome {

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

    public ChromosomeImpl recombined(Chromosome other) {
        return new ChromosomeImpl(
                Genes.recombine(this.traitVectors, other.getTraitVectors()), Sets.union(parents, other.getParents()));
    }

    public static ChromosomeImpl forAgent(Agent<?, ?> agent) {
        checkNotNull(agent, "Agent is null");
        return new ChromosomeImpl(
                Lists.transform(agent.getTraits(), new Function<AgentTrait<?, ?>, TraitVector<?>>() {
                    @Override
                    public TraitVector<?> apply(AgentTrait<?, ?> input) {
                        return new TraitVector<Object>(input.get(), 0);
                    }
                }), Sets.newHashSet(agent.getId()));
    }
}
