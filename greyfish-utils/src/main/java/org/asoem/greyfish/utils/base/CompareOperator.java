/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.utils.base;

import com.google.common.collect.Ordering;

public enum CompareOperator {

	EQUAL("equal to") {
		
		@Override
		public <T> boolean apply(final Comparable<T> a, final Comparable<T> b) {
			return compare(a, b) == 0;
		}
	},
	NOT_EQUAL("not equal to") {
		@Override
		public <T> boolean apply(final Comparable<T> a, final Comparable<T> b) {
			return compare(a, b) != 0;
		}
	},
	LESS_THAN("less than") {
		@Override
		public <T> boolean apply(final Comparable<T> a, final Comparable<T> b) {
			return compare(a, b) < 0;
		}
	},
    LESS_THAN_OR_EQUAL("less than or equal") {
		@Override
		public <T> boolean apply(final Comparable<T> a, final Comparable<T> b) {
			return compare(a, b) <= 0;
		}
	},
    GREATER_THAN("greater than") {
		@Override
		public <T> boolean apply(final Comparable<T> a, final Comparable<T> b) {
			return compare(a, b) > 0;
		}
	},
    GREATER_THAN_OR_EQUAL("greater than or equal") {
		@Override
		public <T> boolean apply(final Comparable<T> a, final Comparable<T> b) {
			return compare(a, b) >= 0;
		}
	};
    
    private static <T> int compare(final Comparable<T> a, final Comparable<T> b) {
        return Ordering.natural().compare(a, b);
    }

	private final String name;

	private CompareOperator(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public abstract <T> boolean apply(Comparable<T> a, Comparable<T> b);
}