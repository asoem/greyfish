package org.asoem.greyfish.core.properties;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.genes.ForwardingGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ListenerSupport;
import org.simpleframework.xml.Root;

import java.util.List;

@Root
public abstract class AbstractGFProperty extends AbstractGFComponent implements GFProperty {

    private final ListenerSupport<GFPropertyChangeListener> listenerSupport = ListenerSupport.newInstance();

    private List<ForwardingGene<?>> geneList = ImmutableList.of();

    @Override
    public Iterable<ForwardingGene<?>> getGenes() {
        return geneList;
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
                GreyfishLogger.error("No mutated copy for " + gene + " in " + genes);
            }
        }
    }

    @Override
    public void export(Exporter e) {
    }

    public void addGFPropertyChangeListener(GFPropertyChangeListener listener) {
        listenerSupport.addListener(listener);
    }

    @Override
    public final <S> Gene<S> registerGene(final Gene<S> gene) {
        checkNotFrozen();

        final ForwardingGene<S> ret = ForwardingGene.newInstance(gene);
        geneList = ImmutableList.<ForwardingGene<?>>builder().addAll(geneList).add( ret ).build();
        return ret;
    }

    public void removeGFPropertyChangeListener(GFPropertyChangeListener listener) {
        listenerSupport.removeListener(listener);
    }

    protected void firePropertyChanged() {
        listenerSupport.notifyListeners(new Functor<GFPropertyChangeListener>() {

            @Override
            public void update(GFPropertyChangeListener listener) {
                listener.propertyChanged(AbstractGFProperty.this);
            }
        });
    }

    protected AbstractGFProperty(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    protected AbstractGFProperty(AbstractGFComponent clonable, CloneMap map) {
        super(clonable, map);
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFComponent.AbstractBuilder<T> {}
}
