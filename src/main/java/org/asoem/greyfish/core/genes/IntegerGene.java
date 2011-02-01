package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.RandomUtils;
import org.uncommons.maths.random.GaussianGenerator;

public class IntegerGene extends AbstractGene<Integer> {

	private int max;

	private int min;
	
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

    public IntegerGene(IntegerGene integerGene, CloneMap map) {
        super(integerGene, map);
        this.max = integerGene.max;
        this.min = integerGene.min;
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

    @Override
    public IntegerGene deepCloneHelper(CloneMap map) {
        return new IntegerGene(this, map);
    }
}
