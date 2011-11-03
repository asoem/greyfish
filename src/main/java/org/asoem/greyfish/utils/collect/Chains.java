package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 03.11.11
 * Time: 08:55
 */
public class Chains {

    public static <T> Iterable<T> of(@Nullable final T root, final Function<? super T, ? extends T> nextElementFunction) {
        checkNotNull(nextElementFunction);

        if (root == null) {
            return ImmutableList.of();
        }
        else {
            return new Iterable<T>() {
                @Override
                public Iterator<T> iterator() {
                    return new AbstractIterator<T>() {
                        T next = root;

                        @Override
                        protected T computeNext() {
                            if (next == null)
                                return endOfData();
                            else {
                                T ret = next;
                                next = nextElementFunction.apply(next);
                                return ret;
                            }
                        }
                    };
                }
            };
        }
    }
}
