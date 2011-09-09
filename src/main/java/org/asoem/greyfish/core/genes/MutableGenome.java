package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

/**
 * User: christoph
 * Date: 09.09.11
 * Time: 15:21
 */
public class MutableGenome implements Genome {

    private List<ForwardingGene<?>> genes = Lists.newArrayList();

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
        MutableGenome ret = new MutableGenome();
        ret.reset(this);
        for (ForwardingGene<?> gene : ret.genes) {
            gene.setDelegate(ImmutableGene.newMutatedCopy(gene));
        }
        return ret;
    }

    @Override
    public Genome recombined(Genome rws) {
        MutableGenome ret = new MutableGenome();
        ret.reset(this);
        // TODO: implement recombination
        return ret;
    }

    @Override
    public Iterator<Gene<?>> iterator() {
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
                thisGenesIterator.next().setDelegate(ImmutableGene.copyOf(thatGenesIterator.next()));
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
}
