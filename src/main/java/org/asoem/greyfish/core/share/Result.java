package org.asoem.greyfish.core.share;

import org.asoem.greyfish.core.individual.AbstractAgent;

public class Result {

	private AbstractAgent winner;
	private AbstractAgent looser;
	private boolean tie;

	public Result(AbstractAgent winner, AbstractAgent looser) {
		this(winner, looser, false);
	}

	public Result(AbstractAgent winner, AbstractAgent looser, boolean tie) {
		if(winner == null || looser == null)
			throw new NullPointerException();

		this.winner = winner;
		this.looser = looser;
		this.tie = tie;
	}

	/**
	 * @return the winner
	 */
	public AbstractAgent getWinner() {
		return winner;
	}

	/**
	 * @return the looser
	 */
	public AbstractAgent getLooser() {
		return looser;
	}

	/**
	 * @return the tie
	 */
	public boolean isTie() {
		return tie;
	}


}
