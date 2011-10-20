package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;

import java.util.Iterator;
import java.util.Stack;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 05.10.11
 * Time: 10:00
 */
public class Trees {

    public static <T> TreeIterator<T> postOrderView(final T root, final Function<? super T, ? extends Iterator<? extends T>> childrenFunction) {
        checkNotNull(root);

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
}
