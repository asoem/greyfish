package org.asoem.greyfish.core.genes;

import static com.google.common.base.Preconditions.checkNotNull;

public class ForwardingGene<T> implements Gene<T> {

    private Gene<T> delegate;

    /**
     * Copy Constructor
     * @param gene the delegate to forward methods to
     */
    public ForwardingGene(Gene<T> gene) {
        setDelegate(gene);
    }

    @SuppressWarnings("unchecked")
    public void setDelegate(Gene<?> newDelegate) {
        if (checkNotNull(newDelegate) == this)
            throw new IllegalArgumentException("Delegation to 'this' is not allowed");
        this.delegate = Gene.class.cast(newDelegate);
    }


    @Override
    public T get() {
        return delegate.get();
    }

    @Override
    public Class<T> getSupplierClass() {
        return delegate.getSupplierClass();
    }

    @Override
    public GeneController<T> getGeneController() {
        return delegate.getGeneController();
    }

    @Override
    public boolean isMutatedCopyOf(Gene<?> gene) {
        return this.delegate.isMutatedCopyOf(gene);
    }

    public static <T> ForwardingGene<T> newInstance(Gene<T> delegate) {
        return new ForwardingGene<T>(delegate);
    }

    public Gene<T> getDelegate() {
        return delegate;
    }

    @Override
    public double distance(Gene<?> thatGene) {
        return delegate.distance(thatGene);
    }

    @Override
    public String toString() {
        return "Gene@[" + get() + "]";
    }
}
