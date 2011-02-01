package org.asoem.greyfish.core.utils;

import com.google.common.collect.Lists;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.individual.IndividualInterface;
import org.asoem.greyfish.core.simulation.Simulation;
import org.uncommons.maths.statistics.DataSet;

import java.util.List;

public class GenomeUtils {


	public static void statistics(Simulation simulation) {
		final List<Genome> genomes = Lists.newArrayList();

		for (IndividualInterface individual : simulation.getAgents()) {
			genomes.add(individual.getGenome());
		}

		if (genomes.size() > 0) {

			double[][] m = new double[genomes.get(0).size()][genomes.size()];

			int i, j = 0;

			for (Genome genome : genomes) {
				i = 0;
				for (Gene<?> gene : genome.getGenes()) {
					m[i][j] = ((Number) gene.getRepresentation()).doubleValue();
					++i;
				}
				++j;
			}

            for (double[] aM : m) {
                System.out.println(String.valueOf(new DataSet(aM).getArithmeticMean()));
            }
		}
	}
}
