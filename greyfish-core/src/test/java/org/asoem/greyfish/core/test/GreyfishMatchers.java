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

package org.asoem.greyfish.core.test;

import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;


public class GreyfishMatchers {

    private GreyfishMatchers() {}

    public static <T> Matcher<T> isA(final TypeToken<T> typeToken) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(final Object item) {
                return item != null && typeToken.isAssignableFrom(item.getClass());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("an instance of ").appendValue(typeToken.getType());
            }
        };
    }

    public static <I, O> TransformingTypeSafeMatcher<I, O> has(final String description, final Function<I, O> transformation, final Matcher<? super O> transformMatcher) {
        return new TransformingTypeSafeMatcher<I, O>(description, transformation, transformMatcher);
    }
}
