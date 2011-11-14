package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 05.10.11
 * Time: 10:00
 */
public class Trees {

    public static <T> TreeIterator<T> postOrderView(@Nullable final T root, final Function<? super T, ? extends Iterator<? extends T>> childrenFunction) {
        checkNotNull(childrenFunction);

        if (root == null)
            return emptyTreeIterator();

        return new AbstractStackBasedTreeIterator<T>(new NodeIteratorPair<T>(root, childrenFunction.apply(root))) {

            @Override
            protected T computeNext() {
                if (pairStack.empty()) {
                    return endOfData();
                }
                else {

                    NodeIteratorPair<T> currentPair = pairStack.peek();

                    while (currentPair.iterator.hasNext()) {
                        T nextNode = currentPair.iterator.next();
                        currentPair = new NodeIteratorPair<T>(nextNode, childrenFunction.apply(nextNode));

                        pairStack.push(currentPair);
                    }

                    return pairStack.pop().node;
                }
            }
        };
    }

    public static <T> TreeIterator<T> preOrderView(@Nullable final T root, final Function<? super T, ? extends Iterator<? extends T>> childrenFunction) {
        checkNotNull(childrenFunction);

        if (root == null)
            return emptyTreeIterator();

        return new AbstractStackBasedTreeIterator<T>(new NodeIteratorPair<T>(root, childrenFunction.apply(root))) {

            boolean firstCallToComputeNext = true;

            @Override
            protected T computeNext() {
                if (pairStack.empty()) {
                    return endOfData();
                }
                else {

                    NodeIteratorPair<T> currentPair = pairStack.peek();

                    if (firstCallToComputeNext) {
                        firstCallToComputeNext = false;
                        return currentPair.node;
                    }

                    if (currentPair.iterator.hasNext()) {
                        T ret = currentPair.iterator.next();
                        pairStack.push(new NodeIteratorPair<T>(ret, childrenFunction.apply(ret)));
                        return ret;
                    }
                    else {
                        pairStack.pop();
                        return computeNext();
                    }
                }
            }
        };
    }

    private static class NodeIteratorPair<T> {
        private final T node;
        private final Iterator<? extends T> iterator;

        private NodeIteratorPair(T node, Iterator<? extends T> iterator) {
            this.node = node;
            this.iterator = iterator;
        }
    }

    private abstract static class AbstractStackBasedTreeIterator<T> extends AbstractIterator<T> implements TreeIterator<T> {

        protected final Stack<NodeIteratorPair<T>> pairStack = new Stack<NodeIteratorPair<T>>();

        protected AbstractStackBasedTreeIterator(NodeIteratorPair<T> root) {
            pairStack.push(root);
        }

        @Override
        public int depth() {
            return pairStack.size() - 1;
        }
    }

    @SuppressWarnings({"unchecked"}) // EMPTY_ITERATOR contains no elements per definition, and therefore casting is ok.
    public static <T> TreeIterator<T> emptyTreeIterator() {
        return (TreeIterator<T>) EMPTY_ITERATOR;
    }

    private static final TreeIterator<?> EMPTY_ITERATOR = new TreeIterator<Object>() {
        @Override
        public int depth() {
            return -1;
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };
}
