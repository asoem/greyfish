package org.asoem.sico.lang;

public class WProperty<T> {

	private T value;

	protected T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
}
