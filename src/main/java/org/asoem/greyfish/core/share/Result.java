package org.asoem.greyfish.core.share;

import org.asoem.greyfish.core.individual.DefaultAgent;

public class Result {

	private DefaultAgent winner;
	private DefaultAgent looser;
	private boolean tie;

	public Result(DefaultAgent winner, DefaultAgent looser) {
		this(winner, looser, false);
	}

	public Result(DefaultAgent winner, DefaultAgent looser, boolean tie) {
		if(winner == null || looser == null)
			throw new NullPointerException();

		this.winner = winner;
		this.looser = looser;
		this.tie = tie;
	}

	/**
	 * @return the winner
	 */
	public DefaultAgent getWinner() {
		return winner;
	}

	/**
	 * @return the looser
	 */
	public DefaultAgent getLooser() {
		return looser;
	}

	/**
	 * @return the tie
	 */
	public boolean isTie() {
		return tie;
	}


}
