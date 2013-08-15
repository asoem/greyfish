package org.asoem.greyfish.core.io;

/**
 * A Storeable is an object which defines a {@link #convert()} method to convert it's state
 * into a value which can be stored into the destination database of a {@link SimulationLogger}.
 */
public interface Storeable<T> {

    /**
     * Convert the internal state of this object into a value of type {@code T}, which can be stored into the database.
     * @return this object converted into a value of type {@code T}
     */
    T convert();
}
