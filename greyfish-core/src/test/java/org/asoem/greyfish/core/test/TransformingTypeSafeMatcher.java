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
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
* User: christoph
* Date: 02.11.12
* Time: 14:56
*/
public class TransformingTypeSafeMatcher<I, O> extends CustomTypeSafeMatcher<I> {

    private final Function<I, O> transformation;
    private final Matcher<? super O> transformMatcher;

    public TransformingTypeSafeMatcher(final String description, final Function<I, O> transformation, final Matcher<? super O> transformMatcher) {
        super("has " + description);
        this.transformation = transformation;
        this.transformMatcher = transformMatcher;
    }

    @Override
    protected boolean matchesSafely(final I item) {
        return transformMatcher.matches(transformation.apply(item));
    }

    @Override
    protected void describeMismatchSafely(final I item, final Description mismatchDescription) {
        mismatchDescription.appendText("failed: ");
        transformMatcher.describeMismatch(transformation.apply(item), mismatchDescription);
    }

}
