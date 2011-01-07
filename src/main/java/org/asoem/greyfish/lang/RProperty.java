package org.asoem.greyfish.lang;

public class RProperty<T> {

	private T value;

	public T getValue() {
		return value;
	}

	protected void setValue(T value) {
		this.value = value;
	}
}
