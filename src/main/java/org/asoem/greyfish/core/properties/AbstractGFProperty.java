package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.IndexedGene;
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

    private List<IndexedGene<?>> geneList = ImmutableList.of();

    @Override
    public Iterable<IndexedGene<?>> getGenes() {
        return geneList;
    }

    @Override
    public void setGenes(ListIterator<? extends Gene<?>> geneIterator) {
        for (IndexedGene<?> proxy : geneList) {
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

    @Override
    public final <S> Supplier<S> registerGene(final Gene<S> gene) {
        checkNotFrozen();

        final IndexedGene<S> ret = IndexedGene.newInstance(gene);
        ret.setIndex(geneList.size()-1);
        geneList = ImmutableList.<IndexedGene<?>>builder().addAll(geneList).add( ret ).build();
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
