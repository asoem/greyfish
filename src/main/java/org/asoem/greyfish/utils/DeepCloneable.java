package org.asoem.greyfish.utils;

public interface DeepCloneable {
	public <T extends DeepCloneable> T deepClone(Class<T> clazz);
    public DeepCloneable deepCloneHelper(CloneMap map);
}