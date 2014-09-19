package org.asoem.greyfish.utils.collect;

import com.google.common.base.Optional;

import javax.annotation.Nullable;

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
}
