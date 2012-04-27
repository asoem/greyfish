package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.actions.utils.ForwardingComponentList;

/**
 * User: christoph
 * Date: 19.09.11
 * Time: 15:59
 */
public abstract class ForwardingChromosome<E extends Gene<?>> extends ForwardingComponentList<E> implements Chromosome<E> {

    protected abstract Chromosome<E> delegate();

    @Override
    public double distance(Chromosome<? extends E> es) {
        return delegate().distance(es);
    }

    @Override
    public boolean isCompatible(Chromosome<? extends Gene<?>> es) {
        return delegate().isCompatible(es);
    }

    @Override
    public void initGenes() {
        delegate().initGenes();
    }

    @Override
    public void updateGenes(Iterable<?> values) {
        delegate().updateGenes(values);
    }

    @Override
    public void updateAllGenes(Chromosome<? extends E> genes) {
        delegate().updateAllGenes(genes);
    }

    @Override
    public ChromosomalOrigin getOrigin() {
        return delegate().getOrigin();
    }

    @Override
    public void setOrigin(ChromosomalOrigin origin) {
        delegate().setOrigin(origin);
    }
}
