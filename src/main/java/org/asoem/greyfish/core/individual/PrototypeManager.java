package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.ListenerSupport;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrototypeManager extends AbstractCollection<Prototype> { // TODO: replace inheritance with delegation

	private final ListenerSupport<PrototypeRegistryListener> listenerSupport = ListenerSupport.newInstance();
    private final List<Prototype> prototypes = Lists.newArrayList();

	public PrototypeManager() {
	}

	@Override
	public synchronized boolean add(Prototype individual) {
		if ( ! hasCloneOf(individual)
				&& prototypes.add(individual)) {
			firePrototypeAdded(individual, indexOf(individual));
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean remove(Object individual) {
		Preconditions.checkArgument(individual instanceof Prototype);
		final Prototype individual2 = (Prototype) individual;
		final int index = indexOf(individual2);
		if (index != -1
				&& prototypes.remove(individual2)) {
			firePrototypeRemoved(individual2, index);
			return true;
		}
		return false;
	}

	public void unregisterAllPrototypes() {
        for (Prototype prototype : prototypes) {
            remove(prototype);
        }
	}

	public synchronized Prototype[] getProptotypes() {
		return prototypes.toArray( new Prototype[prototypes.size()] );
	}

	/**
	 * @return
	 */
	@Override
	public synchronized int size() {
		return prototypes.size();
	}

	public void addPrototypeRegistryListener(PrototypeRegistryListener listener) {
		listenerSupport.addListener(listener);
	}

	public void removePrototypeRegistryListener(PrototypeRegistryListener listener) {
		listenerSupport.removeListener(listener);
	}

	private void firePrototypeAdded(final Prototype individual, final Integer index) {
        listenerSupport.notifyListeners( new Functor<PrototypeRegistryListener>() {
            @Override
            public void update(PrototypeRegistryListener l) {
                l.prototypeAdded(PrototypeManager.this, individual, index);
        }});
	}

	private void firePrototypeRemoved(final Prototype individual, final Integer index) {
		listenerSupport.notifyListeners( new Functor<PrototypeRegistryListener>() {
            @Override
            public void update(PrototypeRegistryListener l) {
                l.prototypeRemoved(PrototypeManager.this, individual, index);
        }});
	}

	public Prototype get(Population population) {
		for (Prototype individual : getProptotypes()) {
			if (individual.getPopulation().equals(population))
				return individual;
		}
		return null;
	}

	public boolean hasCloneOf(Prototype clone) {
		boolean ret = false;
		for (Prototype individual : prototypes) {
			if (clone.isCloneOf(individual)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	public int indexOf(Prototype individual) {
		return prototypes.indexOf(individual);
	}

	public Prototype get(int index) {
		return prototypes.get(index);
	}

	@Override
	public Iterator<Prototype> iterator() {
		return prototypes.iterator();
	}
}
