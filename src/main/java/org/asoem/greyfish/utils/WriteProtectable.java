package org.asoem.greyfish.utils;

import com.google.common.base.Supplier;

/**
 * User: christoph
 * Date: 09.09.11
 * Time: 14:30
 */
public interface WriteProtectable {
    void setWriteProtection(Supplier<Boolean> writeProtection);

    boolean isWriteProtected();
}
