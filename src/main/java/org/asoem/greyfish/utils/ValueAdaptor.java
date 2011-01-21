package org.asoem.greyfish.utils;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.google.common.base.Preconditions;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.Validatable;
import com.jgoodies.validation.ValidationResult;


public abstract class ValueAdaptor<T> implements ValueModel, Validatable {

	public final String name;
	public final Class<T> clazz;
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	private T o;
	
	public ValueAdaptor(String name, Class<T> clazz, T o) {
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(clazz);
		
		this.name = name;
		this.clazz = clazz;
		this.o = o;
	}

	@Override
	public void addValueChangeListener(PropertyChangeListener arg0) {
		propertyChangeSupport.addPropertyChangeListener(arg0);
	}

	@Override
	public T getValue() {
		return o;
	}

	@Override
	public void removeValueChangeListener(PropertyChangeListener arg0) {
		propertyChangeSupport.removePropertyChangeListener(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValue(Object arg0) {
		Preconditions.checkArgument(clazz.isInstance(arg0));
		
		T old = this.o;
		this.o = (T) arg0;
		writeThrough((T) arg0);
		fireValueChanged(old, this.o);
	}
	
	private void fireValueChanged(Object oldValue, Object newValue) {
		propertyChangeSupport.firePropertyChange("to", oldValue, newValue);
	}

	protected abstract void writeThrough(T arg0);
	
	@Override
	public ValidationResult validate() {
		return new ValidationResult();
	}
}
