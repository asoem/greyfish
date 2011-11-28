package org.asoem.greyfish.utils.base;

public interface DeepCloneable {
    /**
     * This method is called by a {@code DeepCloner} instance to request a deep clone {@code this} object
     * @param cloner the {@code DeepCloner} instance
     * @return a deep clone (new instance) of {@code this} object
     */
    public DeepCloneable deepClone(DeepCloner cloner);
}