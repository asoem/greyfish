package org.asoem.greyfish.utils.base;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListenerSupport<T> {
    
    private Set<T> listeners = null;

	public ListenerSupport() {
	}

    private synchronized Set<T> getListeners() {
        if (listeners == null)
            listeners = Sets.newHashSet();
        return listeners;
    }

	public void addListener(final T listener) {
		getListeners().add(listener);
	}

	public void removeListener(final T listener) {
		getListeners().remove(listener);
	}

	public void notifyListeners(final Function<T, Void> command) {
		for (Iterator<T> i=getListeners().iterator(); i.hasNext(); ) {
			final T l = i.next();
			try {
				command.apply(l);
			}
			catch (RuntimeException e) {
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, "Exception occurred in listener " + l + ". It will no longer be notified from " + this + ".", e);
				i.remove();
			}
		}
	}

    public static <T> ListenerSupport<T> newInstance() {
        return new ListenerSupport<T>();
    }
}
