package org.asoem.greyfish.core.agent;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.persistence.Persister;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * User: christoph
 * Date: 17.09.11
 * Time: 12:00
 */
@RunWith(MockitoJUnitRunner.class)
public class MutableAgentTest {

    @Inject
    private Persister persister;

    public MutableAgentTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }
    
    @Test
    public void testAddGene() throws Exception {
        // given
        final AgentTrait gene = mock(AgentTrait.class);
        MutableAgent agent = MutableAgent.builder(mock(Population.class)).build();

        // when
        boolean ret = agent.addTrait(gene);

        // then
        assertTrue(ret);
        verify(gene).setAgent(agent);
    }

    @Test
    public void testGetGene() throws Exception {
        // given
        final AgentTrait gene = mock(AgentTrait.class);
        MutableAgent agent = MutableAgent.builder(mock(Population.class)).build();
        given(gene.getName()).willReturn("foo");
        given(gene.children()).willReturn(Collections.<AgentNode>emptyList());
        agent.addTrait(gene);

        // when
        AgentTrait ret = agent.getTrait("foo");

        // then
        assertThat(ret, is(equalTo(gene)));
    }

    @Test
    public void testBasicPersistence() throws Exception {
        /*
        // given
        final Population population = Population.newPopulation("Test", Color.green);
        final Agent agent = MutableAgent.of(population).build();

        // when
        final Agent deserializedAgent = Persisters.createCopy(agent, FrozenAgent.class, persister);

        // then
        assertThat(deserializedAgent.getPopulation()).isEqualTo(population);
        assertThat(deserializedAgent.getBody()).isNotNull();
        */
    }
}
