package org.asoem.greyfish.lang;

import java.util.Iterator;

/**
 * User: christoph
 * Date: 05.10.11
 * Time: 17:26
 */
public interface TreeIterator<T> extends Iterator<T> {
    int depth();
}
