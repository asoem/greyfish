package org.asoem.greyfish.utils.space;

public interface TwoDimTree<T> extends KDTree<TwoDimTree.Node<T>> {

    /**
     * @param x     the x coordinate of the circle's center
     * @param y     the y coordinate of the circle's center
     * @param range the radius of the circle around the point at {@code x} and {@code y}
     * @return all nodes whose point intersects with the circle in undefined order
     */
    Iterable<SearchResult<T>> findNodes(double x, double y, final double range);

    interface Node<T> extends KDNode<Node<T>, T> {
        double xCoordinate();

        double yCoordinate();
    }

    interface SearchResult<T> {
        Node<T> node();

        double distance();
    }
}
