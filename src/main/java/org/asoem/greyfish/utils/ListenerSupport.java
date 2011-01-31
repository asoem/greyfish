package org.asoem.greyfish.utils;

import org.asoem.greyfish.lang.Functor;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListenerSupport<T> {

	private List<T> listeners = new Vector<T>(0);

	public ListenerSupport() {
	}

	public void addListener(T listener) {
		if (listener != null)
			listeners.add(listener);
	}

	public void removeListener(T listener) {
		listeners.remove(listener);
	}

	public void notifyListeners(Functor<T> command) {
		for (Iterator<T> i=listeners.iterator(); i.hasNext(); ) {
			final T l = i.next();
			try {
				command.update(l);
			}
			catch (RuntimeException e) {
				Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, "Unexpected exception in listener", e);
				i.remove();
			}
		}
	}

    public static <T> ListenerSupport<T> newInstance() {
        return new ListenerSupport<T>();
    }
}
