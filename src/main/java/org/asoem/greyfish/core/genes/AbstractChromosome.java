package org.asoem.greyfish.core.genes;

import com.google.common.collect.ForwardingList;
import org.asoem.greyfish.core.individual.ComponentList;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: christoph
 * Date: 08.02.12
 * Time: 11:08
 */
public abstract class AbstractChromosome<E extends Gene<?>> extends ForwardingList<E> implements Chromosome<E> {

    protected AbstractChromosome() {
    }

    public boolean isCompatible(Chromosome<? extends Gene<?>> chromosome) {
        if (chromosome == null || this.size() != chromosome.size())
            return false;

        final Iterator<E> this_genome_iterator = this.iterator();

        for (Gene<?> gene : chromosome) {
            if (!this_genome_iterator.next().isMutatedCopy(gene)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void updateAllGenes(Chromosome<? extends E> chromosome) {
        checkArgument(isCompatible(chromosome), "Given chromosome %s is not compatible to this chromosome %s.", chromosome, this);

        final Iterator<? extends E> sourceIterator = chromosome.iterator();
        final Iterator<E> destinationIterator = this.iterator();

        while (sourceIterator.hasNext() && destinationIterator.hasNext()) {
            E source =  sourceIterator.next();
            E destination = destinationIterator.next();

            destination.setValue(source.get());
        }
    }

    @Override
    public void initGenes() {
        for(Gene<?> gene : this) {
            gene.setValue(gene.getGeneController().createInitialValue());
        }
    }

    @Override
    protected abstract ComponentList<E> delegate();

    @Override
    public <T extends E> T find(String name, Class<T> clazz) {
        return delegate().find(name, clazz);
    }

    @Override
    public double distance(Chromosome<? extends E> that) {
        return Genes.normalizedDistance(this, that);
    }

}
