package org.asoem.greyfish.lang;

import javax.annotation.Nullable;

/**
* User: christoph
* Date: 12.10.11
* Time: 15:04
*/
public interface BinaryFunction<T,R> {
    public R apply(@Nullable T t1, @Nullable T t2);
}
