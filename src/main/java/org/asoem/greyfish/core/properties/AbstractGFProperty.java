package org.asoem.greyfish.core.properties;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.genes.DefaultGene;
import org.asoem.greyfish.core.genes.ForwardingGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.simpleframework.xml.Root;

import java.util.List;

@Root
public abstract class AbstractGFProperty extends AbstractGFComponent implements GFProperty {

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

    @Override
    public void setGenes(final Iterable<? extends Gene<?>> genes) {
        for (final ForwardingGene<?> gene : geneList) {
            Gene<?> copy = Iterables.find(genes, new Predicate<Gene<?>>() {
                        @Override
                        public boolean apply(Gene<?> o) {
                            return gene.isMutatedCopyOf(o);
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
    public void export(Exporter e) {
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);

        if (simulation.getSteps() == 0)
            for (ForwardingGene<?> gene : geneList) {
                try {
                    gene.setDelegate(DefaultGene.newInitializedCopy(gene));
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

    protected AbstractGFProperty(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    protected AbstractGFProperty(AbstractGFComponent clonable, CloneMap map) {
        super(clonable, map);
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFComponent.AbstractBuilder<T> {}
}
