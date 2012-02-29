package org.asoem.greyfish.utils.space;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 10:42
 */
public class ImmutableLocation2DTest {

    @Test
    public void testEqualsNegativeZero() throws Exception {
        final ImmutableLocation2D negZero = ImmutableLocation2D.at(-0.0, 1.0);
        final ImmutableLocation2D posZero = ImmutableLocation2D.at(0.0, 1.0);

        assertThat(negZero).isEqualTo(posZero);
        assertThat(negZero.hashCode()).isEqualTo(posZero.hashCode());
    }
}
