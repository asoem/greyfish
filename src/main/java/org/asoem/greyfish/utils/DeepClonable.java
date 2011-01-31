package org.asoem.greyfish.utils;

public interface DeepClonable {
	public <T extends DeepClonable> T deepClone(Class<T> clazz);
    public DeepClonable deepCloneHelper(CloneMap map);
}