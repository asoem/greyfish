package org.asoem.sico.lang;

import com.google.common.base.Predicate;



public class Property<T> {

	private T value;
	private Predicate<Object> predicate;

	public Property(T value) {
		this.value = value;
	}

	public Property(T value, Predicate<Object> predicate) {
		this.value = value;
		this.predicate = predicate;
	}

	public T get() {
		return value;
	}

	public void set(T value) {
		if (predicate == null || predicate.apply(value))
			this.value = value;
	}
}
