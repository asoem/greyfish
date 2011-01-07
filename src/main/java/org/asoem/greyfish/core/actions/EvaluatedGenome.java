package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.genes.Genome;

public class EvaluatedGenome extends Genome {

	/**
	 * 
	 */
	private static final long serialVersionUID = 80778815146904013L;
	private final double fitness;
	
	public EvaluatedGenome() {
		fitness = 0;
	}

	public EvaluatedGenome(Genome g) {
		super(g);
		fitness = 0;
	}

	public EvaluatedGenome(Genome sperm, double fitness) {
		super(sperm);
		this.fitness = fitness;
	}

	public double getFitness() {
		return fitness;
	}

	@Override
	public String toString() {
		return super.toString() + " (" + fitness + ")";
	}
}
