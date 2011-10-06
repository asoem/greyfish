package org.asoem.greyfish.core.scenario;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Placeholder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.asoem.greyfish.core.space.MutableObject2D.at;
import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class ScenarioTest {

    @Mock Agent prototype;

    @Test
    public void builderTest() {
        // given
        Scenario scenario = Scenario.with().name("foo")
                .add(prototype, at(0,0))
                .add(prototype, at(0,0))
                .space(1,1)
                .build();
        // when
        Iterable<Agent> prototypes = scenario.getPrototypes();
        Iterable<Placeholder> agents = scenario.getPlaceholder();

        // then
        assertEquals(ImmutableList.of(prototype), ImmutableList.copyOf(prototypes));
        assertEquals(2, Iterables.size(agents));
    }
}