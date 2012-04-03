package org.asoem.greyfish.core.individual;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.actions.NullAction;
import org.asoem.greyfish.core.genes.*;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.awt.*;
import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;
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
        ImmutableAgent agent = ImmutableAgent.of(population).addGenes(gene).build();

        // when
        Gene ret = agent.getGene("foo", Gene.class);

        // then
        verify(gene).setAgent(agent);
        assertThat(ret).isEqualTo(gene);
    }

    @Test
    public void testDeepClone() throws Exception {
        // given
        ImmutableAgent agent = ImmutableAgent.of(population).build();
        agent.setPopulation(mock(Population.class));
        agent.setProjection(mock(Object2D.class));
        agent.setMotion(mock(Motion2D.class));

        // when
        ImmutableAgent clone = DeepCloner.clone(agent, ImmutableAgent.class);

        // then
        assertThat(clone).isEqualTo(agent);
    }

    @Test
    public void testCloneOf() {
        // given
        ImmutableAgent agent = ImmutableAgent.of(population).build();
        agent.setPopulation(mock(Population.class));
        agent.setProjection(mock(Object2D.class));
        agent.setMotion(mock(Motion2D.class));

        // when
        ImmutableAgent clone = ImmutableAgent.cloneOf(agent);

        // then
        assertThat(clone).isEqualTo(agent);
    }

    @Test
    public void testInjectGamete() throws Exception {
        // given
        final GeneControllerAdaptor<String> geneController = new GeneControllerAdaptor<String>();
        Gene<String> gene1 = new MutableGene<String>("Foo", String.class, geneController);
        Gene<String> gene2 = ImmutableGene.of("DefaultName", "Bar", String.class, geneController);
        Agent agent = ImmutableAgent.of(population).addGenes(gene1).build();

        Chromosome<Gene<String>> chromosome = ImmutableChromosome.copyOf(Collections.singleton(gene2));

        // when
        agent.injectGamete(chromosome);

        // then
        assertThat(gene1.get()).isEqualTo(gene2.get());
    }

    @Test
    public void testBasicPersistence() throws Exception {
        // given
        final Population population = Population.newPopulation("Test", Color.green);
        final Agent agent = ImmutableAgent.of(population).build();

        // when
        final Agent copy = Persisters.createCopy(agent, ImmutableAgent.class, persister);

        // then
        assertThat(copy).isEqualTo(agent);
    }

    @Test
    public void testPersistenceWithActions() throws Exception {
        // given
        final Population population = Population.newPopulation("Test", Color.green);
        final GFAction action = new NullAction();
        final Agent agent = ImmutableAgent.of(population).addActions(action).build();

        // when
        final Agent copy = Persisters.createCopy(agent, ImmutableAgent.class, persister);

        // then
        assertThat(copy).isEqualTo(agent);
    }

    @Test
    public void testPersistenceWithProperties() throws Exception {
        // given
        final Population population = Population.newPopulation("Test", Color.green);
        final GFProperty property = new DoubleProperty();
        final Agent agent = ImmutableAgent.of(population).addProperties(property).build();

        // when
        final Agent copy = Persisters.createCopy(agent, ImmutableAgent.class, persister);

        // then
        assertThat(copy).isEqualTo(agent);
    }

    @Test
    public void testPersistenceWithGenes() throws Exception {
        // given
        final Population population = Population.newPopulation("Test", Color.green);
        final Gene<?> gene = new DoubleGene();
        final Agent agent = ImmutableAgent.of(population).addGenes(gene).build();

        // when
        final Agent copy = Persisters.createCopy(agent, ImmutableAgent.class, persister);

        // then
        assertThat(copy).isEqualTo(agent);
    }
}
