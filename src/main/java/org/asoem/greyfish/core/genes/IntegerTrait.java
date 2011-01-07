package org.asoem.greyfish.core.genes;

public class IntegerTrait extends AbstractTrait<Integer> {

	private IntegerGene gene;

	public IntegerTrait(String name, Integer d, Integer min, Integer max) {
		super(name);
		gene = new IntegerGene(d, min, max);
	}

	@Override
	public Integer getValue() {
		return gene.getRepresentation();
	}

	@Override
	public AbstractGene<?>[] getGenes() {
		return new IntegerGene[] {gene};
	}

	public double getMax() {
		return gene.getMax();
	}

	public double getMin() {
		return gene.getMin();
	}

	public void setMax(Integer max) {
		gene.setMax(max);
	}

	public void setMin(Integer min) {
		gene.setMin(min);
	}

	@Override
	public void setValue(Integer value) {
		gene.setRepresentation(value);
	}
}
