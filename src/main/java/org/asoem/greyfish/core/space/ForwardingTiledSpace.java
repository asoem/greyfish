package org.asoem.greyfish.core.space;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.utils.space.MovingProjectable2D;

import java.util.List;

/**
 * User: christoph
 * Date: 30.08.12
 * Time: 09:39
 */
public abstract class ForwardingTiledSpace<O extends MovingProjectable2D, T extends Tile> extends ForwardingObject implements TiledSpace<O, T> {

    @Override
    protected abstract TiledSpace<O, T> delegate();
    
    @Override
    public int countObjects() {
        return delegate().countObjects();
    }

    @Override
    public boolean contains(double x, double y) {
        return delegate().contains(x, y);
    }

    @Override
    public List<O> getObjects() {
        return delegate().getObjects();
    }

    @Override
    public boolean insertObject(O projectable, double x, double y, double orientation) {
        return delegate().insertObject(projectable, x, y, orientation);
    }

    @Override
    public boolean removeObject(O object) {
        return delegate().removeObject(object);
    }

    @Override
    public void moveObject(O object2d) {
        delegate().moveObject(object2d);
    }

    @Override
    public Iterable<O> findObjects(double x, double y, double radius) {
        return delegate().findObjects(x, y, radius);
    }

    @Override
    public int getHeight() {
        return delegate().getHeight();
    }

    @Override
    public int getWidth() {
        return delegate().getWidth();
    }

    @Override
    public boolean hasTileAt(int x, int y) {
        return delegate().hasTileAt(x, y);
    }

    @Override
    public T getTileAt(int x, int y) {
        return delegate().getTileAt(x, y);
    }

    @Override
    public Iterable<T> getTiles() {
        return delegate().getTiles();
    }

    @Override
    public T getAdjacentTile(T borderedTile, TileDirection direction) {
        return delegate().getAdjacentTile(borderedTile, direction);
    }

    @Override
    public Iterable<O> getVisibleNeighbours(O object, double radius) {
        return delegate().getVisibleNeighbours(object, radius);
    }

    @Override
    public void insertObject(O agent) {
        delegate().insertObject(agent);
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public Iterable<O> getObjects(Iterable<? extends Tile> tiles) {
        return delegate().getObjects(tiles);
    }

    @Override
    public boolean removeIf(Predicate<O> predicate) {
        return delegate().removeIf(predicate);
    }
}
