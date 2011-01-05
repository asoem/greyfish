package org.asoem.sico.core.properties;

import java.util.Map;

import org.asoem.sico.core.genes.Gene;
import org.asoem.sico.core.individual.AbstractGFComponent;
import org.asoem.sico.lang.Functor;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;
import org.asoem.sico.utils.ListenerSupport;
import org.simpleframework.xml.Root;

import com.google.common.collect.ObjectArrays;

@Root
public abstract class AbstractGFProperty extends AbstractGFComponent implements GFProperty {

	private final ListenerSupport<GFPropertyChangeListener> listenerSupport = new ListenerSupport<GFPropertyChangeListener>();
	
	private static final long serialVersionUID = -2745483694884432599L;

	private static final Gene<?>[] NULL_GENES = new Gene[0];
	private Gene<?>[] genes = NULL_GENES;

	public AbstractGFProperty() {
	}

	protected AbstractGFProperty(AbstractGFProperty property,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(property, mapDict);
	}

	@Override
	public void mutate() {}

	@Override
	public final Gene<?>[] getGenes() {
		return genes;
	}
	
	@Override
	public void export(Exporter e) {
	}
	
	public void addGFPropertyChangeListener(GFPropertyChangeListener listener) {
		listenerSupport.addListener(listener);
	}
	
	public final <R extends Gene<?>> R registerGene(final R gene) {
		if (this.genes == NULL_GENES)
			this.genes = new Gene<?>[] {gene};
		else
			ObjectArrays.concat(genes, gene);
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
}
