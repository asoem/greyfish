package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.core.individual.DefaultAgent;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * This class acts as an object pool for the DefaultAgent class to increase performance by object reuse.
 * See: <i>http://www.javaworld.com/javaworld/jw-11-1999/jw-11-performance.html?page=7</i>
 * @author christoph
 *
 */
public class PrototypePool {

	private static final int FREE_POOL_SIZE = 10000;  // Free pool capacity.

	private final ArrayList<Long> lastAccess = new ArrayList<Long>();

	private final ArrayList<ArrayDeque<DefaultAgent>> clonePool = new ArrayList<ArrayDeque<DefaultAgent>>();

	/**
	 * Keeps a list of all available prototypes (defined by isCloneOf(clone) function) and a to indicating its usage
	 */
	private final ArrayList<DefaultAgent> prototypeIndexMap = new ArrayList<DefaultAgent>();

	private int cloneCount = 0;

	/**
	 * @param prototype
	 * @return
	 */
	public synchronized DefaultAgent createClone(DefaultAgent prototype) {

		final int index = indexOfPrototype(prototype);
		if (index == -1) {
			addPrototype(prototype.deepClone(DefaultAgent.class));
			return createClone(prototype);
		}
		else if (clonePool.get(index).isEmpty()) {
			return prototype.deepClone(DefaultAgent.class);
		}
		else {
			--cloneCount;
			lastAccess.set(index, System.currentTimeMillis());
			return clonePool.get(index).pop();
		}
	}

	/**
	 * @param defaultAgent
	 */
	public synchronized void recycleClone(DefaultAgent defaultAgent) {

		if (cloneCount == FREE_POOL_SIZE) {
			remove();
		}

		final int index = indexOfPrototype(defaultAgent);
		if (index == -1) {
			addPrototype(defaultAgent);
		}
		clonePool.get(index).push(defaultAgent);
		++cloneCount;
	}

	/**
	 * @param prototype
	 * @return
	 */
	private int indexOfPrototype(DefaultAgent prototype) {
		for (int i = 0; i < prototypeIndexMap.size(); i++) {
			final DefaultAgent defaultAgent = prototypeIndexMap.get(i);
			if (defaultAgent.isCloneOf(prototype))
				return i;
		}
		return -1;
	}

	/**
	 * Add this defaultAgent as a prototype.
	 * It will be used for comparison with individuals returned in the future,
	 * using it's <code>isCloneOf(clone)</code> method, to locate the deque responsible for their storage.
	 * @param defaultAgent
	 */
	private void addPrototype(DefaultAgent defaultAgent) {
		prototypeIndexMap.add(defaultAgent);
		clonePool.add(new ArrayDeque<DefaultAgent>());
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

	public DefaultAgent[] getProptotypes() {
		return prototypeIndexMap.toArray(new DefaultAgent[prototypeIndexMap.size()]);
	}

	public void clear() {
		clonePool.clear();
		prototypeIndexMap.clear();
		cloneCount = 0;
	}
}
