package org.asoem.sico.lang;


public abstract class RadioProperty<T> extends Property<T> {

	public RadioProperty(T value) {
		super(value);
	}

	abstract T[] values();
}
