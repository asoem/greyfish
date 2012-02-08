package org.asoem.greyfish.core.genes;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.MutableComponentList;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: christoph
 * Date: 09.09.11
 * Time: 15:21
 */
public class MutableGenome<E extends Gene<?>> extends MutableComponentList<E> implements Genome<E> {

    public MutableGenome() {
    }

    public MutableGenome(Iterable<? extends E> genome) {
        Iterables.addAll(delegate(), genome);
    }

    protected MutableGenome(MutableGenome<E> es, DeepCloner cloner) {
        super(es, cloner);
    }

    @Override
    public double distance(Genome<? extends E> genome) {
        return Genes.normalizedDistance(this, genome);
    }

    @Override
    public boolean isCompatibleGenome(Genome<? extends Gene<?>> genome) {
        if (genome == null || this.size() != genome.size())
            return false;

        final Iterator<? extends Gene<?>> other_genome_iter = genome.iterator();
        return(Iterables.all(this, new Predicate<Gene<?>>() {
            @Override
            public boolean apply(Gene<?> gene) {
                return other_genome_iter.hasNext() && other_genome_iter.next().isMutatedCopy(gene);
            }
        }));
    }

    @Override
    public void replaceGenes(Genome<? extends E> genome) {
        checkArgument(isCompatibleGenome(genome));

        clear();
        addAll(genome);
    }

    public Genome<E> unmodifiableView() {
        throw new RuntimeException("Not yet implemented"); // TODO: implement
    }

    @Override
    public Iterable<E> findCopiesFor(Iterable<? extends E> thisGenes) {
        throw new RuntimeException("Not yet implemented"); // TODO: implement
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MutableGenome<E>(this, cloner);
    }
}
