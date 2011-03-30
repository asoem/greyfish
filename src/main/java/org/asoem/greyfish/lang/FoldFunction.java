package org.asoem.greyfish.lang;

public abstract class FoldFunction<T,E> {
    public abstract E firstValue();
    public abstract E apply(T left, T right);
}
