package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.AgentComponent;

/**
 * User: christoph
 * Date: 11.11.11
 * Time: 09:24
 */
public interface GreyfishVariableSelector {
    Object $(String pattern, AgentComponent context);
}
