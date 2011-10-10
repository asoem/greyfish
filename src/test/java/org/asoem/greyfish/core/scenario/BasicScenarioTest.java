package org.asoem.greyfish.core.scenario;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Placeholder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static org.asoem.greyfish.core.space.MutableObject2D.locatedAt;
import static org.asoem.greyfish.core.space.TiledSpace.ofSize;
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