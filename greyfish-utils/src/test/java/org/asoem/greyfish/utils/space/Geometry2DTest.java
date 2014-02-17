package org.asoem.greyfish.utils.space;

import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.Matchers.*;


public class Geometry2DTest {
    @Test
    public void testNoIntersectionWithAdjacentFP() throws Exception {

        // when
        final ImmutablePoint2D intersection = Geometry2D.intersection(1.0, 0.0, 1.0, Math.nextAfter(1.0, -Double.MIN_VALUE), 0.0, 1.0, 1.0, 1.0);

        // then
        MatcherAssert.assertThat(intersection, is(nullValue()));
    }

    @Test
    public void testIntersection() throws Exception {

        // when
        final ImmutablePoint2D intersection = Geometry2D.intersection(9.975393084761107, 0.5, 10.066768813786554, 0.5, Math.nextAfter(10.0, -Double.MIN_VALUE), 0.0, Math.nextAfter(10.0, -Double.MIN_VALUE), 1.0);

        // then
        MatcherAssert.assertThat(intersection, is(equalTo(ImmutablePoint2D.at(Math.nextAfter(10.0, -Double.MIN_VALUE), 0.5))));
    }
}
