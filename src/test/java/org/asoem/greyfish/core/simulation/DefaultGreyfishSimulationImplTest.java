package org.asoem.greyfish.core.simulation;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Inject;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.agent.*;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.space.DefaultGreyfishSpace;
import org.asoem.greyfish.core.space.DefaultGreyfishSpaceImpl;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.utils.base.CycleCloner;
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.collect.ImmutableFunctionalList;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.asoem.greyfish.utils.space.Point2D;
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
public class DefaultGreyfishSimulationImplTest {

    @Inject
    private Persister persister;

    public DefaultGreyfishSimulationImplTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void newSimulationTest() {
        // given
        final Population population = new Population("testPopulation");
        final DefaultGreyfishAgent prototype = DefaultGreyfishAgentImpl.builder(population).build();
        final DefaultGreyfishSpace space = DefaultGreyfishSpaceImpl.ofSize(1, 1);

        // when
        final DefaultGreyfishSimulationImpl simulation = DefaultGreyfishSimulationImpl.builder(space, ImmutableSet.of(prototype))
                .agentPool(new StackKeyedObjectPool<Population, DefaultGreyfishAgent>(new BaseKeyedPoolableObjectFactory<Population, DefaultGreyfishAgent>() {
                    @Override
                    public DefaultGreyfishAgent makeObject(final Population o) throws Exception {
                        return CycleCloner.clone(prototype);
                    }
                })).build();
        simulation.createAgent(population, AgentInitializers.<Point2D>projection(ImmutablePoint2D.at(0, 0)));
        simulation.createAgent(population, AgentInitializers.<Point2D>projection(ImmutablePoint2D.at(0, 0)));
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
        final BasicSpatialSimulation simulation = scenario.createSimulation(new ParallelizedSimulationFactory(1000));

        // when
        final BasicSpatialSimulation copy = Persisters.createCopy(simulation, BasicSpatialSimulation.class, persister);

        // then
        assertThat(copy).isEqualTo(simulation); // TODO: Overwritten equals was removed. Fix this test
        */
    }

    @Test
    public void testCreateAgent() throws Exception {
        // given
        final DefaultGreyfishAgent agent = mock(DefaultGreyfishAgent.class);
        final Population testPopulation = Population.named("TestPopulation");
        given(agent.getPopulation()).willReturn(testPopulation);
        given(agent.hasPopulation(testPopulation)).willReturn(true);
        given(agent.getProjection()).willReturn(ImmutablePoint2D.at(0, 0));
        given(agent.getTraits()).willReturn(ImmutableFunctionalList.<AgentTrait<DefaultGreyfishAgent, ?>>of());
        @SuppressWarnings("unchecked")
        final Initializer<DefaultGreyfishAgent> initializer = mock(Initializer.class);

        final KeyedObjectPool<Population, DefaultGreyfishAgent> pool =
                new StackKeyedObjectPool<Population, DefaultGreyfishAgent>(new BaseKeyedPoolableObjectFactory<Population, DefaultGreyfishAgent>() {
                    @Override
                    public DefaultGreyfishAgent makeObject(final Population population) throws Exception {
                        return agent;
                    }
                });
        final DefaultGreyfishSpace space = DefaultGreyfishSpaceImpl.ofSize(1, 1);
        final ImmutableSet<DefaultGreyfishAgent> prototypes = ImmutableSet.of(agent);
        final DefaultGreyfishSimulationImpl simulation =
                DefaultGreyfishSimulationImpl.builder(space, prototypes)
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
    public void testCreateAgentWithNewChromosome() {
        // given
        final DefaultGreyfishAgent agent = mock(DefaultGreyfishAgent.class);
        final Population testPopulation = Population.named("TestPopulation");
        final Chromosome chromosome = mock(Chromosome.class);
        final Point2D point2D = mock(Point2D.class);
        given(agent.getProjection()).willReturn(point2D);
        given(agent.getPopulation()).willReturn(testPopulation);
        given(agent.hasPopulation(testPopulation)).willReturn(true);

        final KeyedObjectPool<Population, DefaultGreyfishAgent> pool =
                new StackKeyedObjectPool<Population, DefaultGreyfishAgent>(new BaseKeyedPoolableObjectFactory<Population, DefaultGreyfishAgent>() {
                    @Override
                    public DefaultGreyfishAgent makeObject(final Population population) throws Exception {
                        return agent;
                    }
                });
        final DefaultGreyfishSpace space = DefaultGreyfishSpaceImpl.ofSize(1, 1);
        final ImmutableSet<DefaultGreyfishAgent> prototypes = ImmutableSet.of(agent);
        final DefaultGreyfishSimulationImpl simulation =
                DefaultGreyfishSimulationImpl.builder(space, prototypes)
                        .agentPool(pool)
                        .build();

        // when
        simulation.createAgent(testPopulation, point2D, chromosome);
        simulation.nextStep();

        // then
        assertThat(simulation.getAgents(), contains(agent));
        verify(agent).activate(any(ActiveSimulationContext.class));
        verify(chromosome).updateAgent(agent);
        verify(agent).setProjection(point2D);
    }

    @Test
    public void testRemoveAgent() throws Exception {
        // given
        final DefaultGreyfishAgent agent = mock(DefaultGreyfishAgent.class);
        final Population testPopulation = Population.named("TestPopulation");
        given(agent.getPopulation()).willReturn(testPopulation);
        given(agent.getProjection()).willReturn(ImmutablePoint2D.at(0, 0));
        given(agent.getTraits()).willReturn(ImmutableFunctionalList.<AgentTrait<DefaultGreyfishAgent, ?>>of());
        final KeyedObjectPool<Population, DefaultGreyfishAgent> pool =
                new StackKeyedObjectPool<Population, DefaultGreyfishAgent>(new BaseKeyedPoolableObjectFactory<Population, DefaultGreyfishAgent>() {
                    @Override
                    public DefaultGreyfishAgent makeObject(final Population population) throws Exception {
                        return agent;
                    }
                });
        final DefaultGreyfishSpace space = DefaultGreyfishSpaceImpl.ofSize(1, 1);
        final ImmutableSet<DefaultGreyfishAgent> prototypes = ImmutableSet.of(agent);
        final DefaultGreyfishSimulationImpl simulation = DefaultGreyfishSimulationImpl.builder(space, prototypes)
                .agentPool(pool)
                .build();
        //given(agent.simulation()).willReturn(simulation);

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