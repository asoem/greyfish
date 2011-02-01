package org.asoem.greyfish.utils;

import com.google.common.collect.Lists;
import org.asoem.greyfish.lang.Functor;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListenerSupport<T> {

	private List<T> listeners = null;

	public ListenerSupport() {
	}

    private synchronized List<T> getListeners() {
        if (listeners == null)
            listeners = Lists.newArrayList();
        return listeners;
    }

	public void addListener(T listener) {
		if (listener != null)
			getListeners().add(listener);
	}

	public void removeListener(T listener) {
		getListeners().remove(listener);
	}

	public void notifyListeners(Functor<T> command) {
		for (Iterator<T> i=getListeners().iterator(); i.hasNext(); ) {
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
