package org.asoem.greyfish.utils.space;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


public class ImmutableLocation2DTest {

    @Test
    public void testEqualsNegativeZero() throws Exception {
        final ImmutablePoint2D negZero = ImmutablePoint2D.at(-0.0, -0.0);
        final ImmutablePoint2D posZero = ImmutablePoint2D.at(0.0, 0.0);

        MatcherAssert.assertThat(negZero, is(equalTo(posZero)));
    }
}
