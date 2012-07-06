package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import org.asoem.greyfish.utils.collect.Trees;

import java.util.Iterator;

/**
 * User: christoph
 * Date: 05.07.12
 * Time: 13:35
 */
public class AgentNodes {
    public static <T extends AgentNode<T>> Iterable<T> postOrderIteration(final T node) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return Trees.postOrderView(node, new Function<T, Iterator<T>>() {
                    @Override
                    public Iterator<T> apply(T input) {
                        final Iterable<T> children = input.children();
                        return children.iterator();
                    }
                });
            }
        };
    }
}
