package org.asoem.greyfish.core.space;

import org.asoem.greyfish.utils.space.ImmutableLocation2D;
import org.asoem.greyfish.utils.space.Location2D;
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
    public void testmaxTransitionWithNorthernWall() throws Exception {
        // given
        Location2D origin = ImmutableLocation2D.at(0.0, 0.0);
        Location2D destination = ImmutableLocation2D.at(0.0, -1.0);
        TiledSpace space = new TiledSpace(3, 3);
        space.getTileAt(0, 0).setBorder(TileDirection.NORTH, true);
        
        // when
        final Location2D maxTransition = space.maxTransition(origin, destination);
        
        // then
        assertThat(maxTransition).isEqualTo(ImmutableLocation2D.at(0.0, 0.0));
    }

    @Test
    public void testmaxTransitionWithEasternWall() throws Exception {
        // given
        Location2D origin = ImmutableLocation2D.at(0.0, 0.0);
        Location2D destination = ImmutableLocation2D.at(2.0, 0.0);
        TiledSpace space = new TiledSpace(3, 3);
        space.getTileAt(0, 0).setBorder(TileDirection.EAST, true);

        // when
        final Location2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(ImmutableLocation2D.at(1.0, 0.0));
    }

    @Test
    public void testmaxTransitionWithSouthernWall() throws Exception {
        // given
        Location2D origin = ImmutableLocation2D.at(0.0, 0.0);
        Location2D destination = ImmutableLocation2D.at(0.0, 2.0);
        TiledSpace space = new TiledSpace(3, 3);
        space.getTileAt(0, 0).setBorder(TileDirection.SOUTH, true);

        // when
        final Location2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(ImmutableLocation2D.at(0.0, 1.0));
    }

    @Test
    public void testmaxTransitionWithWesternWall() throws Exception {
        // given
        Location2D origin = ImmutableLocation2D.at(0.0, 0.0);
        Location2D destination = ImmutableLocation2D.at(-1.0, 0.0);
        TiledSpace space = new TiledSpace(3, 3);
        space.getTileAt(0, 0).setBorder(TileDirection.WEST, true);

        // when
        final Location2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(ImmutableLocation2D.at(0.0, 0.0));
    }

    @Test
    public void testmaxTransitionWithAtEdge() throws Exception {
        // given
        Location2D origin = ImmutableLocation2D.at(0.0, 0.0);
        Location2D destination = ImmutableLocation2D.at(2.0, 2.0);
        TiledSpace space = new TiledSpace(3, 3);
        space.getTileAt(0, 0).setBorder(TileDirection.SOUTH, true);

        // when
        final Location2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(ImmutableLocation2D.at(1.0, 1.0));
    }

    @Test
    public void testNomaxTransition() throws Exception {
        // given
        Location2D origin = ImmutableLocation2D.at(0.0, 0.0);
        Location2D destination = ImmutableLocation2D.at(0.5, 0.5);
        TiledSpace space = new TiledSpace(1, 1);

        // when
        final Location2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(destination);
    }
}
