package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.collect.BidirectionalTreeNode;

public interface AgentNode extends BidirectionalTreeNode<AgentNode, AgentNode>, DeepCloneable {
    void initialize();
}
