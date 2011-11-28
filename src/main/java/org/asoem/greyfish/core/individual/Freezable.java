package org.asoem.greyfish.core.individual;

public interface Freezable {
    void freeze();
    boolean isFrozen();
    void checkNotFrozen() throws IllegalStateException;
}
