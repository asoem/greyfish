package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.RandomUtils;
import org.uncommons.maths.random.GaussianGenerator;

public class DoubleGene extends AbstractGene<Double> {

	private double min;
	
	private double max;
	
	private static final GaussianGenerator GUSSIAN_GENERATOR = new GaussianGenerator(0, 3, RandomUtils.RNG);

    private DoubleGene(DoubleGene doubleGene, CloneMap map) {
        super(doubleGene, map);
        min = doubleGene.min;
        max = doubleGene.max;
    }

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

	public DoubleGene(Double init, Double min, Double max) {
		super(init);
        this.max = max;
        this.min = min;
	}

	private DoubleGene(DoubleGene gene) {
		this(gene.representation, gene.min, gene.max);
	}

	@Override
	public void mutate() {
		representation += GUSSIAN_GENERATOR.nextValue();
		representation = Math.max(min, Math.min(max, representation));
	}

	@Override
	public void initialize() {
		representation = RandomUtils.nextDouble(min, max); // TODO max is exclusive in func. but inclusive as field
	}

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new DoubleGene(this, map);
    }
}
