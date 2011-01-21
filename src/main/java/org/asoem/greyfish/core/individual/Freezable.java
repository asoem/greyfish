package org.asoem.greyfish.core.individual;

public interface Freezable {
    void freeze();
    void checkIfFreezable(Iterable<? extends GFComponent> components) throws IllegalStateException;
    <T> T checkFrozen(T value) throws IllegalStateException;
    void checkFrozen() throws IllegalStateException;
}
