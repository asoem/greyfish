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

    public TransformingTypeSafeMatcher(String description, Function<I, O> transformation, Matcher<? super O> transformMatcher) {
        super("has " + description);
        this.transformation = transformation;
        this.transformMatcher = transformMatcher;
    }

    @Override
    protected boolean matchesSafely(I item) {
        return transformMatcher.matches(transformation.apply(item));
    }

    @Override
    protected void describeMismatchSafely(I item, Description mismatchDescription) {
        mismatchDescription.appendText("failed: ");
        transformMatcher.describeMismatch(transformation.apply(item), mismatchDescription);
    }

}
