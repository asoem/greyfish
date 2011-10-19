package org.asoem.greyfish.utils;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import jsr166y.RecursiveAction;
import org.asoem.greyfish.lang.Functor;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 19.10.11
 * Time: 14:24
 */
public class ConcurrentIterables {

    private static final RecursiveAction NULL_ACTION = new RecursiveAction() {
        @Override
        protected void compute() {
            /* NOP */
        }
    };

    public static <T> RecursiveAction create(final Iterable<T> iterable, final Functor<? super T> functor, final int threshold) {
        checkNotNull(iterable);
        checkNotNull(functor);

        if (Iterables.isEmpty(iterable)) {
            return NULL_ACTION;
        }
        else {
            return new RecursiveAction() {
                @Override
                protected void compute() {
                    invokeAll(ImmutableList.copyOf(Iterables.transform(Iterables.partition(iterable, threshold), new Function<List<T>, RecursiveAction>() {
                        @Override
                        public RecursiveAction apply(@Nullable final List<T> elements) {
                            return new RecursiveAction() {
                                @Override
                                protected void compute() {
                                    for (T element : elements)
                                        functor.apply(element);
                                }
                            };
                        }
                    })));
                }
            };
        }
    }
}
