package org.asoem.greyfish.core.space;

import org.asoem.greyfish.utils.space.ImmutableLocation2D;
import org.asoem.greyfish.utils.space.ImmutableObject2D;
import org.asoem.greyfish.utils.space.Location2D;
import org.asoem.greyfish.utils.space.Object2D;
import org.junit.Test;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;
import static org.fest.assertions.Assertions.assertThat;
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

    @Test
    public void testCollision() throws Exception {
        // given
        Object2D object1 = ImmutableObject2D.of(0.0, 0.0, 0.0);
        Object2D object2 = ImmutableObject2D.of(2.0, 2.0, 0.0);
        TiledSpace space = new TiledSpace(3, 3);
        space.getTileAt(0, 0).setBorder(TileDirection.SOUTH, true);
        
        // when
        final Location2D collision = space.collision(object1, object2);
        
        // then
        assertThat(collision).isEqualTo(ImmutableLocation2D.at(1.0, 1.0));
    }
}
