package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.properties.GFProperty;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.*;
/**
 * User: christoph
 * Date: 20.09.11
 * Time: 17:54
 */
@RunWith(MockitoJUnitRunner.class)
public class ImmutableAgentTest {

    @Mock Gene<?> gene;
    @Mock GFAction action;
    @Mock GFProperty property;

    @Test(expected = UnsupportedOperationException.class)
    public void testAddGene() throws Exception {
        // given
        ImmutableAgent agent = ImmutableAgent.with().build();

        // when
        agent.addGene(gene);

        // then
        assertTrue(false);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddAction() throws Exception {
        // given
        ImmutableAgent agent = ImmutableAgent.with().build();

        // when
        agent.addAction(action);

        // then
        assertTrue(false);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddProperty() throws Exception {
        // given
        ImmutableAgent agent = ImmutableAgent.with().build();

        // when
        agent.addProperty(property);

        // then
        assertTrue(false);
    }

    @Test
    public void testGetGene() throws Exception {
        // given
        given(gene.getName()).willReturn("foo");
        given(gene.hasName("foo")).willReturn(true);
        ImmutableAgent agent = ImmutableAgent.with().addGenes(gene).build();

        // when
        Gene ret = agent.getGene("foo", Gene.class);

        // then
        verify(gene).setAgent(agent);
        assertEquals(gene, ret);
    }
}
