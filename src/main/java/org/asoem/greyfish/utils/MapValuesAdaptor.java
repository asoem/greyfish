package org.asoem.greyfish.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * User: christoph
 * Date: 14.02.11
 * Time: 10:32
 */
public abstract class MapValuesAdaptor<E> implements PropertyChangeListener {

    private final String name;
    private final Class<E> clazz;
//    private final IndirectListModel<E> listModel = new IndirectListModel<E>();
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public MapValuesAdaptor(String name, Class<E> clazz) {
        this.name = name;
        this.clazz = clazz;
        refresh();
    }

    public abstract Object[] keys();
    public abstract E[] get();
    public abstract void set(E[] list);

    public String getName() {
        return name;
    }

    public Class<E> getClazz() {
        return clazz;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("value"))
            refresh();
    }

    private void refresh() {
        fireValueChanged(null, get());
    }

    public void addValueChangeListener(PropertyChangeListener arg0) {
        propertyChangeSupport.addPropertyChangeListener("value", arg0);
    }

    public void removeValueChangeListener(PropertyChangeListener arg0) {
        propertyChangeSupport.removePropertyChangeListener("value", arg0);
    }

    private void fireValueChanged(Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange("value", oldValue, newValue);
    }
}
