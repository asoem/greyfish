package org.asoem.greyfish.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.jgoodies.binding.list.SelectionInList;


public abstract class FiniteSetValueAdaptor<T> extends ValueAdaptor<T> {

    protected SelectionInList<T> inList;

    public FiniteSetValueAdaptor(final String name, Class<T> clazz) {
       super(name, clazz);
       inList = new SelectionInList<T>(Iterables.toArray(values(), clazz), this);
    }

    public SelectionInList<T> getSet() {
        return inList;
    }

    public abstract Iterable<T> values();

    @Override
    public void refresh() {
        inList.setList(Lists.<T>newArrayList(values()));
        super.refresh();
    }
}
