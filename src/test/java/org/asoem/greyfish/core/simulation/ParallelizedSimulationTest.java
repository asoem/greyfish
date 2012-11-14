package org.asoem.greyfish.core.simulation;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Inject;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.agent.ActiveSimulationContext;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.FrozenAgent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.space.WalledPointSpace;
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.space.MotionObject2DImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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
        final Agent prototype = FrozenAgent.builder(population).build();
        final WalledPointSpace<Agent> space = WalledPointSpace.builder(1, 1).build();

        // when
        ParallelizedSimulation simulation = ParallelizedSimulation.builder(space, ImmutableSet.of(prototype))
                .agentPool(new StackKeyedObjectPool<Population, Agent>(new BaseKeyedPoolableObjectFactory<Population, Agent>() {
                    @Override
                    public Agent makeObject(Population o) throws Exception {
                        return prototype;
                    }
                })).build();
        simulation.createAgent(population);
        simulation.createAgent(population);
        simulation.nextStep();

        // then
        assertThat(simulation.getAgents(), hasSize(2));
    }

    @Test
    public void testBasicPersistence() throws Exception {
        /*
        // given
        final WalledPointSpace<Agent> space = WalledPointSpace.<Agent>builder(1, 1)
                .addWall(0, 0, TileDirection.NORTH)
                .build();
        final BasicSimulationTemplate scenario = BasicSimulationTemplate.builder("TestScenario", space).build();
        final ParallelizedSimulation simulation = scenario.createSimulation(new ParallelizedSimulationFactory(1000));

        // when
        final ParallelizedSimulation copy = Persisters.createCopy(simulation, ParallelizedSimulation.class, persister);

        // then
        assertThat(copy).isEqualTo(simulation); // TODO: Overwritten equals was removed. Fix this test
        */
    }

    @Test
    public void testCreateAgent() throws Exception {
        // given
        final Agent agent = mock(Agent.class);
        final Population testPopulation = Population.named("TestPopulation");
        given(agent.getPopulation()).willReturn(testPopulation);
        given(agent.hasPopulation(testPopulation)).willReturn(true);
        given(agent.getProjection()).willReturn(MotionObject2DImpl.of(0, 0));
        @SuppressWarnings("unchecked")
        final Initializer<Agent> initializer = mock(Initializer.class);

        final KeyedObjectPool<Population, Agent> pool =
                new StackKeyedObjectPool<Population, Agent>(new BaseKeyedPoolableObjectFactory<Population, Agent>() {
                    @Override
                    public Agent makeObject(Population population) throws Exception {
                        return agent;
                    }
                });
        final WalledPointSpace<Agent> space = WalledPointSpace.ofSize(1, 1);
        final ImmutableSet<Agent> prototypes = ImmutableSet.of(agent);
        final ParallelizedSimulation simulation = ParallelizedSimulation.builder(space, prototypes)
                .agentPool(pool)
                .build();

        // when
        simulation.createAgent(testPopulation, initializer);
        simulation.nextStep();

        // then
        final InOrder inOrder = inOrder(agent, initializer);
        inOrder.verify(agent).initialize();
        inOrder.verify(initializer).initialize(agent);

        assertThat(simulation.getAgents(), contains(agent));
        assertThat(simulation.getAgents(testPopulation), contains(agent));
        verify(agent).activate(any(ActiveSimulationContext.class));
    }

    @Test
    public void testRemoveAgent() throws Exception {
        // given
        final Agent agent = mock(Agent.class);
        final Population testPopulation = Population.named("TestPopulation");
        given(agent.getPopulation()).willReturn(testPopulation);
        given(agent.getProjection()).willReturn(MotionObject2DImpl.of(0, 0));
        final KeyedObjectPool<Population, Agent> pool =
                new StackKeyedObjectPool<Population, Agent>(new BaseKeyedPoolableObjectFactory<Population, Agent>() {
                    @Override
                    public Agent makeObject(Population population) throws Exception {
                        return agent;
                    }
                });
        final WalledPointSpace<Agent> space = WalledPointSpace.ofSize(1, 1);
        final ImmutableSet<Agent> prototypes = ImmutableSet.of(agent);
        final ParallelizedSimulation simulation = ParallelizedSimulation.builder(space, prototypes)
                .agentPool(pool)
                .build();
        given(agent.simulation()).willReturn(simulation);

        // when
        simulation.createAgent(testPopulation);
        simulation.nextStep();
        simulation.removeAgent(agent);
        simulation.nextStep();

        // then
        assertThat(simulation.getAgents(), is(empty()));
        assertThat(simulation.getAgents(testPopulation), is(emptyIterable()));
        assertThat(agent.isActive(), is(false));
    }
}