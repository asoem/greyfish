package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.genes.Gene;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * User: christoph
 * Date: 17.09.11
 * Time: 12:00
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultAgentTest {

    @Mock Gene<?> gene;

    @Test
    public void testAddGene() throws Exception {
        // given
        given(gene.getName()).willReturn("foo");

        // when
        DefaultAgent agent = DefaultAgent.with().build();
        agent.addGene(gene);

        // then
        assertEquals(gene, agent.getGene("foo", Gene.class));
        verify(gene).setAgent(agent);
    }
}
