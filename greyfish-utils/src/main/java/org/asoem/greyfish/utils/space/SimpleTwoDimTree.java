package org.asoem.greyfish.utils.space;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;

import javax.annotation.Nullable;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.List;

/**
 * A simple implementation of a two dimensional kd-tree with no search optimization.
 *
 * @param <T> the value type
 */
public final class SimpleTwoDimTree<T> extends AbstractCollection<TwoDimTree.Node<T>> implements TwoDimTree<T> {

    private final Optional<Node<T>> rootNode;
    private final TreeTraverser<Node<T>> treeTraverser = new TwoDimTreeTraverser<>();
    private final Supplier<Integer> lazySize = Suppliers.memoize(new Supplier<Integer>() {
        @Override
        public Integer get() {
            if (rootNode().isPresent()) {
                return Iterables.size(treeTraverser.postOrderTraversal(rootNode.get()));
            } else {
                return 0;
            }
        }
    });

    private SimpleTwoDimTree(@Nullable final Node<T> rootNode) {
        this.rootNode = Optional.fromNullable(rootNode);
    }

    public static <T> SimpleTwoDimTree<T> create(final Node<T> rootNode) {
        return new SimpleTwoDimTree<T>(rootNode);
    }

    @Override
    public int dimensions() {
        return 2;
    }

    @Override
    public int size() {
        return lazySize.get();
    }

    @Nullable
    @Override
    public Node<T> root() {
        return rootNode().orNull();
    }

    @Override
    public Optional<Node<T>> rootNode() {
        return rootNode;
    }

    @Override
    public Iterable<SearchResult<T>> findNodes(final double x, final double y, final double range) {
        final FluentIterable<Node<T>> nodes = treeTraverser.preOrderTraversal(root());
        List<SearchResult<T>> searchResults = Lists.newArrayList();
        for (final Node<T> node : nodes) {
            final double distance = Geometry2D.distance(ImmutablePoint2D.at(x, y), ImmutablePoint2D.at(node.xCoordinate(), node.yCoordinate()));
            if (distance <= range) {
                searchResults.add(new SearchResult<T>() {
                    @Override
                    public Node<T> node() {
                        return node;
                    }

                    @Override
                    public double distance() {
                        return distance;
                    }
                });
            }
        }
        return searchResults;
    }

    @Override
    public Iterator<Node<T>> iterator() {
        return treeTraverser.postOrderTraversal(root()).iterator();
    }
}
