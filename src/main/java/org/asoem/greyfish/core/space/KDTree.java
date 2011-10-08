package org.asoem.greyfish.core.space;

public interface KDTree<T extends Object2D> {

    /**
     * Rebuild this {@code KDTree} with the given {@code elements}
     * @param elements the objects this {@code KDTree} will contain afterwards
     */
    void rebuild(final Iterable<? extends T> elements);

    /**
     * @param coordinates the search point
     * @param range the radius of the circle around {@code coordinates}
     * @return all objects whose anchor point ({@link T#getCoordinates()}) intersects with the circle defined by {@code coordinates} and {@code range}
     */
    Iterable<T> findObjects(Coordinates2D coordinates, final double range);
}
