package org.asoem.greyfish.utils.base;

import com.google.common.collect.Ordering;

public enum CompareOperator {

	Equal("equal to") {
		
		@Override
		public <T> boolean apply(Comparable<T> a, Comparable<T> b) {
			return compare(a, b) == 0;
		}
	},
	NotEqual("not equal to") {
		@Override
		public <T> boolean apply(Comparable<T> a, Comparable<T> b) {
			return compare(a, b) != 0;
		}
	},
	LessThan("less than") {
		@Override
		public <T> boolean apply(Comparable<T> a, Comparable<T> b) {
			return compare(a, b) < 0;
		}
	},
    LessThanOrEquals("less than or equal") {
		@Override
		public <T> boolean apply(Comparable<T> a, Comparable<T> b) {
			return compare(a, b) <= 0;
		}
	},
    GreaterThan("greater than") {
		@Override
		public <T> boolean apply(Comparable<T> a, Comparable<T> b) {
			return compare(a, b) > 0;
		}
	},
    GreaterThanOrEquals("greater than or equal") {
		@Override
		public <T> boolean apply(Comparable<T> a, Comparable<T> b) {
			return compare(a, b) >= 0;
		}
	};
    
    private static <T> int compare(Comparable<T> a, Comparable<T> b) {
        return Ordering.natural().compare(a, b);
    }

	private final String name;

	private CompareOperator(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public abstract <T> boolean apply(Comparable<T> a, Comparable<T> b);
}