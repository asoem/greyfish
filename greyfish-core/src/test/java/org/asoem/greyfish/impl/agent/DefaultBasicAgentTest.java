package org.asoem.greyfish.impl.agent;

import org.asoem.greyfish.core.agent.PrototypeGroup;
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
        final PrototypeGroup prototypeGroup = mock(PrototypeGroup.class);

        // when
        final DefaultBasicAgent agent = DefaultBasicAgent.builder(prototypeGroup).build();

        // then
        assertThat(agent, is(notNullValue()));
        assertThat(agent.getPrototypeGroup(), is(prototypeGroup));
    }

    @Test(expected = NullPointerException.class)
    public void testBuilderNullPopulation() throws Exception {
        // given
        final PrototypeGroup prototypeGroup = null;

        // when
        DefaultBasicAgent.builder(prototypeGroup).build();

        // then
        fail();
    }
}
