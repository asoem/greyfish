package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.individual.AbstractAgent;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * This class acts as an object pool for the AbstractAgent class to increase performance by object reuse.
 * See: <i>http://www.javaworld.com/javaworld/jw-11-1999/jw-11-performance.html?page=7</i>
 * @author christoph
 *
 */
public class PrototypePool {

	private static final int FREE_POOL_SIZE = 10000;  // Free pool capacity.

	private final ArrayList<Long> lastAccess = new ArrayList<Long>();

	private final ArrayList<ArrayDeque<AbstractAgent>> clonePool = new ArrayList<ArrayDeque<AbstractAgent>>();

	/**
	 * Keeps a list of all available prototypes (defined by isCloneOf(clone) function) and a to indicating its usage
	 */
	private final ArrayList<AbstractAgent> prototypeIndexMap = new ArrayList<AbstractAgent>();

	private int cloneCount = 0;

	/**
	 * @param prototype
	 * @return
	 */
	public synchronized AbstractAgent createClone(AbstractAgent prototype) {

		final int index = indexOfPrototype(prototype);
		if (index == -1) {
			addPrototype(prototype.deepClone(AbstractAgent.class));
			return createClone(prototype);
		}
		else if (clonePool.get(index).isEmpty()) {
			return prototype.deepClone(AbstractAgent.class);
		}
		else {
			--cloneCount;
			lastAccess.set(index, System.currentTimeMillis());
			return clonePool.get(index).pop();
		}
	}

	/**
	 * @param abstractAgent
	 */
	public synchronized void recycleClone(AbstractAgent abstractAgent) {

		if (cloneCount == FREE_POOL_SIZE) {
			remove();
		}

		final int index = indexOfPrototype(abstractAgent);
		if (index == -1) {
			addPrototype(abstractAgent);
		}
		clonePool.get(index).push(abstractAgent);
		++cloneCount;
	}

	/**
	 * @param prototype
	 * @return
	 */
	private int indexOfPrototype(AbstractAgent prototype) {
		for (int i = 0; i < prototypeIndexMap.size(); i++) {
			final AbstractAgent abstractAgent = prototypeIndexMap.get(i);
			if (abstractAgent.isCloneOf(prototype))
				return i;
		}
		return -1;
	}

	/**
	 * Add this abstractAgent as a prototype.
	 * It will be used for comparison with individuals returned in the future,
	 * using it's <code>isCloneOf(clone)</code> method, to locate the deque responsible for their storage.
	 * @param abstractAgent
	 */
	private void addPrototype(AbstractAgent abstractAgent) {
		prototypeIndexMap.add(abstractAgent);
		clonePool.add(new ArrayDeque<AbstractAgent>());
		lastAccess.add((long) 0);
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

	public AbstractAgent[] getProptotypes() {
		return prototypeIndexMap.toArray(new AbstractAgent[prototypeIndexMap.size()]);
	}

	public void clear() {
		clonePool.clear();
		prototypeIndexMap.clear();
		cloneCount = 0;
	}
}
