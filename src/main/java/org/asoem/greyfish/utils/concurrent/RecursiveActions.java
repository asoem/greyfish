package org.asoem.greyfish.utils.concurrent;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import jsr166y.RecursiveAction;

import java.util.Collection;
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
     * Creates a {@code RecursiveAction} which applies a function {@code f} to all elements in the given {@code collection}.
     * If the size of the collection exceeds the given {@code size},
     * then the collection will be divided into partitions of that {@code size} and {@code f} will be applied on them in parallel.
     *
     * @param collection the elements on which the function will be applied
     * @param f the function to apply
     * @param size the desired size of each sublist (the last may be smaller)
     * @return a {@code RecursiveAction} which should be executed with a {@link jsr166y.ForkJoinPool}
     */
    public static <T> RecursiveAction foreach(final Collection<T> collection, final Function<? super T, Void> f, final int size) {
        checkNotNull(collection);
        checkNotNull(f);

        if (collection.isEmpty()) {
            return NULL_ACTION;
        }
        else {
            return new RecursiveAction() {
                @Override
                protected void compute() {
                    if (collection.size() < size) {
                        applyFunction(collection);
                    }
                    else {
                        checkState(inForkJoinPool(), "This action is executed from outside of an ForkJoinPool which is forbidden");
                        final List<RecursiveAction> applier =
                                ImmutableList.copyOf(Iterables.transform( // Lists.transform will lead to a deadlock
                                        Iterables.partition(collection, size),
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
