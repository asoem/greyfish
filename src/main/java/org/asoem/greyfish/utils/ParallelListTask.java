package org.asoem.greyfish.utils;

import jsr166y.ForkJoinPool;
import jsr166y.RecursiveAction;
import org.asoem.greyfish.lang.Functor;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 13.10.11
 * Time: 11:38
 */
public class ParallelListTask<T> extends RecursiveAction {

    private final List<T> elements;
    private final Functor<T> elementFunctor;
    private final int parallelizationThreshold;

    public ParallelListTask(Functor<T> elementFunctor, List<T> elements, int parallelizationThreshold) {
        this.elementFunctor = checkNotNull(elementFunctor);
        this.elements = checkNotNull(elements);
        this.parallelizationThreshold = parallelizationThreshold;
    }

    @Override
    protected void compute() {
        invokeAll(new ParallelListIterationInternal(0, elements.size()));
    }

    public void execute(ForkJoinPool forkJoinPool) {
        checkNotNull(forkJoinPool).execute(this);
    }

    private class ParallelListIterationInternal extends RecursiveAction {

        private final int start;
        private final int end;

        public ParallelListIterationInternal(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected void compute() {
            int diff = end - start;
            int middle = diff / 2;

            if (diff == 0) {
            }
            else if (diff == 1) {
                elementFunctor.apply(elements.get(start));
            }
            else if (diff < parallelizationThreshold) {
                for (T element : elements.subList(start, end)) {
                    elementFunctor.apply(element);
                }
            }
            else {
                invokeAll(
                        new ParallelListIterationInternal(start, middle),
                        new ParallelListIterationInternal(middle + 1, end)
                );
            }
        }
    }

    public static <T> Builder<T> parallelApply(Functor<T> functor) {
        return new Builder<T>(functor);
    }

    public static class Builder<T> {
        private final Functor<T> functor;
        private int parallelizationThreshold;

        public Builder(Functor<T> functor) {
            this.functor = functor;
        }

        public Builder<T> squential(int parallelizationThreshold) {
            this.parallelizationThreshold = parallelizationThreshold;
            return this;
        }

        public ParallelListTask<T> on(List<T> list) {
            parallelizationThreshold = 1;
            return new ParallelListTask<T>(functor, list, parallelizationThreshold);
        }
    }
}
