package org.asoem.greyfish.utils.collect;

import java.util.AbstractList;

/**
 * This class provides a skeletal implementation of {@link org.asoem.greyfish.utils.collect.LinearSequence}.
 *
 * @param <T> the element type
 */
public abstract class AbstractLinearSequence<T> extends AbstractList<T> implements LinearSequence<T> {
    @Override
    public final int size() {
        return length();
    }
}
