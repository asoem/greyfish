package org.asoem.greyfish.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.Validatable;
import com.jgoodies.validation.ValidationResult;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


public abstract class ValueAdaptor<T> implements ValueModel, Validatable, PropertyChangeListener {

    public final String name;
    public final Class<T> clazz;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final Supplier<? extends T> supplier;

    public ValueAdaptor(String name, Class<T> clazz, T o) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(clazz);

        this.name = name;
        this.clazz = clazz;
        this.supplier = Suppliers.ofInstance(o);
    }

    public ValueAdaptor(String name, Class<T> clazz, Supplier<? extends T> o) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(clazz);

        this.name = name;
        this.clazz = clazz;
        this.supplier = o;
    }

    @Override
    public void addValueChangeListener(PropertyChangeListener arg0) {
        propertyChangeSupport.addPropertyChangeListener(arg0);
    }

    @Override
    public T getValue() {
        return supplier.get();
    }

    @Override
    public void removeValueChangeListener(PropertyChangeListener arg0) {
        propertyChangeSupport.removePropertyChangeListener(arg0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue(Object arg0) {
        Preconditions.checkArgument(clazz.isInstance(arg0));

        T old = this.supplier.get();
        writeThrough((T) arg0);
        fireValueChanged(old, this.supplier.get());
    }

    private void fireValueChanged(Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange("to", oldValue, newValue);
    }

    protected abstract void writeThrough(T arg0);

    @Override
    public ValidationResult validate() {
        return new ValidationResult();
    }

    public void refresh() {
        fireValueChanged(null, this.supplier.get());
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        refresh();
    }
}
