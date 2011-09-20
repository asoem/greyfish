package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.genes.Gene;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * User: christoph
 * Date: 17.09.11
 * Time: 12:00
 */
@RunWith(MockitoJUnitRunner.class)
public class MutableAgentTest {

    @Mock Gene<?> gene;

    @Test
    public void testAddGene() throws Exception {
        // given
        MutableAgent agent = new MutableAgent();

        // when
        boolean ret = agent.addGene(gene);

        // then
        assertTrue(ret);
        verify(gene).setAgent(agent);
    }

    @Test
    public void testGetGene() throws Exception {
        // given
        MutableAgent agent = new MutableAgent();
        given(gene.getName()).willReturn("foo");
        given(gene.hasName("foo")).willReturn(true);
        agent.addGene(gene);

        // when
        Gene ret = agent.getGene("foo", Gene.class);

        // then
        assertEquals(gene, ret);
    }
}
