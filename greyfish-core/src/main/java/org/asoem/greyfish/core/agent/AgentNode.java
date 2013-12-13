package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.utils.collect.TreeNode;

public interface AgentNode extends TreeNode<AgentNode> {
    void initialize();
}
