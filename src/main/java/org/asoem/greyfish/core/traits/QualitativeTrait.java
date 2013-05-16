package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.core.agent.Agent;

/**
 * User: christoph
 * Date: 30.04.13
 * Time: 09:43
 *
 * A QualitativeTrait is a trait which has no continuous variation.
 * Countability, which is required for a discrete distribution, is not obligate.
 * Therefore this interface neither defines a size() or getPossibleValues() method
 * like
 */
public interface QualitativeTrait<A extends Agent<A, ?>, T> extends AgentTrait<A,T> {
}
