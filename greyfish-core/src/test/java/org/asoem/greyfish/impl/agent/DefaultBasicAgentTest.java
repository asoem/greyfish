package org.asoem.greyfish.impl.agent;

import org.asoem.greyfish.core.agent.Population;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class DefaultBasicAgentTest {

    @Test
    public void testBuilder() throws Exception {
        // given
        final Population population = mock(Population.class);

        // when
        final DefaultBasicAgent agent = DefaultBasicAgent.builder(population).build();

        // then
        assertThat(agent, is(notNullValue()));
        assertThat(agent.getPopulation(), is(population));
    }

    @Test(expected = NullPointerException.class)
    public void testBuilderNullPopulation() throws Exception {
        // given
        final Population population = null;

        // when
        DefaultBasicAgent.builder(population).build();

        // then
        fail();
    }
}
