package org.asoem.greyfish.core.simulation;

import com.google.common.collect.Iterables;
import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.space.TileDirection;
import org.asoem.greyfish.core.space.WalledTileSpace;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;

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
        final WalledTileSpace<Agent> space = WalledTileSpace.<Agent>builder(1, 1).build();
        space.insertObject(prototype, 0, 0, 0);
        space.insertObject(prototype, 0, 0, 0);

        // when
        ParallelizedSimulation simulation = new ParallelizedSimulation(1000, space);

        // then
        assertThat(Iterables.size(simulation.getAgents())).isEqualTo(2);
    }

    @Test
    public void testBasicPersistence() throws Exception {
        // given
        final WalledTileSpace<Agent> space = WalledTileSpace.<Agent>builder(1, 1)
                .addWall(0, 0, TileDirection.NORTH)
                .build();
        final BasicSimulationTemplate scenario = BasicSimulationTemplate.builder("TestScenario", space).build();
        final ParallelizedSimulation simulation = scenario.createSimulation(new ParallelizedSimulationFactory(1000));

        // when
        final ParallelizedSimulation copy = Persisters.createCopy(simulation, ParallelizedSimulation.class, persister);

        // then
        assertThat(copy).isEqualTo(simulation); // TODO: Overwritten equals was removed. Fix this test
    }
}
