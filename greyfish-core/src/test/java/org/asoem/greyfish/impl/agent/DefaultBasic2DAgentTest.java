package org.asoem.greyfish.impl.agent;

import com.google.common.base.Function;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.core.agent.PrototypeGroup;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.traits.AgentTrait;
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

import static org.asoem.greyfish.core.test.GreyfishMatchers.has;
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
public class DefaultBasic2DAgentTest {

    @Test
    public void testGetGene() throws Exception {
        // given
        final AgentTrait<Basic2DAgent, Object> gene = mock(AgentTrait.class);
        given(gene.getName()).willReturn("foo");
        given(gene.children()).willReturn(Collections.<AgentNode>emptyList());
        final DefaultBasic2DAgent agent = DefaultBasic2DAgent.builder(mock(PrototypeGroup.class)).addTraits(gene).build();

        // when
        final AgentTrait<Basic2DAgent, Object> ret = (AgentTrait<Basic2DAgent, Object>) agent.getTrait("foo");

        // then
        verify(gene).setAgent(agent);
        assertThat(ret, is(equalTo(gene)));
    }

    @Test
    public void testDeepClone() throws Exception {
        // given
        final DefaultBasic2DAgent agent = DefaultBasic2DAgent.builder(mock(PrototypeGroup.class))
                .addAction(when(mock(AgentAction.class).deepClone(any(DeepCloner.class))).thenReturn(mock(AgentAction.class)).<AgentAction<Basic2DAgent>>getMock())
                .addProperties(when(mock(AgentProperty.class).deepClone(any(DeepCloner.class))).thenReturn(mock(AgentProperty.class)).<AgentProperty<Basic2DAgent, Object>>getMock())
                .addTraits(when(mock(AgentTrait.class).deepClone(any(DeepCloner.class))).thenReturn(mock(AgentTrait.class)).<AgentTrait<Basic2DAgent, Object>>getMock())
                .build();

        // when
        final DefaultBasic2DAgent clone = CycleCloner.clone(agent);

        // then
        assertThat(clone, isSameAs(agent));
    }

    @Test
    public void testSerialization() throws Exception {
        // given
        final DefaultBasic2DAgent frozenAgent = DefaultBasic2DAgent.builder(PrototypeGroup.named("foo"))
                .addAction(mock(AgentAction.class, withSettings().serializable()))
                .addProperties(mock(AgentProperty.class, withSettings().serializable()))
                .addTraits(mock(AgentTrait.class, withSettings().serializable()))
                .build();

        // when
        final DefaultBasic2DAgent copy = Persisters.copyAsync(frozenAgent, Persisters.javaSerialization());

        // then
        assertThat(copy, isSameAs(frozenAgent));
    }

    private static Matcher<? super DefaultBasic2DAgent> isSameAs(final DefaultBasic2DAgent agent) {
        return Matchers.<DefaultBasic2DAgent>allOf(
                has("population " + agent.getPrototypeGroup(),
                        new Function<DefaultBasic2DAgent, PrototypeGroup>() {
                            @Override
                            public PrototypeGroup apply(final DefaultBasic2DAgent frozenAgent) {
                                return frozenAgent.getPrototypeGroup();
                            }
                        },
                        is(equalTo(agent.getPrototypeGroup()))),
                has(agent.getActions().size() + " actions", new Function<DefaultBasic2DAgent, FunctionalList<AgentAction<Basic2DAgent>>>() {
                    @Override
                    public FunctionalList<AgentAction<Basic2DAgent>> apply(final DefaultBasic2DAgent frozenAgent) {
                        return frozenAgent.getActions();
                    }
                }, hasSize(agent.getActions().size())),
                has(agent.getProperties().size() + " properties", new Function<DefaultBasic2DAgent, FunctionalList<AgentProperty<Basic2DAgent, ?>>>() {
                    @Override
                    public FunctionalList<AgentProperty<Basic2DAgent, ?>> apply(final DefaultBasic2DAgent frozenAgent) {
                        return frozenAgent.getProperties();
                    }
                }, hasSize(agent.getProperties().size())),
                has(agent.getTraits().size() + " traits", new Function<DefaultBasic2DAgent, FunctionalList<AgentTrait<Basic2DAgent, ?>>>() {
                    @Override
                    public FunctionalList<AgentTrait<Basic2DAgent, ?>> apply(final DefaultBasic2DAgent frozenAgent) {
                        return frozenAgent.getTraits();
                    }
                }, hasSize(agent.getTraits().size()))
        );
    }
}
