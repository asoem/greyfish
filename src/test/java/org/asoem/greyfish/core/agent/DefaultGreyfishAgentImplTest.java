package org.asoem.greyfish.core.agent;

import com.google.common.base.Function;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.SearchableList;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

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
        given(gene.childConditions()).willReturn(Collections.<AgentNode>emptyList());
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
        final DeepCloner clonerMock = mock(DeepCloner.class);
        given(clonerMock.getClone(any(DeepCloneable.class), any(Class.class))).willAnswer(new Answer<DeepCloneable>() {
            @Override
            public DeepCloneable answer(InvocationOnMock invocation) throws Throwable {
                return (DeepCloneable) invocation.getArguments()[0];
            }
        });
        final DefaultGreyfishAgentImpl agent = DefaultGreyfishAgentImpl.builder(mock(Population.class))
                .addAction(mock(AgentAction.class))
                .addProperties(mock(AgentProperty.class))
                .addTraits(mock(AgentTrait.class))
                .build();

        // when
        final DefaultGreyfishAgentImpl clone = agent.deepClone(clonerMock);

        // then
        assertThat(clone, isSameAs(agent));
        verify(clonerMock, times(3)).getClone(any(DeepCloneable.class), any(Class.class));
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
        final DefaultGreyfishAgentImpl copy = Persisters.createCopy(frozenAgent, JavaPersister.INSTANCE);

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
                has(agent.getActions().size() + " actions", new Function<DefaultGreyfishAgentImpl, SearchableList<AgentAction<DefaultGreyfishAgent>>>() {
                    @Override
                    public SearchableList<AgentAction<DefaultGreyfishAgent>> apply(DefaultGreyfishAgentImpl frozenAgent) {
                        return frozenAgent.getActions();
                    }
                }, hasSize(agent.getActions().size())),
                has(agent.getProperties().size() + " properties", new Function<DefaultGreyfishAgentImpl, SearchableList<AgentProperty<DefaultGreyfishAgent, ?>>>() {
                    @Override
                    public SearchableList<AgentProperty<DefaultGreyfishAgent, ?>> apply(DefaultGreyfishAgentImpl frozenAgent) {
                        return frozenAgent.getProperties();
                    }
                }, hasSize(agent.getProperties().size())),
                has(agent.getTraits().size() + " traits", new Function<DefaultGreyfishAgentImpl, SearchableList<AgentTrait<DefaultGreyfishAgent, ?>>>() {
                    @Override
                    public SearchableList<AgentTrait<DefaultGreyfishAgent, ?>> apply(DefaultGreyfishAgentImpl frozenAgent) {
                        return frozenAgent.getTraits();
                    }
                }, hasSize(agent.getTraits().size()))
        );
    }
}
