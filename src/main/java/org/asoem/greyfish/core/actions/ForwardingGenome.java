package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;

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
    public boolean isCompatibleGenome(Genome<? extends E> es) {
        return delegate().isCompatibleGenome(es);
    }
}
