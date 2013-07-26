package org.asoem.greyfish.utils.concurrent;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: christoph
 * Date: 19.10.11
 * Time: 14:24
 */
public final class RecursiveActions {

    private RecursiveActions() {}

    private static final RecursiveAction NULL_ACTION = new RecursiveAction() {
        @Override
        protected void compute() {
            /* NOP */
        }
    };

    /**
     * Creates a {@code RecursiveAction} which applies a function {@code f} to all elements in the given {@code list}.
     * If the size of the list exceeds the given {@code size},
     * then the list will be split into partitions of that {@code size} and {@code f} will be applied on them in parallel.
     *
     * @param list the elements on which the function will be applied
     * @param f the function to apply
     * @param size the desired size of each sublist (the last may be smaller)
     * @return a {@code RecursiveAction} which should be executed with a {@link ForkJoinPool}
     */
    public static <T> RecursiveAction foreach(final List<T> list, final Function<? super T, Void> f, final int size) {
        checkNotNull(list);
        checkNotNull(f);

        if (list.isEmpty()) {
            return NULL_ACTION;
        }
        else {
            return new RecursiveAction() {
                @Override
                protected void compute() {
                    checkState(inForkJoinPool(),
                            "This action is executed from outside of an ForkJoinPool which is forbidden");

                    if (list.size() < size) {
                        applyFunction(list);
                    }
                    else {
                        invokeAll(partitionAndFork(list));
                    }
                }

                private List<RecursiveAction> partitionAndFork(final List<T> list) {
                    // copyOf is prevents deadlock!
                    return ImmutableList.copyOf(Lists.transform(Lists.partition(list, size), new Function<List<T>, RecursiveAction>() {
                        @Nullable
                        @Override
                        public RecursiveAction apply(@Nullable final List<T> input) {
                            return new RecursiveAction() {
                                @Override
                                protected void compute() {
                                    applyFunction(input);
                                }
                            };
                        }
                    }));
                }

                private void applyFunction(final Iterable<T> elements) {
                    for (final T element : elements) {
                        f.apply(element);
                    }
                }
            };
        }
    }

}
