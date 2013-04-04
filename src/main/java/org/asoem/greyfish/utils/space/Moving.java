package org.asoem.greyfish.utils.space;

/**
 * User: christoph
 * Date: 24.02.12
 * Time: 17:00
 */
public interface Moving<T extends Motion> {
    T getMotion();
    void setMotion(T motion);
}
