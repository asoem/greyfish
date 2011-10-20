package org.asoem.greyfish.core.properties;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.genes.ForwardingGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.ImmutableGene;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Root;

import java.util.Collections;
import java.util.List;

@Root
public abstract class AbstractGFProperty extends AbstractAgentComponent implements GFProperty {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGFProperty.class);
    private List<ForwardingGene<?>> geneList = ImmutableList.of();

    @Override
    public Iterable<Gene<?>> getGenes() {
        return Iterables.transform(geneList, new Function<ForwardingGene<?>, Gene<?>>() {
            @Override
            public Gene<?> apply(ForwardingGene<?> forwardingGene) {
                return forwardingGene.getDelegate();
            }
        });
    }

    /**
     * Set the delegates of the contained {@code IndexedGene}s and their index to the values provided by the given {@code geneIterator}
     *
     * @param genes A {@code Genome}'s geneList ListIterator which provides the delegate genes
     * @see org.asoem.greyfish.core.genes.ImmutableGenome#listIterator()
     */
    public void setGenes(final Iterable<? extends Gene<?>> genes) {
        for (final ForwardingGene<?> gene : geneList) {
            Gene<?> copy = Iterables.find(genes, new Predicate<Gene<?>>() {
                        @Override
                        public boolean apply(Gene<?> o) {
                            return gene.isMutatedCopy(o);
                        }
                    }, null);

            if (copy != null) {
                gene.setDelegate(copy);
            }
            else {
                LOGGER.error("No mutated copy for " + gene + " in " + genes);
            }
        }
    }

    @Override
    public void configure(ConfigurationHandler e) {
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);

        if (simulation.getSteps() == 0)
            for (ForwardingGene<?> gene : geneList) {
                try {
                    gene.setDelegate(ImmutableGene.newInitializedCopy(gene));
                }
                catch (Exception e) {
                    LoggerFactory.getLogger(AbstractGFProperty.class).warn("Could initialize gene.", e);
                }
            }
    }

    @Override
    public final <S> Gene<S> registerGene(final Gene<S> gene) {
        checkNotFrozen();

        final ForwardingGene<S> ret = ForwardingGene.newInstance(gene);
        geneList = ImmutableList.<ForwardingGene<?>>builder().addAll(geneList).add( ret ).build();
        return ret;
    }

    protected AbstractGFProperty(AbstractBuilder<? extends AbstractGFProperty, ? extends AbstractBuilder> builder) {
        super(builder);
    }

    protected AbstractGFProperty(AbstractAgentComponent cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Iterable<AgentComponent> children() {
        return Collections.emptyList();
    }

    protected static abstract class AbstractBuilder<E extends AbstractGFProperty, T extends AbstractBuilder<E, T>> extends AbstractAgentComponent.AbstractBuilder<E, T> {}
}
