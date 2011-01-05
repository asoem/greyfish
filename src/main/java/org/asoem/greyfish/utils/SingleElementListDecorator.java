package org.asoem.sico.utils;

import java.util.AbstractList;

import com.google.common.base.Preconditions;

public class SingleElementListDecorator<E> extends AbstractList<E> {

	private E element;
	
	public SingleElementListDecorator(E element) {
		this.element = element;
	}
	
	@Override
	public E get(int index) {
		Preconditions.checkPositionIndex(index, 1);
		return element;
	}

	@Override
	public int size() {
		return 1;
	}

	public E set(int index, E element) {
		Preconditions.checkPositionIndex(index, 1);
		E ret = this.element;
		this.element = element;
		return ret;
	};
}
