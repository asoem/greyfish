package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.collect.RangeElement;

/**
 * User: christoph
 * Date: 01.03.11
 * Time: 16:08
 */
public interface RangeElementProperty<T extends Number & Comparable<T>, A extends Agent<A, ?>> extends AgentProperty<A, T>, RangeElement<T> {
}
