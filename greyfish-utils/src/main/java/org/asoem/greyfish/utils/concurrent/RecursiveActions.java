/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.utils.concurrent;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;
import jsr166y.RecursiveTask;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;


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
     * If the size of the list exceeds the given {@code size}, then the list will be split into partitions of that
     * {@code size} and {@code f} will be applied on them in parallel.
     *
     * @param list the elements on which the function will be applied
     * @param f    the function to apply
     * @param size the desired size of each sublist (the last may be smaller)
     * @return a {@code RecursiveAction} which should be executed with a {@link ForkJoinPool}
     */
    public static <T> RecursiveAction foreach(final List<T> list, final Function<? super T, Void> f, final int size) {
        checkNotNull(list);
        checkNotNull(f);

        if (list.isEmpty()) {
            return NULL_ACTION;
        } else {
            return new RecursiveAction() {
                @Override
                protected void compute() {
                    checkState(inForkJoinPool(),
                            "This action is executed from outside of an ForkJoinPool which is forbidden");

                    if (list.size() < size) {
                        applyFunction(list);
                    } else {
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

    public static <T, S> RecursiveAction foldLeft(final T initialValue, final List<T> list, final Function<? super T, S> f, final int size) {
        checkNotNull(list);
        checkNotNull(f);

        if (list.isEmpty()) {
            return NULL_ACTION;
        } else {
            return new RecursiveAction() {
                @Override
                protected void compute() {
                    checkState(inForkJoinPool(),
                            "This action is executed from outside of an ForkJoinPool which is forbidden");

                    if (list.size() < size) {
                        applyFunction(list);
                    } else {
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

    public static final class Transform<E1, E2> extends RecursiveTask<Iterable<E2>> {

        private final List<? extends E1> list;
        private final int start;
        private final int end;
        private final int sequentialThreshold;
        private final Function<? super E1, ? extends E2> transform;

        public Transform(final List<? extends E1> list, final int start, final int end, final int sequentialThreshold, final Function<? super E1, ? extends E2> transform) {
            this.list = list;
            this.start = start;
            this.end = end;
            this.sequentialThreshold = sequentialThreshold;
            this.transform = transform;
        }

        @Override
        protected Iterable<E2> compute() {
            if (end - start <= sequentialThreshold) {
                return Iterables.transform(list.subList(start, end), transform);
            } else {
                int mid = start + (end - start) / 2;
                final Transform<E1, E2> leftTransform = new Transform<E1, E2>(list, start, mid, sequentialThreshold, transform);
                final Transform<E1, E2> rightTransform = new Transform<E1, E2>(list, mid, end, sequentialThreshold, transform);

                leftTransform.fork();
                final Iterable<E2> rightAnswer = rightTransform.invoke();
                final Iterable<E2> leftAnswer = leftTransform.join();
                return Iterables.concat(leftAnswer, rightAnswer);
            }
        }
    }

}
