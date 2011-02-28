package org.asoem.greyfish.core.genes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ForwardingGene<T> implements Gene<T> {

    private Gene<T> delegate;

    /**
     * Copy Constructor
     * @param gene the delegate to forward methods to
     */
    public ForwardingGene(Gene<T> gene) {
        this.delegate = checkNotNull(gene);
    }

    @SuppressWarnings("unchecked")
    public void setDelegate(Gene<?> newDelegate) {
        checkArgument(getSupplierClass().equals(newDelegate.getSupplierClass()),
                "type of the given newDelegate's supplier class do not match this newDelegate's supplier class: "
                        + delegate.getSupplierClass() + " != " + newDelegate.getSupplierClass());
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
    public MutationOperator<T> getMutationFunction() {
        return delegate.getMutationFunction();
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
}
