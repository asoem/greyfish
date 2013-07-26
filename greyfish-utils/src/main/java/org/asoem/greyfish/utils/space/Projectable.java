package org.asoem.greyfish.utils.space;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 24.02.12
 * Time: 12:56
 */
public interface Projectable<T extends SpatialObject> {
    @Nullable
    T getProjection();
    void setProjection(T projection);
}
