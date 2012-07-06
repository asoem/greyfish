package org.asoem.greyfish.core.space;

import com.google.inject.Guice;
import com.google.inject.Inject;
import javolution.lang.MathLib;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.asoem.greyfish.utils.space.*;
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
        Guice.createInjector(new CoreModule()).injectMembers(this);
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
        Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        Point2D destination = ImmutablePoint2D.at(0.0, -1.0);
        TiledSpace space = new TiledSpace(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.NORTH, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(ImmutablePoint2D.at(0.0, 0.0));
    }

    @Test
    public void testMaxTransitionWithEasternWall() throws Exception {
        // given
        Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        Point2D destination = ImmutablePoint2D.at(2.0, 0.0);
        TiledSpace space = new TiledSpace(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.EAST, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(ImmutablePoint2D.at(Math.nextAfter(1.0, -Double.MIN_VALUE), 0.0));
    }

    @Test
    public void testMaxTransitionWithSouthernWall() throws Exception {
        // given
        Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        Point2D destination = ImmutablePoint2D.at(0.0, 2.0);
        TiledSpace space = new TiledSpace(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.SOUTH, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(ImmutablePoint2D.at(0.0, Math.nextAfter(1.0, -Double.MIN_VALUE)));
    }

    @Test
    public void testMaxTransitionWithWesternWall() throws Exception {
        // given
        Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        Point2D destination = ImmutablePoint2D.at(-1.0, 0.0);
        TiledSpace space = new TiledSpace(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.WEST, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(ImmutablePoint2D.at(0.0, 0.0));
    }

    @Test
    public void testMaxTransitionWithAtEdge() throws Exception {
        // given
        Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        Point2D destination = ImmutablePoint2D.at(2.0, 2.0);
        TiledSpace space = new TiledSpace(3, 3);
        space.getTileAt(0, 0).setWall(TileDirection.SOUTH, true);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(ImmutablePoint2D.at(Math.nextAfter(1.0, -Double.MIN_VALUE), Math.nextAfter(1.0, -Double.MIN_VALUE)));
    }

    @Test
    public void testNoMaxTransition() throws Exception {
        // given
        Point2D origin = ImmutablePoint2D.at(0.0, 0.0);
        Point2D destination = ImmutablePoint2D.at(0.5, 0.5);
        TiledSpace space = new TiledSpace(1, 1);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        assertThat(maxTransition).isEqualTo(destination);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBasicPersistence() throws Exception {
        // given
        final TiledSpace<Agent> space = TiledSpace.<Agent>builder(1, 1)
                .addWall(0, 0, TileDirection.NORTH)
                .build();

        // when
        final TiledSpace<Agent> copy = Persisters.createCopy(space, TiledSpace.class, persister);

        // then
        assertThat(copy).isEqualTo(space);
    }

    @Test
    public void testMaxTransitionWithEasternSpaceBorder() throws Exception {
        // given
        Point2D origin = ImmutablePoint2D.at(4.835470690262208, 9.9999999997506);
        Point2D destination = ImmutablePoint2D.at(4.9251314448644665, 10.044282607873617);
        TiledSpace space = new TiledSpace(10, 10);

        // when
        final Point2D maxTransition = space.maxTransition(origin, destination);

        // then
        final ImmutablePoint2D expected = ImmutablePoint2D.at(4.835470690767176, Math.nextAfter(10.0, -Double.MIN_VALUE));
        assertThat(maxTransition).isEqualTo(expected);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testCollision() throws Exception {
        // given
        final TiledSpace<Agent> space = TiledSpace.ofSize(1,1);
        Agent agent = ImmutableAgent.of(Population.named("test")).build();
        agent.setMotion(ImmutableMotion2D.of(0, 1));
        space.insertObject(agent, 0, 0, 0);

        // when
        space.moveObject(agent);

        // then
        final MotionObject2D projection = agent.getProjection();
        assertThat(projection).isNotNull();
        assertThat(projection.didCollide()).isTrue();
    }


    @SuppressWarnings("ConstantConditions")
    @Test
    public void testNoCollision() throws Exception {
        // given
        final TiledSpace<Agent> space = TiledSpace.ofSize(1,1);
        Agent agent = ImmutableAgent.of(Population.named("test")).build();
        agent.setMotion(ImmutableMotion2D.of(0, 0.5));
        space.insertObject(agent, 0, 0, MathLib.HALF_PI / 2);

        // when
        space.moveObject(agent);

        // then
        final MotionObject2D projection = agent.getProjection();
        assertThat(projection).isNotNull();
        assertThat(projection.didCollide()).isFalse();
    }

    @Test
    public void testFindVisibleNeighbours() throws Exception {
        // given
        final TiledSpace<MovingProjectable2D> space = TiledSpace.builder(3, 1).addWall(0, 0, TileDirection.EAST).build();
        final MovingProjectable2DImpl focal = new MovingProjectable2DImpl();
        final MovingProjectable2DImpl neighbour1 = new MovingProjectable2DImpl();
        final MovingProjectable2DImpl neighbour2 = new MovingProjectable2DImpl();

        space.insertObject(focal, 1.5, 0.5, 0);
        space.insertObject(neighbour1, 0.5, 0.5, 0);
        space.insertObject(neighbour2, 2.5, 0.5, 0);

        // when
        final Iterable<MovingProjectable2D> visibleNeighbours = space.getVisibleNeighbours(focal, 2.0);

        // then
        assertThat(visibleNeighbours).containsOnly(neighbour2);
    }
}
