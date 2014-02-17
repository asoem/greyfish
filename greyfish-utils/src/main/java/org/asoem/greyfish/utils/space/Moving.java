package org.asoem.greyfish.utils.space;


public interface Moving<T extends Motion> {
    T getMotion();

    void setMotion(T motion);
}
