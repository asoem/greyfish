package org.asoem.greyfish.core.genes;

/**
 * User: christoph
 * Date: 23.05.11
 * Time: 15:12
 */
public class GeneControllerAdaptor<T> implements GeneController<T> {
    @Override
    public T mutate(T original) {
        return original;
    }

    @Override
    public double normalizedDistance(T orig, T copy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double normalizedWeightedDistance(T orig, T copy) {
        return normalizedDistance(orig, copy);
    }

    @Override
    public T createInitialValue() {
        throw new UnsupportedOperationException();
    }
}
