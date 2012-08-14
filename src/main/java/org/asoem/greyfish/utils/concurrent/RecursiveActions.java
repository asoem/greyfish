package org.asoem.greyfish.utils.concurrent;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import jsr166y.RecursiveAction;
import org.asoem.greyfish.utils.base.VoidFunction;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: christoph
 * Date: 19.10.11
 * Time: 14:24
 */
public class RecursiveActions {

    private static final RecursiveAction NULL_ACTION = new RecursiveAction() {
        @Override
        protected void compute() {
            /* NOP */
        }
    };

    /**
     * Creates a {@code RecursiveAction} which applies a function {@code f} to all elements in the given {@code list}.
     * If the size of the list exceeds the given {@code size},
     * then the list will be divided into partitions of that {@code size} and {@code f} will be applied on them in parallel.
     * @param list the elements on which the function will be applied
     * @param f the function to apply
     * @param size the desired size of each sublist (the last may be smaller)
     * @param <T> the type of the elements in the list
     * @return a {@code RecursiveAction} which should be executed with a {@link jsr166y.ForkJoinPool}
     */
    public static <T> RecursiveAction foreach(final List<T> list, final VoidFunction<? super T> f, final int size) {
        checkNotNull(list);
        checkNotNull(f);

        if (list.isEmpty()) {
            return NULL_ACTION;
        }
        else {
            return new RecursiveAction() {
                @Override
                protected void compute() {
                    if (list.size() < size) {
                        applyFunction(list);
                    }
                    else {
                        checkState(inForkJoinPool(), "This action is executed from outside of an ForkJoinPool which is forbidden");
                        final List<RecursiveAction> applier =
                                ImmutableList.copyOf(Iterables.transform( // Lists.transform will lead to a deadlock
                                        Lists.partition(list, size),
                                        new Function<List<T>, RecursiveAction>() {
                                            @Override
                                            public RecursiveAction apply(final List<T> sublist) {
                                                return new RecursiveAction() {
                                                    @Override
                                                    protected void compute() {
                                                        applyFunction(sublist);
                                                    }
                                                };
                                            }
                                        }));
                        invokeAll(applier);
                    }
                }

                private void applyFunction(Iterable<T> elements) {
                    for (T element : elements)
                        f.apply(element);
                }
            };
        }
    }

}
