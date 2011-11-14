package org.asoem.greyfish.utils.collect;

import java.util.Iterator;

/**
 * User: christoph
 * Date: 05.10.11
 * Time: 17:26
 */
public interface TreeIterator<T> extends Iterator<T> {
    /**
     *
     * @return the depth in the Tree this iterator is iterating over. Root is at depth 0, therefore the depth is initially at -1.
     */
    int depth();
}
