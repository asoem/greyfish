package org.asoem.greyfish.impl.simulation;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Inject;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.asoem.greyfish.core.agent.DefaultActiveSimulationContext;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.agent.DefaultBasic2DAgent;
import org.asoem.greyfish.impl.space.BasicTiled2DSpace;
import org.asoem.greyfish.impl.space.DefaultBasicTiled2DSpace;
import org.asoem.greyfish.utils.base.CycleCloner;
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.collect.ImmutableFunctionalList;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.space.ImmutablePoint2D;
import org.asoem.greyfish.utils.space.Point2D;
import org.asoem.greyfish.utils.space.TwoDimTree;
import org.asoem.greyfish.utils.space.TwoDimTreeFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultBasic2DSimulationTest {

    @Inject
    private Persister persister;

    public DefaultBasic2DSimulationTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNewSimulation() {
        // given
        final Population population = Population.named("testPopulation");
        final Basic2DAgent prototype = DefaultBasic2DAgent.builder(population).build();
        final BasicTiled2DSpace space = DefaultBasicTiled2DSpace.ofSize(1, 1, new TwoDimTreeFactory<Basic2DAgent>() {
            @Override
            public TwoDimTree<Basic2DAgent> create(final Iterable<? extends Basic2DAgent> elements, final Function<? super Basic2DAgent, Point2D> function) {
                return mock(TwoDimTree.class);
            }
        });

        // when
        final DefaultBasic2DSimulation simulation = DefaultBasic2DSimulation.builder(space, ImmutableSet.of(prototype))
                .agentPool(new StackKeyedObjectPool<Population, Basic2DAgent>(new BaseKeyedPoolableObjectFactory<Population, Basic2DAgent>() {
                    @Override
                    public Basic2DAgent makeObject(final Population o) throws Exception {
                        return CycleCloner.clone(prototype);
                    }
                })).build();
        simulation.addAgent(CycleCloner.clone(prototype), ImmutablePoint2D.at(0, 0));
        simulation.addAgent(CycleCloner.clone(prototype), ImmutablePoint2D.at(0, 0));
        simulation.nextStep();

        // then
        assertThat(simulation.getAgents(), hasSize(2));
    }

    @Test
    public void testCreateSimulationFromAgents() {
        // given
        final Population population = Population.named("testPopulation");
        final Basic2DAgent agent1 = DefaultBasic2DAgent.builder(population).build();
        final Collection<Basic2DAgent> agents = ImmutableList.of(agent1);
        final Basic2DAgent prototype = DefaultBasic2DAgent.builder(population).build();
        final BasicTiled2DSpace space = DefaultBasicTiled2DSpace.ofSize(1, 1, new TwoDimTreeFactory<Basic2DAgent>() {
            @Override
            public TwoDimTree<Basic2DAgent> create(final Iterable<? extends Basic2DAgent> elements, final Function<? super Basic2DAgent, Point2D> function) {
                return mock(TwoDimTree.class);
            }
        });

        // when
        final DefaultBasic2DSimulation simulation = DefaultBasic2DSimulation
                .builder(space, ImmutableSet.of(prototype))
                .build();
        for (Basic2DAgent agent : agents) {
            simulation.addAgent(agent, ImmutablePoint2D.at(0, 0));
        }
        simulation.nextStep();

        // then
        assertThat(simulation.getAgents(), containsInAnyOrder(agents.toArray()));
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
        final Basic2DAgent agent = mock(Basic2DAgent.class);
        final Population testPopulation = Population.named("TestPopulation");
        given(agent.getPopulation()).willReturn(testPopulation);
        given(agent.hasPopulation(testPopulation)).willReturn(true);
        given(agent.getProjection()).willReturn(ImmutablePoint2D.at(0, 0));
        given(agent.getTraits()).willReturn(ImmutableFunctionalList.<AgentTrait<Basic2DAgent, ?>>of());
        @SuppressWarnings("unchecked")
        final Initializer<Basic2DAgent> initializer = mock(Initializer.class);

        final KeyedObjectPool<Population, Basic2DAgent> pool =
                new StackKeyedObjectPool<Population, Basic2DAgent>(new BaseKeyedPoolableObjectFactory<Population, Basic2DAgent>() {
                    @Override
                    public Basic2DAgent makeObject(final Population population) throws Exception {
                        return agent;
                    }
                });
        final BasicTiled2DSpace space = DefaultBasicTiled2DSpace.ofSize(1, 1, new TwoDimTreeFactory<Basic2DAgent>() {
            @Override
            public TwoDimTree<Basic2DAgent> create(final Iterable<? extends Basic2DAgent> elements, final Function<? super Basic2DAgent, Point2D> function) {
                return mock(TwoDimTree.class);
            }
        });
        final ImmutableSet<Basic2DAgent> prototypes = ImmutableSet.of(agent);
        final DefaultBasic2DSimulation simulation =
                DefaultBasic2DSimulation.builder(space, prototypes)
                        .agentPool(pool)
                        .build();

        // when
        simulation.addAgent(agent, ImmutablePoint2D.at(0, 0));
        simulation.nextStep();

        // then
        assertThat(simulation.getAgents(), contains(agent));
        assertThat(simulation.getAgents(testPopulation), contains(agent));
        verify(agent).activate(any(DefaultActiveSimulationContext.class));
    }

    @Test
    public void testCreateAgentWithNewChromosome() {
        // given
        final Basic2DAgent agent = mock(Basic2DAgent.class);
        final Population testPopulation = Population.named("TestPopulation");
        final Chromosome chromosome = mock(Chromosome.class);
        final Point2D point2D = mock(Point2D.class);
        given(agent.getProjection()).willReturn(point2D);
        given(agent.getPopulation()).willReturn(testPopulation);
        given(agent.hasPopulation(testPopulation)).willReturn(true);
        given(agent.getTraits()).willReturn(ImmutableFunctionalList.<AgentTrait<Basic2DAgent, ?>>of());

        final KeyedObjectPool<Population, Basic2DAgent> pool =
                new StackKeyedObjectPool<Population, Basic2DAgent>(new BaseKeyedPoolableObjectFactory<Population, Basic2DAgent>() {
                    @Override
                    public Basic2DAgent makeObject(final Population population) throws Exception {
                        return agent;
                    }
                });
        final BasicTiled2DSpace space = DefaultBasicTiled2DSpace.ofSize(1, 1, new TwoDimTreeFactory<Basic2DAgent>() {
            @Override
            public TwoDimTree<Basic2DAgent> create(final Iterable<? extends Basic2DAgent> elements, final Function<? super Basic2DAgent, Point2D> function) {
                return mock(TwoDimTree.class);
            }
        });
        final ImmutableSet<Basic2DAgent> prototypes = ImmutableSet.of(agent);
        final DefaultBasic2DSimulation simulation =
                DefaultBasic2DSimulation.builder(space, prototypes)
                        .agentPool(pool)
                        .build();

        // when
        simulation.addAgent(agent, point2D);
        simulation.nextStep();

        // then
        assertThat(simulation.getAgents(), contains(agent));
        verify(agent).activate(any(DefaultActiveSimulationContext.class));
        verify(agent).setProjection(point2D);
    }

    @Test
    public void testRemoveAgent() throws Exception {
        // given
        final Basic2DAgent agent = mock(Basic2DAgent.class);
        final Population testPopulation = Population.named("TestPopulation");
        given(agent.getPopulation()).willReturn(testPopulation);
        given(agent.getTraits()).willReturn(ImmutableFunctionalList.<AgentTrait<Basic2DAgent, ?>>of());
        final BasicTiled2DSpace space = DefaultBasicTiled2DSpace.ofSize(1, 1, new TwoDimTreeFactory<Basic2DAgent>() {
            @Override
            public TwoDimTree<Basic2DAgent> create(final Iterable<? extends Basic2DAgent> elements, final Function<? super Basic2DAgent, Point2D> function) {
                return mock(TwoDimTree.class);
            }
        });
        final ImmutableSet<Basic2DAgent> prototypes = ImmutableSet.of(agent);
        final DefaultBasic2DSimulation simulation = DefaultBasic2DSimulation.builder(space, prototypes)
                .build();
        //given(agent.simulation()).willReturn(simulation);

        // when
        simulation.addAgent(agent, ImmutablePoint2D.at(0, 0));
        simulation.nextStep();
        simulation.removeAgent(agent);
        simulation.nextStep();

        // then
        assertThat(simulation.getAgents(), is(empty()));
        assertThat(simulation.getAgents(testPopulation), is(emptyIterable()));
        assertThat(agent.isActive(), is(false));
    }

    @Test(expected = Throwable.class)
    public void testNextStepWithException() throws Exception {
        // given
        final Basic2DAgent agent = mock(Basic2DAgent.class);
        final Population testPopulation = Population.named("TestPopulation");
        given(agent.getPopulation()).willReturn(testPopulation);
        given(agent.getTraits()).willReturn(ImmutableFunctionalList.<AgentTrait<Basic2DAgent, ?>>of());
        doThrow(new RuntimeException()).when(agent).run();
        final KeyedObjectPool<Population, Basic2DAgent> pool =
                new StackKeyedObjectPool<Population, Basic2DAgent>(new BaseKeyedPoolableObjectFactory<Population, Basic2DAgent>() {
                    @Override
                    public Basic2DAgent makeObject(final Population population) throws Exception {
                        return agent;
                    }
                });
        final BasicTiled2DSpace space = DefaultBasicTiled2DSpace.ofSize(1, 1, new TwoDimTreeFactory<Basic2DAgent>() {
            @Override
            public TwoDimTree<Basic2DAgent> create(final Iterable<? extends Basic2DAgent> elements, final Function<? super Basic2DAgent, Point2D> function) {
                return mock(TwoDimTree.class);
            }
        });
        final ImmutableSet<Basic2DAgent> prototypes = ImmutableSet.of(agent);
        final DefaultBasic2DSimulation simulation = DefaultBasic2DSimulation.builder(space, prototypes)
                .agentPool(pool)
                .build();
        simulation.addAgent(agent, ImmutablePoint2D.at(0, 0));

        // when
        simulation.nextStep();

        // then
        fail();
    }
}