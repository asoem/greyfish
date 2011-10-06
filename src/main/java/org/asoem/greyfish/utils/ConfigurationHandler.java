package org.asoem.greyfish.utils;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public interface ConfigurationHandler {
	public <E> void add(ValueAdaptor<E> a);
	public <E> void add(FiniteSetValueAdaptor<E> a);
    public <E> void add(MapValuesAdaptor<E> multiValueAdaptor);
    void setWriteProtection(Supplier<Boolean> protection);
}
