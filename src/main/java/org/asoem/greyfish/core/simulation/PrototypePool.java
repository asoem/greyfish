package org.asoem.greyfish.core.simulation;

import java.util.ArrayDeque;
import java.util.ArrayList;

import org.asoem.greyfish.core.individual.Individual;

/**
 * This class acts as an object pool for the Individual class to increase performance by object reuse.
 * See: <i>http://www.javaworld.com/javaworld/jw-11-1999/jw-11-performance.html?page=7</i>
 * @author christoph
 *
 */
public class PrototypePool {

	private static final int FREE_POOL_SIZE = 10000;  // Free pool capacity.

	private final ArrayList<Long> lastAccess = new ArrayList<Long>();

	private final ArrayList<ArrayDeque<Individual>> clonePool = new ArrayList<ArrayDeque<Individual>>();

	/**
	 * Keeps a list of all available prototypes (defined by isCloneOf(clone) function) and a to indicating its usage
	 */
	private final ArrayList<Individual> prototypeIndexMap = new ArrayList<Individual>();

	private int cloneCount = 0;

	/**
	 * @param prototype
	 * @return
	 */
	public synchronized Individual createClone(Individual prototype) {

		final int index = indexOfPrototype(prototype);
		if (index == -1) {
			addPrototype((Individual) prototype.deepClone());
			return createClone(prototype);
		}
		else if (clonePool.get(index).isEmpty()) {
			return (Individual) prototype.deepClone();
		}
		else {
			--cloneCount;
			lastAccess.set(index, System.currentTimeMillis());
			return clonePool.get(index).pop();
		}
	}

	/**
	 * @param individual
	 */
	public synchronized void recycleClone(Individual individual) {

		if (cloneCount == FREE_POOL_SIZE) {
			remove();
		}

		final int index = indexOfPrototype(individual);
		if (index == -1) {
			addPrototype(individual);
		}
		clonePool.get(index).push(individual);
		++cloneCount;
	}

	/**
	 * @param prototype
	 * @return
	 */
	private int indexOfPrototype(Individual prototype) {
		for (int i = 0; i < prototypeIndexMap.size(); i++) {
			final Individual individual = prototypeIndexMap.get(i);
			if (individual.isCloneOf(prototype))
				return i;
		}
		return -1;
	}

	/**
	 * Add this individual as a prototype.
	 * It will be used for comparison with individuals returned in the future,
	 * using it's <code>isCloneOf(clone)</code> method, to locate the deque responsible for their storage.
	 * @param individual
	 */
	private void addPrototype(Individual individual) {
		prototypeIndexMap.add(individual);
		clonePool.add(new ArrayDeque<Individual>());
		lastAccess.add(new Long(0));
	}

	/**
	 * Remove one individual from the deque with smallest timestamp.
	 */
	private void remove() {
		Long min = Long.MAX_VALUE;
		int ret_index = -1;
		for (int index = 0; index < prototypeIndexMap.size(); index++) {
			if ( ! clonePool.get(index).isEmpty()
					&& lastAccess.get(index) < min) {
				ret_index = index;
			}
		}
		assert(ret_index != -1); // since this function is ONLY called when stack is full (assuming FREE_POOL_SIZE > 0)
		clonePool.get(ret_index).remove();
		--cloneCount;
	}

	public Individual[] getProptotypes() {
		return prototypeIndexMap.toArray(new Individual[prototypeIndexMap.size()]);
	}

	public void clear() {
		clonePool.clear();
		prototypeIndexMap.clear();
		cloneCount = 0;
	}
}
