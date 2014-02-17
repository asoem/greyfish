package org.asoem.greyfish.utils.space;

import javax.annotation.Nullable;


public interface Projectable<T extends SpatialObject> {

    @Nullable
    T getProjection();

    void setProjection(@Nullable T projection);
}
