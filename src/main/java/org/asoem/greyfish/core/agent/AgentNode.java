package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.Freezable;
import org.asoem.greyfish.utils.collect.TreeNode;

/**
 * User: christoph
 * Date: 05.07.12
 * Time: 12:32
 */
public interface AgentNode<T extends AgentNode<T>> extends TreeNode<T>, Freezable, DeepCloneable {
    void initialize();
}
