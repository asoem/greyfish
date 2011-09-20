package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.MutableComponentList;
import org.asoem.greyfish.utils.DeepCloner;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

/**
 * User: christoph
 * Date: 09.09.11
 * Time: 15:21
 */
public class MutableGenome extends MutableComponentList<Gene<?>> implements Genome {

    private List<ForwardingGene<?>> genes = Lists.newArrayList();

    public MutableGenome(Iterable<? extends Gene<?>> genome) {
        Iterables.addAll(genes, Iterables.transform(genome, new Function<Gene<?>, ForwardingGene<?>>() {
            @Override
            public ForwardingGene<?> apply(@Nullable Gene<?> gene) {
                return ForwardingGene.newInstance(gene);
            }
        }));
    }

    @Override
    public int size() {
        return genes.size();
    }

    @Override
    public double distance(Genome genome) {
        return Genes.normalizedDistance(this, genome);
    }

    @Override
    public Iterable<Gene<?>> findCopiesFor(Iterable<Gene<?>> thisGenes) {
        return null;
    }

    @Override
    public Genome mutated() {
        MutableGenome ret = new MutableGenome(null);
        ret.reset(this);
        for (ForwardingGene<?> gene : ret.genes) {
            gene.setDelegate(ImmutableGene.newMutatedCopy(gene));
        }
        return ret;
    }

    @Override
    public Genome recombined(Genome genome) {
        MutableGenome ret = new MutableGenome(null);
        ret.reset(this);
        // TODO: implement recombination
        return ret;
    }

    public Iterator<Gene<?>> delegateIterator() {
        return Iterators.transform(genes.iterator(), new Function<ForwardingGene<?>, Gene<?>>() {
            @Override
            public Gene<?> apply(ForwardingGene<?> forwardingGene) {
                return forwardingGene.getDelegate();
            }
        });
    }

    public void reset(Genome genome) {
        if (isCompatibleGenome(genome)) {
            Iterator<ForwardingGene<?>> thisGenesIterator = genes.iterator();
            Iterator<Gene<?>> thatGenesIterator = genome.iterator();

            while (thatGenesIterator.hasNext() && thatGenesIterator.hasNext()) {
                thisGenesIterator.next().setDelegate(DeepCloner.startWith(thatGenesIterator.next(), Gene.class));
            }
        }
    }

    public boolean isCompatibleGenome(Genome genome) {
        if (genome == null || this.size() != genome.size())
            return false;

        final Iterator<Gene<?>> other_genome_iter = genome.iterator();
        return(Iterables.all(this, new Predicate<Gene<?>>() {
            @Override
            public boolean apply(Gene<?> gene) {
                return other_genome_iter.hasNext() && other_genome_iter.next().isMutatedCopyOf(gene);
            }
        }));
    }

    public boolean addGene(Gene<?> gene) {
        return genes.add(new ForwardingGene(gene));
    }

    public boolean remove(final Gene gene) {
        return Iterables.removeIf(genes, new Predicate<ForwardingGene<?>>() {
            @Override
            public boolean apply(@Nullable ForwardingGene<?> forwardingGene) {
                assert forwardingGene != null;
                return forwardingGene.getDelegate().equals(gene);
            }
        });
    }

    public void removeAllGenes() {
        genes.clear();
    }

    public <T extends Gene<?>> T getGene(final String geneName, Class<T> geneClass) {
        return geneClass.cast(Iterables.find(this, new Predicate<Gene<?>>() {
            @Override
            public boolean apply(@Nullable Gene<?> forwardingGene) {
                assert forwardingGene != null;
                return forwardingGene.getName().equals(geneName);
            }
        }, null));
    }

    public Genome unmodifiableView() {
        throw new RuntimeException("Not yet implemented");
    }
}
