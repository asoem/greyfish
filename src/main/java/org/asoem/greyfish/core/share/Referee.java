package org.asoem.greyfish.core.share;

import org.asoem.greyfish.core.individual.AbstractAgent;

public class Referee {

	public static Result interfere( AbstractAgent ind1, AbstractAgent ind2) {
		Result ret = null;
		//	if(ind1.getStrength() > ind2.getStrength())
		//	    ret = new Result(ind1, ind2);
		//	if(ind2.getStrength() > ind1.getStrength())
		//	    ret = new Result(ind2, ind1);
		//	if(ind1.getStrength() == ind2.getStrength())
		//	    ret = new Result(ind1, ind2, true);
		return ret;
	}
}
