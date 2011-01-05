package org.asoem.sico.core.individual;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;

public class PrototypeManager extends AbstractCollection<Individual> {

	private final List<PrototypeRegistryListener> listeners = new Vector<PrototypeRegistryListener>();
	private final List<Individual> prototypes = new ArrayList<Individual>();

	public PrototypeManager() {
	}

	@Override
	public synchronized boolean add(Individual individual) {
		if ( ! hasCloneOf(individual)
				&& prototypes.add(individual)) {
			firePrototypeAdded(individual, indexOf(individual));
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean remove(Object individual) {
		Preconditions.checkArgument(individual instanceof Individual);
		final Individual individual2 = (Individual) individual;
		final int index = indexOf(individual2);
		if (index != -1
				&& prototypes.remove(individual2)) {
			firePrototypeRemoved(individual2, index);
			return true;
		}
		return false;
	}

	public void unregisterAllPrototypes() {
		for (int index = 0; index < prototypes.size(); index++) {
			remove(prototypes.get(index));
		}
	}

	public synchronized Individual[] getProptotypes() {
		return prototypes.toArray( new Individual[prototypes.size()] );
	}

	/**
	 * @return
	 */
	@Override
	public synchronized int size() {
		return prototypes.size();
	}

	public void addPrototypeRegistryListener(PrototypeRegistryListener listener) {
		listeners.add(listener);
	}

	public void removePrototypeRegistryListener(PrototypeRegistryListener listener) {
		listeners.remove(listener);
	}

	private void firePrototypeAdded(Individual individual, Integer index) {
		for (Iterator<PrototypeRegistryListener> i=listeners.iterator(); i.hasNext(); ) {
			PrototypeRegistryListener l = i.next();
			try {
				l.prototypeAdded(this, individual, index);
			}
			catch (RuntimeException e) {
				Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, "Unexpected exception in listener", e);
				i.remove();
			}
		}
	}

	private void firePrototypeRemoved(Individual individual, Integer index) {
		for (Iterator<PrototypeRegistryListener> i=listeners.iterator(); i.hasNext(); ) {
			PrototypeRegistryListener l = i.next();
			try {
				l.prototypeRemoved(this, individual, index);
			}
			catch (RuntimeException e) {
				Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, "Unexpected exception in listener", e);
				i.remove();
			}
		}
	}

	public Individual get(Population population) {
		for (Individual individual : getProptotypes()) {
			if (individual.getPopulation().equals(population))
				return individual;
		}
		return null;
	}

	public boolean hasCloneOf(Individual clone) {
		boolean ret = false;
		for (Individual individual : prototypes) {
			if (clone.isCloneOf(individual)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	public int indexOf(Individual individual) {
		return prototypes.indexOf(individual);
	}

	public Individual get(int index) {
		return prototypes.get(index);
	}

	@Override
	public Iterator<Individual> iterator() {
		return prototypes.iterator();
	}
}
