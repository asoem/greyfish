package org.asoem.greyfish.core.share;

import org.asoem.greyfish.core.individual.MutableAgent;

public class Result {

	private MutableAgent winner;
	private MutableAgent looser;
	private boolean tie;

	public Result(MutableAgent winner, MutableAgent looser) {
		this(winner, looser, false);
	}

	public Result(MutableAgent winner, MutableAgent looser, boolean tie) {
		if(winner == null || looser == null)
			throw new NullPointerException();

		this.winner = winner;
		this.looser = looser;
		this.tie = tie;
	}

	/**
	 * @return the winner
	 */
	public MutableAgent getWinner() {
		return winner;
	}

	/**
	 * @return the looser
	 */
	public MutableAgent getLooser() {
		return looser;
	}

	/**
	 * @return the tie
	 */
	public boolean isTie() {
		return tie;
	}


}
