package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.utils.collect.BidirectionalTreeNode;

public interface AgentNode extends BidirectionalTreeNode<AgentNode, AgentNode> {
    void initialize();
}
