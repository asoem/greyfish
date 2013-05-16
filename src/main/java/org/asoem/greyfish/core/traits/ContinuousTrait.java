package org.asoem.greyfish.core.traits;

import com.google.common.collect.Range;
import org.asoem.greyfish.core.agent.Agent;

/**
 * User: christoph
 * Date: 30.04.13
 * Time: 09:27
 */
public interface ContinuousTrait<A extends Agent<A, ?>, T extends Comparable<T>> extends QuantitativeTrait<A, T> {
    Range<T> getRange();
}
