package org.asoem.greyfish.core.individual;

public interface Freezable {
    void freeze();
    boolean isFrozen();
    void checkConsistency(Iterable<? extends GFComponent> components) throws IllegalStateException;
    <T> T checkFrozen(T value) throws IllegalStateException;
    void checkNotFrozen() throws IllegalStateException;
}
