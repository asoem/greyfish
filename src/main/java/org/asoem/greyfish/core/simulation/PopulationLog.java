package org.asoem.greyfish.core.simulation;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.utils.Table;

public class PopulationLog extends Table {

	private Agent prototype;
	private Simulation simulation;

	public PopulationLog(Simulation simulation, Agent prototype) {
		Preconditions.checkNotNull(simulation);
		Preconditions.checkNotNull(prototype);

		this.prototype = prototype;
		this.simulation = simulation;

		addColumn("ID", Integer.class, 0);
		addColumn("birth_time", Integer.class, 0);
		addColumn("death_time", Integer.class, 0);
		for (GFProperty property : prototype.getProperties()) {
			for (Gene<?> product : property.getGeneList()) {
				addColumn(product.toString(), product.get().getClass(), null);
			}
		}
	}

	public void addLogEntry(Agent individual) {
		if ( ! individual.getPopulation().equals(prototype.getPopulation()))
			throw new IllegalArgumentException();
		Object[] row = new Object[getColumnCount()];
		row[0] = individual.getId();
		row[1] = simulation.getSteps();
		row[2] = individual.getTimeOfBirth();
		int i = 3;
		for (GFProperty property : individual.getProperties()) {
			for (Gene<?> product : property.getGeneList()) {
				row[i++] = product.get();
			}
		}
		addRow(row);
	}

	public Population getPopulation() {
		return prototype.getPopulation();
	}
}
