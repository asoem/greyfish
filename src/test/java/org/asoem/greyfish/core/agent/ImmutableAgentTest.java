package org.asoem.greyfish.core.agent;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.persistence.Persister;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * User: christoph
 * Date: 20.09.11
 * Time: 17:54
 */
@RunWith(MockitoJUnitRunner.class)
public class ImmutableAgentTest {

    @Mock
    AgentTrait<?> gene;
    @Mock
    AgentAction action;
    @Mock
    AgentProperty property;
    @Mock
    Population population;

    @Inject
    private Persister persister;

    public ImmutableAgentTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddGene() throws Exception {
        // given
        ImmutableAgent agent = ImmutableAgent.builder(population).build();

        // when
        agent.addGene(gene);

        // then
        // UnsupportedOperationException
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddAction() throws Exception {
        // given
        ImmutableAgent agent = ImmutableAgent.builder(population).build();

        // when
        agent.addAction(action);

        // then
        // UnsupportedOperationException
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddProperty() throws Exception {
        // given
        ImmutableAgent agent = ImmutableAgent.builder(population).build();

        // when
        agent.addProperty(property);

        // then
        // UnsupportedOperationException
    }

    @Test
    public void testGetGene() throws Exception {
        // given
        given(gene.getName()).willReturn("foo");
        given(gene.children()).willReturn(Collections.<AgentComponent>emptyList());
        ImmutableAgent agent = ImmutableAgent.builder(population).addTraits(gene).build();

        // when
        AgentTrait ret = agent.getGene("foo", AgentTrait.class);

        // then
        verify(gene).setAgent(agent);
        assertThat(ret).isEqualTo(gene);
    }

    @Test
    public void testDeepClone() throws Exception {
        // given
        final DeepCloner clonerMock = mock(DeepCloner.class);
        final ComponentList componentListMock = mock(ComponentList.class);
        given(clonerMock.getClone(any(ComponentList.class), eq(ComponentList.class))).willReturn(componentListMock);

        Population population = mock(Population.class);

        final ImmutableAgent agent = ImmutableAgent.builder(population).build();

        // when
        final ImmutableAgent clone = agent.deepClone(clonerMock);

        // then
        assertThat(clone.getPopulation()).isEqualTo(population);
        assertThat(clone.getActions()).isEqualTo(componentListMock);
        assertThat(clone.getProperties()).isEqualTo(componentListMock);
        assertThat(clone.getTraits()).isEqualTo(componentListMock);
    }
}
