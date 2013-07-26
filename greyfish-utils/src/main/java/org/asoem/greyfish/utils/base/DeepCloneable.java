package org.asoem.greyfish.utils.base;

public interface DeepCloneable {
    /**
     * This method is usually called by a {@code DeepCloner} implementation to create a deep clone of this object
     *
     * @param cloner a {@code DeepCloner}
     * @return a deep clone (new instance) of this object
     */
    public DeepCloneable deepClone(DeepCloner cloner);
}