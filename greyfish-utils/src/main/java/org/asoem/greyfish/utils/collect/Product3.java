package org.asoem.greyfish.utils.collect;

/**
 * A generic interface for objects which encapsulate a triple of values. <p><b>Don't overuse this interface!</b> For a
 * matter of readability you should consider to write your own triple class if you don't use any utility function
 * associated with it (see {@link org.asoem.greyfish.utils.collect.Products}) or add methods with names of a defined
 * meaning to the implementation which delegate to the generic accessor functions.</p>
 *
 * @param <E1> the type of the first value
 * @param <E2> the type of the second value
 * @param <E3> the type of the second value
 */
public interface Product3<E1, E2, E3> {
    /**
     * Get the first value
     *
     * @return the first value
     */
    E1 first();

    /**
     * Get the second value
     *
     * @return the second value
     */
    E2 second();

    /**
     * Get the third value
     *
     * @return the third value
     */
    E3 third();
}
