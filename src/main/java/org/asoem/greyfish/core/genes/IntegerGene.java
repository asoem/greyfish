package org.asoem.sico.core.genes;

import org.asoem.sico.utils.RandomUtils;
import org.uncommons.maths.random.GaussianGenerator;

public class IntegerGene extends AbstractGene<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8444774563094338923L;

	private Integer max;

	private Integer min;
	
	private static final GaussianGenerator GUSSIAN_GENERATOR = new GaussianGenerator(0, 3, RandomUtils.RNG);

	public IntegerGene(int init, int min, int max) {
		super(init);
		this.min = min;
		this.max = max;
	}

	public IntegerGene(IntegerGene integerGene) {
		super(integerGene.representation);
		this.min = integerGene.min;
		this.max = integerGene.max;
	}

	@Override
	public IntegerGene clone() {
		return new IntegerGene(this);
	}

	public Integer getMax() {
		return max;
	}

	public Integer getMin() {
		return min;
	}

	@Override
	public void mutate() {
		representation += (int) Math.floor(GUSSIAN_GENERATOR.nextValue());
		representation = Math.max(min, Math.min(max, representation));
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public void setMin(Integer min) {
		this.min = min;
	}

	@Override
	public void initialize() {
		representation = RandomUtils.nextInt(min, max+1);
	}
}
