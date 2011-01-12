package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.DoubleGene;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.Exporter;

@ClassGroup(tags="property")
public class GeneticDoubleProperty extends DoubleProperty {

	private DoubleGene gene;
	
	@Override
	public void add(Double double1) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void subtract(double costs) {
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
