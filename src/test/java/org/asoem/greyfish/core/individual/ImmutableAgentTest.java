package org.asoem.greyfish.core.individual;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.persistence.Persister;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
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
    GFAction action;
    @Mock
    GFProperty property;
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
        ImmutableAgent agent = ImmutableAgent.of(population).build();

        // when
        agent.addGene(gene);

        // then
        // UnsupportedOperationException
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddAction() throws Exception {
        // given
        ImmutableAgent agent = ImmutableAgent.of(population).build();

        // when
        agent.addAction(action);

        // then
        // UnsupportedOperationException
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddProperty() throws Exception {
        // given
        ImmutableAgent agent = ImmutableAgent.of(population).build();

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
        ImmutableAgent agent = ImmutableAgent.of(population).addTraits(gene).build();

        // when
        AgentTrait ret = agent.getGene("foo", AgentTrait.class);

        // then
        verify(gene).setAgent(agent);
        assertThat(ret).isEqualTo(gene);
    }

    @Test
    public void testDeepClone() throws Exception {

        // given
        Population population = mock(Population.class);

        final GFAction actionMock = mock(GFAction.class);
        final GFAction actionMockClone = mock(GFAction.class);
        given(actionMock.deepClone(any(DeepCloner.class))).willReturn(actionMockClone);

        final GFProperty propertyMock = mock(GFProperty.class);
        final GFProperty propertyMockClone = mock(GFProperty.class);
        given(propertyMock.deepClone(any(DeepCloner.class))).willReturn(propertyMockClone);

        final AgentTrait traitMock = mock(AgentTrait.class);
        final AgentTrait traitMockClone = mock(AgentTrait.class);
        given(traitMock.deepClone(any(DeepCloner.class))).willReturn(traitMockClone);

        final ImmutableAgent agent = ImmutableAgent.of(population)
                .addActions(actionMock)
                .addProperties(propertyMock)
                .addTraits(traitMock)
                .build();

        // when
        final ImmutableAgent clone = DeepCloner.clone(agent, ImmutableAgent.class);

        // then
        assertThat(clone.getPopulation()).isEqualTo(population);
        assertThat(clone.getActions()).containsOnly(actionMockClone);
        assertThat(clone.getProperties()).containsOnly(propertyMockClone);
        assertThat(clone.getTraits()).containsOnly(traitMockClone);
    }

    @Test
    public void testFromPrototype() {
        // given
        Population population = mock(Population.class);

        final GFAction actionMock = mock(GFAction.class);
        final GFAction actionMockClone = mock(GFAction.class);
        given(actionMock.deepClone(any(DeepCloner.class))).willReturn(actionMockClone);

        final GFProperty propertyMock = mock(GFProperty.class);
        final GFProperty propertyMockClone = mock(GFProperty.class);
        given(propertyMock.deepClone(any(DeepCloner.class))).willReturn(propertyMockClone);

        final AgentTrait traitMock = mock(AgentTrait.class);
        final AgentTrait traitMockClone = mock(AgentTrait.class);
        given(traitMock.deepClone(any(DeepCloner.class))).willReturn(traitMockClone);

        final ImmutableAgent agent = ImmutableAgent.of(population)
                .addActions(actionMock)
                .addProperties(propertyMock)
                .addTraits(traitMock)
                .build();

        // when
        ImmutableAgent clone = ImmutableAgent.fromPrototype(agent);

        // then
        assertThat(clone.getPopulation()).isEqualTo(population);
        assertThat(clone.getActions()).containsOnly(actionMockClone);
        assertThat(clone.getProperties()).containsOnly(propertyMockClone);
        assertThat(clone.getTraits()).containsOnly(traitMockClone);
    }
}
