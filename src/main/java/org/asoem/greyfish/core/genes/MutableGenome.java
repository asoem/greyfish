package org.asoem.greyfish.core.genes;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.individual.MutableComponentList;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.Iterator;
import java.util.List;

/**
 * User: christoph
 * Date: 09.09.11
 * Time: 15:21
 */
public class MutableGenome<E extends Gene<?>> extends MutableComponentList<E> implements Genome<E> {

    private List<Gene<?>> genes = Lists.newArrayList();

    public MutableGenome() {
    }

    public MutableGenome(Iterable<? extends Gene<?>> genome) {
        Iterables.addAll(genes, genome);
    }

    protected MutableGenome(MutableGenome<E> es, DeepCloner cloner) {
        super(es, cloner);
    }

    @Override
    public int size() {
        return genes.size();
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

    public Genome<E> unmodifiableView() {
        throw new RuntimeException("Not yet implemented"); // TODO: implement
    }

    @Override
    public Iterable<E> findCopiesFor(Iterable<? extends E> thisGenes) {
        throw new RuntimeException("Not yet implemented"); // TODO: implement
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MutableGenome that = (MutableGenome) o;

        return genes.equals(that.genes);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + genes.hashCode();
        return result;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MutableGenome<E>(this, cloner);
    }
}
