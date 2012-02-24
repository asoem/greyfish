package org.asoem.greyfish.core.scenario;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Placeholder;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.asoem.greyfish.utils.space.ImmutableLocatable2D;
import org.fest.assertions.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static org.asoem.greyfish.core.space.TiledSpace.ofSize;
import static org.asoem.greyfish.utils.space.MutableObject2D.locatedAt;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class BasicScenarioTest {

    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public BasicScenarioTest() {
        CoreInjectorHolder.coreInjector().injectMembers(this);
    }

    @Test
    public void builderTest() {
        // given
        final Agent prototype = mock(Agent.class);
        Scenario scenario = BasicScenario.builder("TestScenario", ofSize(1, 1))
                .addAgent(prototype, locatedAt(0.0, 0.0))
                .addAgent(prototype, locatedAt(0.0, 0.0))
                .build();
        // when
        Set<Agent> prototypes = scenario.getPrototypes();
        Iterable<Placeholder> agents = scenario.getPlaceholder();

        // then
        assertThat(prototypes)
                .containsOnly(prototype);
        assertThat(agents)
                .hasSize(2)
                .excludes(new Object[] {null});
    }

    @Test
    public void testPersistence() throws Exception {
        final Agent prototype = ImmutableAgent.of(Population.newPopulation("TestPopulation", Color.blue)).build();
        // given
        Scenario scenario = BasicScenario.builder("TestScenario", ofSize(3, 4))
                .addAgent(prototype, locatedAt(0.42, 1.42))
                .addAgent(prototype, locatedAt(0.42, 1.42))
                .build();

        // when
        final BasicScenario copy = Persisters.createCopy(scenario, BasicScenario.class, persister);

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
                                && o instanceof Placeholder
                                && copy.getSpace().getCoordinates((Placeholder) o).equals(ImmutableLocatable2D.at(0.42, 1.42));
                    }
                });
            }
        });
    }
}