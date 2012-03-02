package org.asoem.greyfish.core.space;

import com.google.inject.Inject;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.asoem.greyfish.utils.space.Geometry2D;
import org.asoem.greyfish.utils.space.ImmutableLocation2D;
import org.asoem.greyfish.utils.space.Location2D;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 08.10.11
 * Time: 16:42
 */
public class TiledSpaceTest {

    @Inject
    private Persister persister;

    public TiledSpaceTest() {
        CoreInjectorHolder.coreInjector().injectMembers(this);
    }

    @Test
    public void testOfSize() throws Exception {
        // given
        int width = 2;
        int height = 3;

        // when
        Tiled tiledSpace = TiledSpace.ofSize(width, height);

        // then
        assertThat(tiledSpace.getTiles()).hasSize(width * height);
    }

    @Test
    public void testMaxTransitionWithNorthernWall() throws Exception {
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
    public void testMaxTransitionWithEasternWall() throws Exception {
        // given
        Location2D origin = ImmutableLocation2D.at(0.0, 0.0);
        Location2D destination = ImmutableLocation2D.at(2.0, 0.0);
        TiledSpace space = new TiledSpace(3, 3);
        space.getTileAt(0, 0).setBorder(TileDirection.EAST, true);

        // when
        final Location2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(ImmutableLocation2D.at(1.0 - Geometry2D.CLOSEST_TO_ZERO_RIGHT, 0.0));
    }

    @Test
    public void testMaxTransitionWithSouthernWall() throws Exception {
        // given
        Location2D origin = ImmutableLocation2D.at(0.0, 0.0);
        Location2D destination = ImmutableLocation2D.at(0.0, 2.0);
        TiledSpace space = new TiledSpace(3, 3);
        space.getTileAt(0, 0).setBorder(TileDirection.SOUTH, true);

        // when
        final Location2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(ImmutableLocation2D.at(0.0, 1.0 - Geometry2D.CLOSEST_TO_ZERO_RIGHT));
    }

    @Test
    public void testMaxTransitionWithWesternWall() throws Exception {
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
    public void testMaxTransitionWithAtEdge() throws Exception {
        // given
        Location2D origin = ImmutableLocation2D.at(0.0, 0.0);
        Location2D destination = ImmutableLocation2D.at(2.0, 2.0);
        TiledSpace space = new TiledSpace(3, 3);
        space.getTileAt(0, 0).setBorder(TileDirection.SOUTH, true);

        // when
        final Location2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(ImmutableLocation2D.at(1.0 - Geometry2D.CLOSEST_TO_ZERO_RIGHT, 1.0 - Geometry2D.CLOSEST_TO_ZERO_RIGHT));
    }

    @Test
    public void testNoMaxTransition() throws Exception {
        // given
        Location2D origin = ImmutableLocation2D.at(0.0, 0.0);
        Location2D destination = ImmutableLocation2D.at(0.5, 0.5);
        TiledSpace space = new TiledSpace(1, 1);

        // when
        final Location2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(destination);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBasicPersistence() throws Exception {
        // given
        final TiledSpace<Agent> space = TiledSpace.<Agent>builder(1, 1)
                .addBorder(0, 0, TileDirection.NORTH)
                .build();

        // when
        final TiledSpace<Agent> copy = Persisters.createCopy(space, TiledSpace.class, persister);

        // then
        assertThat(copy).isEqualTo(space);
    }

    @Test
    public void testMaxTransitionWithEasternSpaceBorder() throws Exception {
        // given
        Location2D origin = ImmutableLocation2D.at(9.9999, 0.0);
        Location2D destination = ImmutableLocation2D.at(10.1, 0.0);
        TiledSpace space = new TiledSpace(10, 1);

        // when
        final Location2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(ImmutableLocation2D.at(1.0 - Geometry2D.CLOSEST_TO_ZERO_RIGHT, 0.0));
    }
}
