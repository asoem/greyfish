package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.actions.utils.ForwardingComponentList;

/**
 * User: christoph
 * Date: 19.09.11
 * Time: 15:59
 */
public abstract class ForwardingGenome<E extends Gene<?>> extends ForwardingComponentList<E> implements Genome<E> {

    protected abstract Genome<E> delegate();

    @Override
    public double distance(Genome<? extends E> es) {
        return delegate().distance(es);
    }

    @Override
    public Iterable<E> findCopiesFor(Iterable<? extends E> thisGenes) {
        return delegate().findCopiesFor(thisGenes);
    }

    @Override
    public boolean isCompatibleGenome(Genome<? extends Gene<?>> es) {
        return delegate().isCompatibleGenome(es);
    }

    @Override
    public void initGenes() {
        delegate().initGenes();
    }
}
