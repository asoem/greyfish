package org.asoem.greyfish.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.Validatable;
import com.jgoodies.validation.ValidationResult;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;

import static com.google.common.base.Preconditions.checkNotNull;


public abstract class ValueAdaptor<T> implements ValueModel, Validatable, PropertyChangeListener, Supplier<T>, WriteProtectable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueAdaptor.class);

    public final String name;
    public final Class<T> clazz;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private Supplier<Boolean> writeProtection = Suppliers.ofInstance(Boolean.FALSE);

    public ValueAdaptor(String name, Class<T> clazz) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(clazz);

        this.name = name;
        this.clazz = clazz;
    }

    @Override
    public void addValueChangeListener(PropertyChangeListener arg0) {
        propertyChangeSupport.addPropertyChangeListener(arg0);
    }

    @Override
    public void removeValueChangeListener(PropertyChangeListener arg0) {
        propertyChangeSupport.removePropertyChangeListener(arg0);
    }

    public String getName() {
        return name;
    }


    @Override
    public T getValue() {
        return get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue(Object arg0) {
        Preconditions.checkArgument(clazz.isInstance(arg0));

        if (isWriteProtected())
            return;

        T old = get();
        try {
            set((T) arg0);
        }
        catch (Exception e) {
            LoggerFactory.getLogger(ValueAdaptor.class).debug("Could not set value", e);
            // TODO: change Adaptors state
            return;
        }

        fireValueChanged(old, get());
    }

    private void fireValueChanged(Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange("value", oldValue, newValue);
    }

    protected abstract void set(T arg0);

    @Override
    public ValidationResult validate() {
        return new ValidationResult();
    }

    public void refresh() {
        fireValueChanged(null, get());
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        refresh();
    }

    public static <T> ValueAdaptor<T> forField(String name, Class<T> clazz, final Object o, final String fieldName) {
        checkNotNull(name);
        checkNotNull(clazz);
        checkNotNull(o);
        checkNotNull(fieldName);
        try {
            final Field field = o.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return new ValueAdaptor<T>(name, clazz) {
                @Override
                protected void set(T arg0) {
                    try {
                        field.set(o, checkNotNull(arg0));
                    } catch (IllegalAccessException e) {
                        LOGGER.error("Cannot set value of field " + fieldName + " for object " + o, e);
                    }
                }

                @Override
                public T get() {
                    try {
                        return clazz.cast(field.get(o));
                    } catch (IllegalAccessException e) {
                        LOGGER.error("Cannot get value of field " + fieldName + " for object " + o, e);
                    }
                    return null;
                }
            };
        } catch (NoSuchFieldException e) {
            LOGGER.error("Could not find field " + fieldName + " for object " + o, e);
        }
        return null;
    }

    @Override
    public void setWriteProtection(Supplier<Boolean> writeProtection) {
        this.writeProtection = writeProtection;
    }

    @Override
    public boolean isWriteProtected() {
        return writeProtection.get();
    }
}
