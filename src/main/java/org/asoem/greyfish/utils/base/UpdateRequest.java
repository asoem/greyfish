package org.asoem.greyfish.utils.base;

import com.google.common.base.Predicate;

/**
* User: christoph
* Date: 26.07.12
* Time: 10:51
*/
public interface UpdateRequest<T> extends Predicate<T> {
    void done();
}
