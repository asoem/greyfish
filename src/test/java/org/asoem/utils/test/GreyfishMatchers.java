package org.asoem.utils.test;

import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * User: christoph
 * Date: 11.12.12
 * Time: 11:59
 */
public class GreyfishMatchers {

    private GreyfishMatchers() {}

    public static <T> Matcher<T> isA(final TypeToken<T> typeToken) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object item) {
                return item != null && typeToken.isAssignableFrom(item.getClass());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an instance of ").appendValue(typeToken.getType());
            }
        };
    }

    public static <I,O> TransformingTypeSafeMatcher<I, O> has(String description, Function<I, O> transformation, Matcher<? super O> transformMatcher) {
        return new TransformingTypeSafeMatcher<I, O>(description, transformation, transformMatcher);
    }
}
