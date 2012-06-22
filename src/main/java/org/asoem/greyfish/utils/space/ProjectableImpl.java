package org.asoem.greyfish.utils.space;

/**
 * User: christoph
 * Date: 22.06.12
 * Time: 11:47
 */
public class ProjectableImpl<T extends SpatialObject> implements Projectable<T> {
    private T projection;

    @Override
    public T getProjection() {
        return projection;
    }

    @Override
    public void setProjection(T projection) {
        this.projection = projection;
    }
}
