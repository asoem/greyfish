package org.asoem.greyfish.core.individual;

import com.google.common.base.Predicates;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.asoem.greyfish.lang.Functor;
import org.asoem.greyfish.utils.ListenerSupport;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class PrototypeManager extends ForwardingSet<Agent> {

	private final ListenerSupport<PrototypeRegistryListener> listenerSupport = ListenerSupport.newInstance();
    private final Set<Agent> prototypes = Sets.newHashSet();

	public PrototypeManager() {
	}

    @Override
    protected Set<Agent> delegate() {
        return prototypes;
    }

    @Override
	public synchronized boolean add(Agent individual) {
		if ( ! hasCloneOf(individual)
				&& delegate().add(individual)) {
			firePrototypeAdded(individual, indexOf(individual));
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean remove(Object object) {
		final Agent prototype = Agent.class.cast(checkNotNull(object));

		final int index = indexOf(prototype);
		if (index != -1 && delegate().remove(prototype)) {
			firePrototypeRemoved(prototype, index);
			return true;
		}
		return false;
	}

    @Override
    public boolean removeAll(Collection<?> collection) {
        for (Object o : collection) {
            remove(o);
        }
        return true;
    }

    public void unregisterAllPrototypes() {
        for (Agent prototype : prototypes) {
            remove(prototype);
        }
	}

	public synchronized Agent[] getProptotypes() {
		return Iterables.toArray(prototypes, Agent.class);
	}

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

	private void firePrototypeAdded(final Agent individual, final Integer index) {
        listenerSupport.notifyListeners( new Functor<PrototypeRegistryListener>() {
            @Override
            public void apply(PrototypeRegistryListener l) {
                l.prototypeAdded(PrototypeManager.this, individual, index);
        }});
	}

	private void firePrototypeRemoved(final Agent individual, final Integer index) {
		listenerSupport.notifyListeners( new Functor<PrototypeRegistryListener>() {
            @Override
            public void apply(PrototypeRegistryListener l) {
                l.prototypeRemoved(PrototypeManager.this, individual, index);
        }});
	}

	public Agent get(Population population) {
		for (Agent individual : getProptotypes()) {
			if (individual.getPopulation().equals(population))
				return individual;
		}
		return null;
	}

	public boolean hasCloneOf(Agent clone) {
		boolean ret = false;
		for (Agent individual : delegate()) {
			if (clone.isCloneOf(individual)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	public int indexOf(Agent individual) {
		return Iterables.indexOf(delegate(), Predicates.equalTo(individual));
	}

	public Agent get(int index) {
		return Iterables.get(delegate(), index);
	}

	@Override
	public Iterator<Agent> iterator() {
		return delegate().iterator();
	}
}
