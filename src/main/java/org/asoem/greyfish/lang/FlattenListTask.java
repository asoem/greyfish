package org.asoem.greyfish.lang;

import jsr166y.RecursiveTask;

import java.util.List;

/**
 * User: christoph
 * Date: 12.10.11
 * Time: 14:16
 */
public class FlattenListTask<T> extends RecursiveTask<T> {

    private final BinaryFunction<? super T, T> function;
    private final List<? extends T> list;

    public FlattenListTask(List<? extends T> list, BinaryFunction<? super T, T> resultCombineFunction) {
        this.list = list;
        this.function = resultCombineFunction;
    }

    @Override
    protected T compute() {
        return new ListApplyInternalTask(0, list.size()).invoke();
    }

    private class ListApplyInternalTask extends RecursiveTask<T> {

        private final int start;
        private final int end;

        public ListApplyInternalTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected T compute() {
            int subListSize = end - start;

            if (subListSize == 1) {
                return list.get(start);
            }
            else if (subListSize == 2) {
                return function.apply(list.get(start), list.get(end));
            }
            else if (subListSize > 2) {

                int middle = (end - start) / 2;

                ListApplyInternalTask left = new ListApplyInternalTask(start, middle);
                ListApplyInternalTask right = new ListApplyInternalTask(middle+1, end);

                right.fork();
                return function.apply(left.invoke(), right.join());
            }
            else {
                throw new AssertionError("end must be greater than start: " + end + " < " + start);
            }
        }
    }
}
