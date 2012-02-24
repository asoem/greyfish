package org.asoem.greyfish.core.individual;

import com.google.inject.Inject;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.awt.*;
import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;
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
        CoreInjectorHolder.coreInjector().injectMembers(this);
    }
    
    @Test
    public void testAddGene() throws Exception {
        // given
        final Gene gene = mock(Gene.class);
        MutableAgent agent = MutableAgent.of(mock(Population.class)).build();

        // when
        boolean ret = agent.addGene(gene);

        // then
        assertTrue(ret);
        verify(gene).setAgent(agent);
    }

    @Test
    public void testGetGene() throws Exception {
        // given
        final Gene gene = mock(Gene.class);
        MutableAgent agent = MutableAgent.of(mock(Population.class)).build();
        given(gene.getName()).willReturn("foo");
        given(gene.hasName("foo")).willReturn(true);
        given(gene.children()).willReturn(Collections.<AgentComponent>emptyList());
        agent.addGene(gene);

        // when
        Gene ret = agent.getGene("foo", Gene.class);

        // then
        assertThat(ret).isEqualTo(gene);
    }

    @Test
    public void testBasicPersistence() throws Exception {
        // given
        final Population population = Population.newPopulation("Test", Color.green);
        final Agent agent = MutableAgent.of(population).build();

        // when
        final Agent deserializedAgent = Persisters.createCopy(agent, ImmutableAgent.class, persister);

        // then
        assertThat(deserializedAgent.getPopulation()).isEqualTo(population);
        assertThat(deserializedAgent.getBody()).isNotNull();
    }
}
