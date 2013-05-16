package org.asoem.greyfish.core.agent;

import com.google.common.base.Function;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.utils.base.CycleCloner;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.asoem.utils.test.GreyfishMatchers.has;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * User: christoph
 * Date: 20.09.11
 * Time: 17:54
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultGreyfishAgentImplTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testAddGene() throws Exception {
        // given
        DefaultGreyfishAgentImpl agent = DefaultGreyfishAgentImpl.builder(mock(Population.class)).build();

        // when
        agent.addTrait(mock(AgentTrait.class));

        // then
        // UnsupportedOperationException
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddAction() throws Exception {
        // given
        DefaultGreyfishAgentImpl agent = DefaultGreyfishAgentImpl.builder(mock(Population.class)).build();

        // when
        agent.addAction(mock(AgentAction.class));

        // then
        // UnsupportedOperationException
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddProperty() throws Exception {
        // given
        DefaultGreyfishAgentImpl agent = DefaultGreyfishAgentImpl.builder(mock(Population.class)).build();

        // when
        agent.addProperty(mock(AgentProperty.class));

        // then
        // UnsupportedOperationException
    }

    @Test
    public void testGetGene() throws Exception {
        // given
        AgentTrait<DefaultGreyfishAgent, Object> gene = mock(AgentTrait.class);
        given(gene.getName()).willReturn("foo");
        given(gene.children()).willReturn(Collections.<AgentNode>emptyList());
        DefaultGreyfishAgentImpl agent = DefaultGreyfishAgentImpl.builder(mock(Population.class)).addTraits(gene).build();

        // when
        AgentTrait<DefaultGreyfishAgent, Object> ret = (AgentTrait<DefaultGreyfishAgent, Object>) agent.getTrait("foo");

        // then
        verify(gene).setAgent(agent);
        assertThat(ret, is(equalTo(gene)));
    }

    @Test
    public void testDeepClone() throws Exception {
        // given
        final DefaultGreyfishAgentImpl agent = DefaultGreyfishAgentImpl.builder(mock(Population.class))
                .addAction(when(mock(AgentAction.class).deepClone(any(DeepCloner.class))).thenReturn(mock(AgentAction.class)).<AgentAction<DefaultGreyfishAgent>>getMock())
                .addProperties(when(mock(AgentProperty.class).deepClone(any(DeepCloner.class))).thenReturn(mock(AgentProperty.class)).<AgentProperty<DefaultGreyfishAgent, Object>>getMock())
                .addTraits(when(mock(AgentTrait.class).deepClone(any(DeepCloner.class))).thenReturn(mock(AgentTrait.class)).<AgentTrait<DefaultGreyfishAgent, Object>>getMock())
                .build();

        // when
        final DefaultGreyfishAgentImpl clone = CycleCloner.clone(agent);

        // then
        assertThat(clone, isSameAs(agent));
    }

    @Test
    public void testSerialization() throws Exception {
        // given
        final DefaultGreyfishAgentImpl frozenAgent = DefaultGreyfishAgentImpl.builder(Population.named("foo"))
                .addAction(mock(AgentAction.class, withSettings().serializable()))
                .addProperties(mock(AgentProperty.class, withSettings().serializable()))
                .addTraits(mock(AgentTrait.class, withSettings().serializable()))
                .build();

        // when
        final DefaultGreyfishAgentImpl copy = Persisters.createCopy(frozenAgent, Persisters.javaSerialization());

        // then
        assertThat(copy, isSameAs(frozenAgent));
    }

    private static Matcher<? super DefaultGreyfishAgentImpl> isSameAs(DefaultGreyfishAgentImpl agent) {
        return Matchers.<DefaultGreyfishAgentImpl>allOf(
                has("population " + agent.getPopulation(),
                        new Function<DefaultGreyfishAgentImpl, Population>() {
                            @Override
                            public Population apply(DefaultGreyfishAgentImpl frozenAgent) {
                                return frozenAgent.getPopulation();
                            }
                        },
                        is(equalTo(agent.getPopulation()))),
                has(agent.getActions().size() + " actions", new Function<DefaultGreyfishAgentImpl, FunctionalList<AgentAction<DefaultGreyfishAgent>>>() {
                    @Override
                    public FunctionalList<AgentAction<DefaultGreyfishAgent>> apply(DefaultGreyfishAgentImpl frozenAgent) {
                        return frozenAgent.getActions();
                    }
                }, hasSize(agent.getActions().size())),
                has(agent.getProperties().size() + " properties", new Function<DefaultGreyfishAgentImpl, FunctionalList<AgentProperty<DefaultGreyfishAgent, ?>>>() {
                    @Override
                    public FunctionalList<AgentProperty<DefaultGreyfishAgent, ?>> apply(DefaultGreyfishAgentImpl frozenAgent) {
                        return frozenAgent.getProperties();
                    }
                }, hasSize(agent.getProperties().size())),
                has(agent.getTraits().size() + " traits", new Function<DefaultGreyfishAgentImpl, FunctionalList<AgentTrait<DefaultGreyfishAgent, ?>>>() {
                    @Override
                    public FunctionalList<AgentTrait<DefaultGreyfishAgent, ?>> apply(DefaultGreyfishAgentImpl frozenAgent) {
                        return frozenAgent.getTraits();
                    }
                }, hasSize(agent.getTraits().size()))
        );
    }
}
