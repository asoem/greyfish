package org.asoem.greyfish.utils.base;

import javax.annotation.Nullable;

/**
* User: christoph
* Date: 26.07.12
* Time: 10:51
*/
@Deprecated
public interface UpdateRequest<T> {
    boolean isOutdated(@Nullable T input);
    void updated();
}
