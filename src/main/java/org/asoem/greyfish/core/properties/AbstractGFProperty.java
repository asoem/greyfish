package org.asoem.greyfish.core.properties;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.GeneProxy;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ListenerSupport;
import org.simpleframework.xml.Root;

import java.util.List;
import java.util.ListIterator;

@Root
public abstract class AbstractGFProperty extends AbstractGFComponent implements GFProperty {

    private final ListenerSupport<GFPropertyChangeListener> listenerSupport = ListenerSupport.newInstance();

    private List<GeneProxy<?>> geneList = ImmutableList.of();

    @Override
    public void mutate() {}

    @Override
    public Iterable<Gene<?>> getGenes() {
        return Iterables.transform(geneList, new Function<GeneProxy<?>, Gene<?>>() {
            @Override
            public Gene<?> apply(GeneProxy o) {
                return o.getGene();
            }
        });
    }

    @Override
    public void setGenes(ListIterator<Gene<?>> geneIterator) {
        for (GeneProxy<?> proxy : geneList) {
            if (!geneIterator.hasNext())
                throw new AssertionError("geneIterator cannot provide elements as needed");
            proxy.setIndex(geneIterator.nextIndex());
            proxy.setGene(geneIterator.next());
        }
    }

    @Override
    public void export(Exporter e) {
    }

    public void addGFPropertyChangeListener(GFPropertyChangeListener listener) {
        listenerSupport.addListener(listener);
    }

    protected final <S> Supplier<S> registerGene(final Gene<S> gene) {
        checkNotFrozen();

        final GeneProxy<S> ret = GeneProxy.newInstance(gene);
        geneList = ImmutableList.<GeneProxy<?>>builder().addAll(geneList).add( ret ).build();
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
