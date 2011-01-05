package org.asoem.sico.core.conditions;

import org.simpleframework.xml.Root;


@SuppressWarnings( {"unchecked" , "rawtypes"})
@Root
public enum Comparator {

	EQ("is equal") {
		
		@Override
		public boolean compare(Comparable a, Comparable b) {
			return a.compareTo(b) == 0;
		}
	},
	NEQ("is not equal") {
		@Override
		public boolean compare(Comparable a, Comparable b) {
			return a.compareTo(b) != 0;
		}
	},
	LT("is less than") {
		@Override
		public boolean compare(Comparable a, Comparable b) {
			return a.compareTo(b) < 0;
		}
	},
	LEQ("is less than or equal") {
		@Override
		public boolean compare(Comparable a, Comparable b) {
			return a.compareTo(b) <= 0;
		}
	},
	GT("is greater than") {
		@Override
		public boolean compare(Comparable a, Comparable b) {
			return a.compareTo(b) > 0;
		}
	},
	GEQ("is greater than or equal") {
		@Override
		public boolean compare(Comparable a, Comparable b) {
			return a.compareTo(b) >= 0;
		}
	};

	private final String name;

	private Comparator(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public abstract boolean compare(Comparable a, Comparable b);
}