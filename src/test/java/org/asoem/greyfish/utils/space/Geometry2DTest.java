package org.asoem.greyfish.utils.space;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 02.03.12
 * Time: 18:18
 */
public class Geometry2DTest {
    @Test
    public void testNoIntersectionWithAdjacentFP() throws Exception {

        // when
        final ImmutableLocation2D intersection = Geometry2D.intersection(1.0, 0.0, 1.0, Math.nextAfter(1.0, -Double.MIN_VALUE), 0.0, 1.0, 1.0, 1.0);

        // then
        assertThat(intersection).isNull();
    }

    @Test
    public void testIntersection() throws Exception {

        // when
        final ImmutableLocation2D intersection = Geometry2D.intersection(9.975393084761107, 0.5, 10.066768813786554, 0.5, Math.nextAfter(10.0, -Double.MIN_VALUE), 0.0, Math.nextAfter(10.0, -Double.MIN_VALUE), 1.0);

        // then
        assertThat(intersection).isEqualTo(ImmutableLocation2D.at(Math.nextAfter(10.0, -Double.MIN_VALUE), 0.5));
    }
}
