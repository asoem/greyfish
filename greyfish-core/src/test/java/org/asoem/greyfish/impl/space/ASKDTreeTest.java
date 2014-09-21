package org.asoem.greyfish.impl.space;

import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.asoem.greyfish.utils.space.Point2D;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ASKDTreeTest {

    @Test
    public void testConstruction() throws Exception {
        // given
        final ImmutableMap<Object, Point2D> map = ImmutableMap.<Object, Point2D>of(
                new Object(), ImmutablePoint2D.at(0.0, 1.0),
                new Object(), ImmutablePoint2D.at(0.3, 2.0));
        final int dimensions = 2;

        // when
        final ASKDTree<Object> tree =
                new ASKDTree<>(dimensions, map);

        // then
        assertThat(tree.size(), is(equalTo(2)));
    }
}