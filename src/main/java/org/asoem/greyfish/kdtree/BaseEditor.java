package org.asoem.sico.kdtree;

public abstract class BaseEditor<T> implements Editor<T> {

	final T val;
	public BaseEditor(T val) {
		this.val = val;
	}

	public static <T> Editor<T> newInserter(T val) {
		return new BaseEditor<T>(val) {

			public T edit(T current) throws KeyDuplicateException {
				if (current != null) {
					throw new KeyDuplicateException();
				}

				return this.val;
			}
		};
	}
	
	public static <T> Editor<T> newOptionalInserter(T val) {
		return new BaseEditor<T>(val) {

			public T edit(T current) throws KeyDuplicateException {
				return (current == null) ? this.val : current;
			}
		};
	}
	
	public static <T> Editor<T> newReplacer(T val) {
		return new BaseEditor<T>(val) {

			public T edit(T current) throws KeyDuplicateException {
				return this.val;
			}
		};
	}
}