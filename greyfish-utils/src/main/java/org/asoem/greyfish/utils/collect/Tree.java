package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * A generic tree data structure.
 */
public interface Tree<N> extends Iterable<N> {
    /**
     * Get the root node of this tree.
     *
     * @return the root node.
     * @deprecated Use {@link #rootNode()}
     */
    @Nullable
    N root();

    /**
     * Get the optional root node of this tree.
     *
     * @return the root node as an {@code Optional}
     */
    Optional<N> rootNode();

    /**
     * Returns an iterator over the tree elements. The traversal oder is implementation dependent. If you need a
     * specific order use a {@link com.google.common.collect.TreeTraverser TreeTraverser} instead.
     *
     * @return an iterator over the tree elements
     */
    @Override
    Iterator<N> iterator();
}
