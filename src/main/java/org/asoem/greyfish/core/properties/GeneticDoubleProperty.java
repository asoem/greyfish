package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.DoubleGene;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.Exporter;

@ClassGroup(tags="property")
public class GeneticDoubleProperty extends DoubleProperty {

	private DoubleGene gene = new DoubleGene();

    protected GeneticDoubleProperty(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    @Override
	public void add(Double val) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void subtract(double val) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e);
	}
}
