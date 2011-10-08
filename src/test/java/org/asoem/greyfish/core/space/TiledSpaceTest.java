package org.asoem.greyfish.core.space;

import org.junit.Test;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;
import static org.junit.Assert.assertEquals;

/**
 * User: christoph
 * Date: 08.10.11
 * Time: 16:42
 */
public class TiledSpaceTest {

    @Test
    public void testOfSize() throws Exception {
        // given
        int width = 2;
        int height = 3;

        // when
        TiledSpace tiledSpace = TiledSpace.ofSize(width, height);

        // then
        assertEquals(size(filter(tiledSpace, instanceOf(TileLocation.class))), width * height);
    }
}
