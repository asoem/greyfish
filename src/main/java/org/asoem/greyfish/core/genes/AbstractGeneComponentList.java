package org.asoem.greyfish.core.genes;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.ComponentList;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 08.02.12
 * Time: 11:08
 */
public abstract class AbstractGeneComponentList<E extends GeneComponent<?>> extends ForwardingList<E> implements GeneComponentList<E> {

    private ChromosomalOrigin chromosomalOrigin = NoChromosomalOrigin.INSTANCE;

    protected AbstractGeneComponentList() {
    }

    public boolean isCompatible(GeneComponentList<? extends GeneComponent<?>> geneComponentList) {
        if (geneComponentList == null || this.size() != geneComponentList.size())
            return false;

        final Iterator<E> this_genome_iterator = this.iterator();

        for (GeneComponent<?> gene : geneComponentList) {
            if (!this_genome_iterator.next().isMutatedCopy(gene)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void updateAllGenes(GeneComponentList<? extends E> geneComponentList) {
        checkArgument(isCompatible(geneComponentList), "Given geneComponentList %s is not compatible to this geneComponentList %s.", geneComponentList, this);

        final Iterator<? extends E> sourceIterator = geneComponentList.iterator();
        final Iterator<E> destinationIterator = this.iterator();

        while (sourceIterator.hasNext() && destinationIterator.hasNext()) {
            E source =  sourceIterator.next();
            E destination = destinationIterator.next();

            destination.setValue(source.get());
        }
    }

    @Override
    public void updateGenes(Iterable<?> values) {

        checkNotNull(values);
        checkArgument(Iterables.size(values) == size());

        final Iterator<?> sourceIterator = values.iterator();
        final Iterator<E> destinationIterator = this.iterator();

        while (sourceIterator.hasNext() && destinationIterator.hasNext()) {
            Object source =  sourceIterator.next();
            E destination = destinationIterator.next();

            destination.setValue(source);
        }
    }


    @Override
    public void initGenes() {
        for(GeneComponent<?> gene : this) {
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
    public double distance(GeneComponentList<? extends E> that) {
        return GenesComponents.normalizedDistance(this, that);
    }

    @Override
    public ChromosomalOrigin getOrigin() {
        return chromosomalOrigin;
    }

    @Override
    public void setOrigin(ChromosomalOrigin origin) {
        this.chromosomalOrigin = checkNotNull(origin);
    }


}
