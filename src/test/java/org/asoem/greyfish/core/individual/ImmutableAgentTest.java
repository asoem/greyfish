package org.asoem.greyfish.core.individual;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.ImmutableGenome;
import org.asoem.greyfish.core.properties.GFProperty;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
    @Mock Population population;

    @Test(expected = UnsupportedOperationException.class)
    public void testAddGene() throws Exception {
        // given
        ImmutableAgent agent = ImmutableAgent.of(population).build();

        // when
        agent.addGene(gene);

        // then
        assertTrue(false);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddAction() throws Exception {
        // given
        ImmutableAgent agent = ImmutableAgent.of(population).build();

        // when
        agent.addAction(action);

        // then
        assertTrue(false);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddProperty() throws Exception {
        // given
        ImmutableAgent agent = ImmutableAgent.of(population).build();

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
        ImmutableAgent agent = ImmutableAgent.of(population).addGenes(gene).build();

        // when
        Gene ret = agent.getGene("foo", Gene.class);

        // then
        verify(gene).setAgent(agent);
        assertThat(ret).isEqualTo(gene);
    }

    @Test
    public void testCloneOf() {
        // given
        Agent agent = ImmutableAgent.of(population).build();

        // when
        ImmutableAgent clone = ImmutableAgent.cloneOf(agent);

        // then
        assertThat(clone.getComponents()).hasSize(Iterables.size(agent.getComponents()));
    }

    @Test
    public void testInjectGamete() throws Exception {
        // given
        Gene<?> mutatedGene = mock(Gene.class);

        given(gene.getName()).willReturn("foo");
        given(gene.hasName("foo")).willReturn(true);
        given(gene.isMutatedCopy(mutatedGene)).willReturn(true);

        given(mutatedGene.getName()).willReturn("foo");
        given(mutatedGene.hasName("foo")).willReturn(true);
        given(mutatedGene.isMutatedCopy(gene)).willReturn(true);

        ImmutableGenome<? extends Gene<?>> genome = ImmutableGenome.copyOf(Collections.singleton(mutatedGene));

        ImmutableAgent agent = ImmutableAgent.of(population).addGenes(gene).build();

        // when
        agent.injectGamete(genome);

        // then
        assertThat(agent.getGene("foo", Gene.class)).isEqualTo(mutatedGene);
    }
}
