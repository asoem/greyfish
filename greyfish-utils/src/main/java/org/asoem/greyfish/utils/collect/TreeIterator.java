package org.asoem.greyfish.utils.collect;

import java.util.Iterator;


public interface TreeIterator<T> extends Iterator<T> {
    /**
     * @return the depth in the Tree this iterator is iterating over. Root is at depth 0, therefore the depth is
     * initially at -1.
     */
    int depth();
}
