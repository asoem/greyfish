package org.asoem.greyfish.core.utils;

import com.google.common.collect.Ordering;
import org.simpleframework.xml.Root;


@SuppressWarnings( "unused" )
@Root
public enum Comparator {

	EQ("is equal") {
		
		@Override
		public <T> boolean compare(Comparable<T> a, Comparable<T> b) {
			return cmp(a, b) == 0;
		}
	},
	NEQ("is not equal") {
		@Override
		public <T> boolean compare(Comparable<T> a, Comparable<T> b) {
			return cmp(a, b) != 0;
		}
	},
	LT("is less than") {
		@Override
		public <T> boolean compare(Comparable<T> a, Comparable<T> b) {
			return cmp(a, b) < 0;
		}
	},
	LEQ("is less than or equal") {
		@Override
		public <T> boolean compare(Comparable<T> a, Comparable<T> b) {
			return cmp(a, b) <= 0;
		}
	},
	GT("is greater than") {
		@Override
		public <T> boolean compare(Comparable<T> a, Comparable<T> b) {
			return cmp(a, b) > 0;
		}
	},
	GEQ("is greater than or equal") {
		@Override
		public <T> boolean compare(Comparable<T> a, Comparable<T> b) {
			return cmp(a, b) >= 0;
		}
	};
    
    private static <T> int cmp(Comparable<T> a, Comparable<T> b) {
        return Ordering.natural().compare(a, b);
    }

	private final String name;

	private Comparator(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public abstract <T> boolean compare(Comparable<T> a, Comparable<T> b);
}