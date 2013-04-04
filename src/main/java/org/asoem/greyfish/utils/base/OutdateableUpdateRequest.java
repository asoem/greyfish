package org.asoem.greyfish.utils.base;

/**
 * User: christoph
 * Date: 26.07.12
 * Time: 11:26
 */
public interface OutdateableUpdateRequest<T> extends UpdateRequest<T> {
    void outdate();
}
