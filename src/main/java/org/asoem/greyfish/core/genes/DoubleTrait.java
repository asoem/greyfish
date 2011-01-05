package org.asoem.sico.core.genes;

public class DoubleTrait extends AbstractTrait<Double> {

	private DoubleGene gene = new DoubleGene();

	public DoubleTrait() {
	}

	public DoubleTrait(String name, Double d, Double min, Double max) {
		super(name);
		gene.setMin(min);
		gene.setMax(max);
		gene.setRepresentation(d);
	}

	@Override
	public Double getValue() {
		return gene.getRepresentation();
	}

	@Override
	public AbstractGene<?>[] getGenes() {
		return new DoubleGene[] {gene};
	}

	public double getMax() {
		return gene.getMax();
	}

	public double getMin() {
		return gene.getMin();
	}

	public void setMax(Double max) {
		gene.setMax(max);
	}

	public void setMin(Double min) {
		gene.setMin(min);
	}

	@Override
	public void setValue(Double value) {
		gene.setRepresentation(value);
	}
}
