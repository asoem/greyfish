package org.asoem.greyfish.core.properties;

import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ListenerSupport;
import org.simpleframework.xml.Root;

import java.util.Map;

@Root
public abstract class AbstractGFProperty extends AbstractGFComponent implements GFProperty {

	private final ListenerSupport<GFPropertyChangeListener> listenerSupport = new ListenerSupport<GFPropertyChangeListener>();

    private ImmutableList<? extends Gene<?>> geneList = ImmutableList.of();

    @Override
	public void mutate() {}

    public ImmutableList<? extends Gene<?>> getGeneList() {
        return geneList;
    }

    @Override
	public void export(Exporter e) {
	}
	
	public void addGFPropertyChangeListener(GFPropertyChangeListener listener) {
		listenerSupport.addListener(listener);
	}
	
	public final <R extends Gene<?>> R registerGene(final R gene) {
        checkFrozen();
        geneList = new ImmutableList.Builder<Gene<?>>().addAll(geneList).add(gene).build();
		return gene;
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

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFComponent.AbstractBuilder<T> {
        protected T fromClone(AbstractGFProperty action, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(action, mapDict);
            return self();
        }
    }
}
