package org.asoem.greyfish.core.agent;

public interface PropertyFunction<P, C, T> {
    T apply(P p, C c);
}
