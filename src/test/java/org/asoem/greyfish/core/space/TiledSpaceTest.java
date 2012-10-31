package org.asoem.greyfish.core.space;

import com.google.inject.Guice;
import com.google.inject.Inject;
import javolution.lang.MathLib;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.ImmutableAgent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.space.*;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: christoph
 * Date: 08.10.11
 * Time: 16:42
 */
public class TiledSpaceTest {

    @Inject
    private Persister persister;

    public TiledSpaceTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testOfSize() throws Exception {
        // given
        int width = 2;
        int height = 3;

        // when
        Tiled tiledSpace = WalledTileSpace.ofSize(width, height);

        // then
        assertThat(tiledSpace.getTiles(), is(iterableWithSize(width * height)));
    }

    @Test
    public void testMaxTransitionWithNorthernWall() throws Exception {
        // given
        Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        Point2D destination = ImmutablePoint2D.at(0.0, -1.0);
        WalledTileSpace space = new WalledTileSpace(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.NORTH, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition, is(equalTo((Point2D) ImmutablePoint2D.at(0.0, 0.0))));
    }

    @Test
    public void testMaxTransitionWithEasternWall() throws Exception {
        // given
        Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        Point2D destination = ImmutablePoint2D.at(2.0, 0.0);
        WalledTileSpace space = new WalledTileSpace(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.EAST, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition, is(equalTo((Point2D) ImmutablePoint2D.at(Math.nextAfter(1.0, -Double.MIN_VALUE), 0.0))));
    }

    @Test
    public void testMaxTransitionWithSouthernWall() throws Exception {
        // given
        Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        Point2D destination = ImmutablePoint2D.at(0.0, 2.0);
        WalledTileSpace space = new WalledTileSpace(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.SOUTH, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition, is(equalTo((Point2D) ImmutablePoint2D.at(0.0, Math.nextAfter(1.0, -Double.MIN_VALUE)))));
    }

    @Test
    public void testMaxTransitionWithWesternWall() throws Exception {
        // given
        Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        Point2D destination = ImmutablePoint2D.at(-1.0, 0.0);
        WalledTileSpace space = new WalledTileSpace(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.WEST, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition, is(equalTo((Point2D) ImmutablePoint2D.at(0.0, 0.0))));
    }

    @Test
    public void testMaxTransitionWithAtEdge() throws Exception {
        // given
        Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        Point2D destination = ImmutablePoint2D.at(2.0, 2.0);
        WalledTileSpace space = new WalledTileSpace(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.SOUTH, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition, is(equalTo((Point2D) ImmutablePoint2D.at(Math.nextAfter(1.0, -Double.MIN_VALUE), Math.nextAfter(1.0, -Double.MIN_VALUE)))));
    }

    @Test
    public void testNoMaxTransition() throws Exception {
        // given
        Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        Point2D destination = ImmutablePoint2D.at(0.5, 0.5);
        WalledTileSpace space = new WalledTileSpace(1, 1);

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
        final WalledTileSpace<Agent> space = WalledTileSpace.<Agent>builder(1, 1)
                .addWall(0, 0, TileDirection.NORTH)
                .build();

        // when
        final TiledSpace<MovingProjectable2D, Tile> copy = Persisters.createCopy(space, WalledTileSpace.class, persister);

        // then
        assertThat(copy).isEqualTo(space);
    }
     */

    @Test
    public void testMaxTransitionWithEasternSpaceBorder() throws Exception {
        // given
        Point2D origin = ImmutablePoint2D.at(4.835470690262208, 9.9999999997506);
        Point2D destination = ImmutablePoint2D.at(4.9251314448644665, 10.044282607873617);
        WalledTileSpace space = new WalledTileSpace(10, 10);

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
        final WalledTileSpace<Agent> space = WalledTileSpace.ofSize(1, 1);
        Agent agent = ImmutableAgent.builder(Population.named("test")).build();
        agent.setMotion(ImmutableMotion2D.of(0, 1));
        space.insertObject(agent, 0, 0, 0);

        // when
        space.moveObject(agent);

        // then
        final MotionObject2D projection = agent.getProjection();
        assertThat(projection, is(notNullValue()));
        assertThat(projection.didCollide(), is(true));
    }


    @SuppressWarnings("ConstantConditions")
    @Test
    public void testNoCollision() throws Exception {
        // given
        final WalledTileSpace<Agent> space = WalledTileSpace.ofSize(1, 1);
        Agent agent = ImmutableAgent.builder(Population.named("test")).build();
        agent.setMotion(ImmutableMotion2D.of(0, 0.5));
        space.insertObject(agent, 0, 0, MathLib.HALF_PI / 2);

        // when
        space.moveObject(agent);

        // then
        final MotionObject2D projection = agent.getProjection();
        assertThat(projection, is(notNullValue()));
        assertThat(projection.didCollide(), is(false));
    }

    @Test
    public void testFindVisibleNeighbours() throws Exception {
        // given
        final WalledTileSpace<MovingProjectable2D> space = WalledTileSpace.builder(3, 1).addWall(0, 0, TileDirection.EAST).build();
        final MovingProjectable2D focal = new MovingProjectable2DImpl();
        final MovingProjectable2D neighbour1 = new MovingProjectable2DImpl();
        final MovingProjectable2D neighbour2 = new MovingProjectable2DImpl();

        space.insertObject(focal, 1.5, 0.5, 0);
        space.insertObject(neighbour1, 0.5, 0.5, 0);
        space.insertObject(neighbour2, 2.5, 0.5, 0);

        // when
        final Iterable<MovingProjectable2D> visibleNeighbours = space.getVisibleNeighbours(focal, 2.0);

        // then
        assertThat(visibleNeighbours, contains(neighbour2));
    }
}
