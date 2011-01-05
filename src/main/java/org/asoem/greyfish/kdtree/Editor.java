package org.asoem.sico.kdtree;


public interface Editor<T> {
	public T edit(T current) throws KeyDuplicateException;
}