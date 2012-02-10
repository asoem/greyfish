package org.asoem.greyfish.utils.collect;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.math.RandomUtils;

/**
 * User: christoph
 * Date: 10.02.12
 * Time: 12:54
 */
public class RandomSelectionStrategy<E> implements ElementSelectionStrategy<E> {
    @Override
    public <T extends E> T pick(Iterable<? extends T> elements) {
        return Iterables.get(elements, RandomUtils.nextInt(Iterables.size(elements)));
    }
}
