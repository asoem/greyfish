package org.asoem.greyfish.lang;

import javax.annotation.Nullable;

public interface Functor<T> {
	public void apply(@Nullable T element);
}
