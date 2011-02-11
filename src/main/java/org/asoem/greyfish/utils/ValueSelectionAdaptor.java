package org.asoem.greyfish.utils;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueHolder;
import com.sun.xml.internal.fastinfoset.util.ValueArray;

import java.util.List;


public abstract class ValueSelectionAdaptor<T> extends ValueAdaptor<T> {

    protected SelectionInList<T> inList;
    private final Supplier<Iterable<T>> listSupplier;

    public ValueSelectionAdaptor(final String name, Class<T> clazz, final Supplier<T> o, final Supplier<Iterable<T>> values) {
        super(name, clazz, o);
        listSupplier = values;
        inList = new SelectionInList<T>(Lists.newArrayList(listSupplier.get()), this);
    }

    public ValueSelectionAdaptor(final String name, Class<T> clazz, final T o, final Iterable<T> values) {
       this(name, clazz, Suppliers.ofInstance(o), Suppliers.ofInstance(values));
    }

    public SelectionInList<T> getInList() {
        return inList;
    }

    @Override
    public void refresh() {
        inList.setList(Lists.newArrayList(listSupplier.get()));
        super.refresh();
    }
}
