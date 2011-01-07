package org.asoem.greyfish.core.share;

import org.asoem.greyfish.core.individual.Individual;

public class Result {

	private Individual winner;
	private Individual looser;
	private boolean tie;

	public Result(Individual winner, Individual looser) {
		this(winner, looser, false);
	}

	public Result(Individual winner, Individual looser, boolean tie) {
		if(winner == null || looser == null)
			throw new NullPointerException();

		this.winner = winner;
		this.looser = looser;
		this.tie = tie;
	}

	/**
	 * @return the winner
	 */
	public Individual getWinner() {
		return winner;
	}

	/**
	 * @return the looser
	 */
	public Individual getLooser() {
		return looser;
	}

	/**
	 * @return the tie
	 */
	public boolean isTie() {
		return tie;
	}


}
