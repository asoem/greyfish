package org.asoem.greyfish.core.simulation;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.space.WalledTileSpace;
import org.asoem.greyfish.utils.persistence.Persister;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static org.asoem.greyfish.utils.space.MutableObject2D.locatedAt;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class BasicSimulationTemplateTest {

    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public BasicSimulationTemplateTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void builderTest() {
        // given

        final Population population = mock(Population.class);
        final Agent prototype = mock(Agent.class);
        given(prototype.getPopulation()).willReturn(population);
        final Agent prototype2 = mock(Agent.class);
        given(prototype2.getPopulation()).willReturn(population);

        SimulationTemplate simulationTemplate = BasicSimulationTemplate.builder("TestScenario", WalledTileSpace.<Agent>ofSize(1, 1))
                .addAgent(prototype, locatedAt(0.0, 0.0))
                .addAgent(prototype2, locatedAt(0.0, 0.0))
                .build();
        // when
        Set<Agent> prototypes = simulationTemplate.getPrototypes();
        Iterable<Agent> agents = simulationTemplate.getPlaceholder();

        // then
        assertThat(prototypes)
                .hasSize(1);
        assertThat(agents)
                .hasSize(2)
                .excludes(new Object[] {null});
    }

    @Test
    public void testPersistence() throws Exception {
        /*
        final Agent prototype = ImmutableAgent.of(Population.newPopulation("TestPopulation", Color.blue)).build();
        // given
        SimulationTemplate simulationTemplate = BasicSimulationTemplate.builder("TestScenario", WalledTileSpace.<Agent>ofSize(3, 4))
                .addAgent(prototype, locatedAt(0.42, 1.42))
                .addAgent(prototype, locatedAt(0.42, 1.42))
                .build();

        // when
        final BasicSimulationTemplate copy = Persisters.createCopy(simulationTemplate, BasicSimulationTemplate.class, persister);

        // then
        assertThat(copy.getName()).isEqualTo("TestScenario");
        assertThat(copy.getSpace().getWidth()).isEqualTo(3);
        assertThat(copy.getSpace().getHeight()).isEqualTo(4);
        assertThat(copy.getPrototypes()).hasSize(1).satisfies(new Condition<Collection<?>>("all elements are Agents") {
            @Override
            public boolean matches(Collection<?> objects) {
                return Iterables.all(objects, Predicates.instanceOf(Agent.class));
            }
        });
        assertThat(copy.getPlaceholder()).hasSize(2).satisfies(new Condition<Iterator<?>>("are located at defined coordinates") {
            @Override
            public boolean matches(Iterator<?> iterator) {
                return Iterators.all(iterator, new Predicate<Object>() {
                    @Override
                    public boolean apply(@Nullable Object o) {
                        return o != null
                                && o instanceof Agent
                                && locatedAt(0.42, 1.42).equals(((Agent) o).getProjection());
                    }
                });
            }
        });
        */
    }
}