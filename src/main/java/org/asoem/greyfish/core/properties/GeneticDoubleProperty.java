package org.asoem.sico.core.properties;

import org.asoem.sico.core.genes.DoubleGene;
import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.Exporter;

@ClassGroup(tags="property")
public class GeneticDoubleProperty extends DoubleProperty {

	private DoubleGene gene;
	
	@Override
	public void add(Double double1) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void substract(double costs) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e);
	}
	
	@Override
	public void setUpperBound(Double upperBound) {
		gene.setMax(upperBound);
	}
	
	@Override
	public void setLowerBound(Double lowerBound) {
		gene.setMin(lowerBound);
	}
	
	@Override
	public void setInitialValue(Double initialValue) {
		gene.setRepresentation(initialValue);
	}
}
