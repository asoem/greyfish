package org.asoem.greyfish.utils.base;

public interface DeepCloneable {
    /**
     * This method is usually called by a {@code CloneMap} implementation to create a deep clone of this object
     *
     * @param cloneMap a {@code CloneMap}
     * @return a deep clone (new instance) of this object
     */
    public DeepCloneable deepClone(CloneMap cloneMap);
}