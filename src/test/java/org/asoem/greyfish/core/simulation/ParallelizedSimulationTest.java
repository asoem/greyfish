package org.asoem.greyfish.core.simulation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.space.TileDirection;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.core.space.WalledTile;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.asoem.greyfish.utils.space.MovingProjectable2D;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ParallelizedSimulationTest {

    @Inject
    private Persister persister;

    public ParallelizedSimulationTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void newSimulationTest() {
        // given

        final Population population = new Population("testPopulation");
        final Agent prototype = ImmutableAgent.of(population).build();
        final TiledSpace<Agent> space = TiledSpace.<Agent>builder(1, 1).build();
        space.insertObject(prototype, 0, 0, 0);
        space.insertObject(prototype, 0, 0, 0);

        // when
        ParallelizedSimulation simulation = new ParallelizedSimulation(space);

        // then
        assertThat(Iterables.size(simulation.getAgents())).isEqualTo(2);
    }

    @Test
    public void testBasicPersistence() throws Exception {
        // given
        final TiledSpace<Agent> space = TiledSpace.<Agent>builder(1, 1)
                .addWall(0, 0, TileDirection.NORTH)
                .build();
        final BasicScenario scenario = BasicScenario.builder("TestScenario", space).build();
        final ParallelizedSimulation simulation = scenario.createSimulation(ParallelizedSimulationFactory.INSTANCE);

        // when
        final ParallelizedSimulation copy = Persisters.createCopy(simulation, ParallelizedSimulation.class, persister);

        // then
        assertThat(copy).isEqualTo(simulation); // TODO: Overwritten equals was removed. Fix this test
    }
}
