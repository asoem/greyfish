package org.asoem.greyfish.utils.collect;

import java.util.List;

public interface LinearSequence<T> extends List<T> {
    /**
     * Alias for {@link #size()}.
     *
     * @return the length of this sequence
     */
    int length();
}
