package org.asoem.greyfish.core.traits;

import com.google.common.collect.Ordering;
import org.asoem.greyfish.core.agent.Agent;

import java.util.Set;

/**
 * User: christoph
 * Date: 30.04.13
 * Time: 09:43
 */
public interface DiscreteTrait<A extends Agent<A, ?>, T> extends QuantitativeTrait<A, T> {
    /**
     *
     * @return all possible values this trait accepts
     */
    Set<T> getStates();

    /**
     *
     * @return the number of values this trait can have
     */
    int size();

    // boolean isInfinite();

    Ordering<T> getOrdering();
}
