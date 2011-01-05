package org.asoem.sico.helper;

import org.asoem.sico.core.actions.MatingReceiverAction;
import org.asoem.sico.core.actions.MatingTransmitterAction;
import org.asoem.sico.core.actions.RandomMovementAction;
import org.asoem.sico.core.actions.SexualReproductionAction;
import org.asoem.sico.core.individual.Individual;
import org.asoem.sico.core.individual.Population;
import org.asoem.sico.core.properties.EvaluatedGenomeStorage;

public class CreatePrototype_SRI {

	private static final String MATE_MESSAGE = "mate";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Population population = new Population("Hund");
		final Individual individual = new Individual(population);
		
		// PROPERTIES
		final EvaluatedGenomeStorage spermBuffer = new EvaluatedGenomeStorage();
		
		
		// ACTIONS
		final RandomMovementAction action_rm = new RandomMovementAction("move");
		individual.addAction(action_rm);
		
		final SexualReproductionAction action_rep = new SexualReproductionAction("rep", spermBuffer, 1);
		individual.addAction(action_rep);
		
		final MatingTransmitterAction matingTransmitterAction = new MatingTransmitterAction("tSperm", MATE_MESSAGE);
		individual.addAction(matingTransmitterAction);
		
		final MatingReceiverAction matingReceiverAction = new MatingReceiverAction("rSperm", spermBuffer, matingTransmitterAction, MATE_MESSAGE);
		individual.addAction(matingReceiverAction);
	}

}
