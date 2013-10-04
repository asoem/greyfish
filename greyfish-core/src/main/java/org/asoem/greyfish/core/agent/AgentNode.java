package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.collect.BidirectionalTreeNode;

/**
 * User: christoph
 * Date: 05.07.12
 * Time: 12:32
 */
public interface AgentNode extends BidirectionalTreeNode<AgentNode>, DeepCloneable {
    void initialize();
}
