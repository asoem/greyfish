package org.asoem.greyfish.utils.collect;

import java.util.AbstractList;

/**
 * User: christoph
 * Date: 01.02.13
 * Time: 12:14
 */
public abstract class AbstractLinearSequence<T> extends AbstractList<T> implements LinearSequence<T> {
    @Override
    public int size() {
        return length();
    }
}
