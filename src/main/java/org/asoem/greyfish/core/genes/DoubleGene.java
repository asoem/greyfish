package org.asoem.sico.core.genes;

import org.asoem.sico.utils.RandomUtils;
import org.uncommons.maths.random.GaussianGenerator;

public class DoubleGene extends AbstractGene<Double> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7694823132081940077L;

	private double min;
	
	private double max;
	
	private static final GaussianGenerator GUSSIAN_GENERATOR = new GaussianGenerator(0, 3, RandomUtils.RNG);

	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		this.max = max;
	}

	public DoubleGene() {
		super(0.0);
	}

	public DoubleGene(Double d, Double min, Double max) {
		super(d);
	}

	public DoubleGene(DoubleGene gene) {
		this(gene.representation, gene.min, gene.max);
	}

	@Override
	public void mutate() {
		representation += GUSSIAN_GENERATOR.nextValue();
		representation = Math.max(min, Math.min(max, representation));
	}

	@Override
	public DoubleGene clone() {
		return new DoubleGene(this);
	}

	@Override
	public void initialize() {
		representation = RandomUtils.nextDouble(min, max); // TODO max is exclusive in func. but inclusive as field
	}
}
