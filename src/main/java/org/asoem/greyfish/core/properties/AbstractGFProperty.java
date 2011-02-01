package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ListenerSupport;
import org.simpleframework.xml.Root;

@Root
public abstract class AbstractGFProperty extends AbstractGFComponent implements GFProperty {

	private final ListenerSupport<GFPropertyChangeListener> listenerSupport = ListenerSupport.newInstance();

    private ImmutableList<Gene<?>> geneList = ImmutableList.of();

    @Override
	public void mutate() {}

    public ImmutableList<Gene<?>> getGeneList() {
        return geneList;
    }

    @Override
	public void export(Exporter e) {
	}
	
	public void addGFPropertyChangeListener(GFPropertyChangeListener listener) {
		listenerSupport.addListener(listener);
	}
	
	protected final <S> Supplier<S> registerGene(final Gene<S> gene, final Class<S> clazz) {
        checkNotFrozen();
        geneList = new ImmutableList.Builder<Gene<?>>().addAll(geneList).add(gene).build();
		final int index = geneList.size();
        return new Supplier<S>() {

            @Override
            public S get() {
                return clazz.cast(geneList.get(index).getRepresentation());
            }
        };
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

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        ImmutableList.Builder builder = ImmutableList.builder();

        for (Gene<?> gene : geneList) {
            final int index = componentOwner.getGenome().size();
            componentOwner.getGenome().add(gene);
            builder.add(new GeneDecorator(gene) {
                @Override public Object getRepresentation() {
                     return Iterables.get(componentOwner.getGenome(), index).getRepresentation();
                }
            });
        }

        geneList = builder.build();
    }

    protected AbstractGFProperty(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    protected AbstractGFProperty(AbstractGFComponent clonable, CloneMap map) {
        super(clonable, map);
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFComponent.AbstractBuilder<T> {}
}
