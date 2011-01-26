package org.asoem.greyfish.core.utils;

import java.util.ArrayList;
import java.util.List;

import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.individual.SimulationObject;
import org.asoem.greyfish.core.simulation.Simulation;
import org.uncommons.maths.statistics.DataSet;

public class GenomeUtils {


	public static void statistics(Simulation simulation) {
		final List<Genome> genomes = new ArrayList<Genome>();

		for (SimulationObject individual : simulation.getIndividuals()) {
			genomes.add(individual.getGenome());
		}

		if (genomes.size() > 0) {

			double[][] m = new double[genomes.get(0).size()][genomes.size()];

			int i = 0, j = 0;

			for (Genome genome : genomes) {
				i = 0;
				for (Gene<?> gene : genome.getGenes()) {
					m[i][j] = ((Number) gene.getRepresentation()).doubleValue();
					++i;
				}
				++j;
			}

			for (int j2 = 0; j2 < m.length; j2++) {
				System.out.println(String.valueOf(new DataSet(m[j2]).getArithmeticMean()));
			}
		}
	}
}
