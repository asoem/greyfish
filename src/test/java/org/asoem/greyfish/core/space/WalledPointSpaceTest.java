package org.asoem.greyfish.core.space;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.space.*;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

/**
 * User: christoph
 * Date: 08.10.11
 * Time: 16:42
 */
public class WalledPointSpaceTest {

    @Inject
    private Persister persister;

    public WalledPointSpaceTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testOfSize() throws Exception {
        // given
        final int width = 2;
        final int height = 3;

        // when
        final WalledPointSpace<Object> tiledSpace = WalledPointSpace.ofSize(width, height);

        // then
        assertThat(tiledSpace.getTiles(), is(Matchers.<WalledTile>iterableWithSize(width * height)));
    }

    @Test
    public void testMaxTransitionWithNorthernWall() throws Exception {
        // given
        final Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        final Point2D destination = ImmutablePoint2D.at(0.0, -1.0);
        final WalledPointSpace<Object> space = new WalledPointSpace<Object>(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.NORTH, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition, is(equalTo((Point2D) ImmutablePoint2D.at(0.0, 0.0))));
    }

    @Test
    public void testMaxTransitionWithEasternWall() throws Exception {
        // given
        final Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        final Point2D destination = ImmutablePoint2D.at(2.0, 0.0);
        final WalledPointSpace<Object> space = new WalledPointSpace<Object>(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.EAST, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition, is(equalTo((Point2D) ImmutablePoint2D.at(Math.nextAfter(1.0, -Double.MIN_VALUE), 0.0))));
    }

    @Test
    public void testMaxTransitionWithSouthernWall() throws Exception {
        // given
        final Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        final Point2D destination = ImmutablePoint2D.at(0.0, 2.0);
        final WalledPointSpace<Object> space = new WalledPointSpace<Object>(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.SOUTH, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition, is(equalTo((Point2D) ImmutablePoint2D.at(0.0, Math.nextAfter(1.0, -Double.MIN_VALUE)))));
    }

    @Test
    public void testMaxTransitionWithWesternWall() throws Exception {
        // given
        final Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        final Point2D destination = ImmutablePoint2D.at(-1.0, 0.0);
        final WalledPointSpace<Object> space = new WalledPointSpace<Object>(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.WEST, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition, is(equalTo((Point2D) ImmutablePoint2D.at(0.0, 0.0))));
    }

    @Test
    public void testMaxTransitionWithAtEdge() throws Exception {
        // given
        final Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        final Point2D destination = ImmutablePoint2D.at(2.0, 2.0);
        final WalledPointSpace<Object> space = new WalledPointSpace<Object>(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.SOUTH, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition, is(equalTo((Point2D) ImmutablePoint2D.at(Math.nextAfter(1.0, -Double.MIN_VALUE), Math.nextAfter(1.0, -Double.MIN_VALUE)))));
    }

    @Test
    public void testNoMaxTransition() throws Exception {
        // given
        final Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        final Point2D destination = ImmutablePoint2D.at(0.5, 0.5);
        final WalledPointSpace<Agent> space = new WalledPointSpace<Agent>(1, 1);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition, is(equalTo(destination)));
    }

    /*
    @SuppressWarnings("unchecked")
    @Test
    public void testBasicPersistence() throws Exception {
        // given
        final WalledPointSpace<Agent> space = WalledPointSpace.<Agent>builder(1, 1)
                .addWall(0, 0, TileDirection.NORTH)
                .build();

        // when
        final TiledSpace<MovingProjectable2D, Tile> copy = Persisters.createCopy(space, WalledPointSpace.class, persister);

        // then
        assertThat(copy).isEqualTo(space);
    }
     */

    @Test
    public void testMaxTransitionWithEasternSpaceBorder() throws Exception {
        // given
        final Point2D origin = ImmutablePoint2D.at(4.835470690262208, 9.9999999997506);
        final Point2D destination = ImmutablePoint2D.at(4.9251314448644665, 10.044282607873617);
        final WalledPointSpace<Object> space = new WalledPointSpace<Object>(10, 10);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        final Point2D expected = ImmutablePoint2D.at(4.835470690767176, Math.nextAfter(10.0, -Double.MIN_VALUE));
        assertThat(maxTransition, is(equalTo(expected)));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testCollision() throws Exception {
        // given
        final WalledPointSpace<Object> space = WalledPointSpace.ofSize(1, 1);
        final Object agent = mock(Object.class);
        space.insertObject(agent, ImmutablePoint2D.at(0, 0));

        // when
        space.moveObject(agent, ImmutableMotion2D.of(0, 1));

        // then
        final Point2D projection = space.getProjection(agent);
        assertThat(projection, is(equalTo((Point2D) ImmutablePoint2D.at(0, 0))));
    }


    @SuppressWarnings("ConstantConditions")
    @Test
    public void testNoCollision() throws Exception {
        // given
        final WalledPointSpace<Object> space = WalledPointSpace.ofSize(1, 1);
        final Object agent = mock(Object.class);
        space.insertObject(agent, ImmutablePoint2D.at(0, 0));

        // when
        space.moveObject(agent, ImmutableMotion2D.of(Math.PI / 4, 1));

        // then
        final Point2D projection = space.getProjection(agent);
        assertThat(projection, is(equalTo((Point2D) ImmutablePoint2D.at(0, 0))));
    }

    @Test
    public void testFindVisibleNeighbours() throws Exception {
        // given
        final WalledPointSpace<MovingProjectable2D> space = WalledPointSpace.<MovingProjectable2D>builder(3, 1).addWall(0, 0, TileDirection.EAST).build();
        final MovingProjectable2D focal = new MovingProjectable2DImpl();
        final MovingProjectable2D neighbour1 = new MovingProjectable2DImpl();
        final MovingProjectable2D neighbour2 = new MovingProjectable2DImpl();

        space.insertObject(focal, ImmutablePoint2D.at(1.5, 0.5));
        space.insertObject(neighbour1, ImmutablePoint2D.at(0.5, 0.5));
        space.insertObject(neighbour2, ImmutablePoint2D.at(2.5, 0.5));

        // when
        final Iterable<MovingProjectable2D> visibleNeighbours = space.getVisibleNeighbours(focal, 2.0);

        // then
        assertThat(visibleNeighbours, contains(neighbour2));
    }
}
