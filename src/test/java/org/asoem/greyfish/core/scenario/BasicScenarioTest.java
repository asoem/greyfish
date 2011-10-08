package org.asoem.greyfish.core.scenario;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Placeholder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.exceptions.base.ConditionalStackTraceFilter;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;
import java.util.concurrent.locks.Condition;

import static org.asoem.greyfish.core.space.MutableObject2D.locatedAt;
import static org.asoem.greyfish.core.space.TiledSpace.ofSize;
import static org.junit.Assert.assertEquals;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class BasicScenarioTest {

    @Mock Agent prototype;

    @Test
    public void builderTest() {
        // given
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
}