package org.asoem.sico.utils;

@SuppressWarnings("unchecked")
public class Range<T extends Comparable> {

	private T from = null;
	private T to = null;

	public Range( T start, T end ){
		this.from = start;
		this.to = end;
	}

	public void setFrom(T from) {
		this.from = from;
	}

	public T getFrom() {
		return from;
	}

	public void setTo(T to) {
		this.to = to;
	}

	public T getTo() {
		return to;
	}

	public boolean contains( T value ){
		return
		from.compareTo( value ) <= 0 &&
		to.compareTo( value ) >= 0;
	}
}