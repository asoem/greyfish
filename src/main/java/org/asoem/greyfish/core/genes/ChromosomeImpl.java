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

    private final List<Gene<?>> genes;
    private final Set<Integer> parents;

    public ChromosomeImpl(Iterable<? extends Gene<?>> genes, Set<Integer> parents) {
        this.genes = ImmutableList.copyOf(genes);
        this.parents = checkNotNull(parents);
    }

    @Override
    public List<Gene<?>> getGenes() {
        return genes;
    }

    @Override
    public Set<Integer> getParents() {
        return parents;
    }

    @Override
    public int size() {
        return genes.size();
    }

    public ChromosomeImpl recombined(Chromosome other) {
        return new ChromosomeImpl(
                Genes.recombine(this.genes, other.getGenes()), Sets.union(parents, other.getParents()));
    }

    public static ChromosomeImpl forAgent(Agent<?, ?> agent) {
        checkNotNull(agent, "Agent is null");
        return new ChromosomeImpl(
                Lists.transform(agent.getTraits(), new Function<AgentTrait<?, ?>, Gene<?>>() {
                    @Override
                    public Gene<?> apply(AgentTrait<?, ?> input) {
                        return new Gene<Object>(input.getAllele(), 0);
                    }
                }), Sets.newHashSet(agent.getId()));
    }
}
