package org.asoem.greyfish.core.traits;

import com.google.common.collect.Range;
import org.asoem.greyfish.core.agent.Agent;

/**
 * User: christoph
 * Date: 30.04.13
 * Time: 09:27
 */
public interface QuantitativeTrait<A extends Agent<A, ?>, T extends Comparable<T>> extends AgentTrait<A,T> {
    /**
     *
     * @return the range of values this trait can have
     */
    Range<T> getRange();
}
