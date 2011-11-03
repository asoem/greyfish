package org.asoem.greyfish.utils.base;

import com.google.common.collect.Sets;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

public class ListenerSupport<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListenerSupport.class);
    private Set<T> listeners = null;

	public ListenerSupport() {
	}

    private synchronized Set<T> getListeners() {
        if (listeners == null)
            listeners = Sets.newHashSet();
        return listeners;
    }

	public void addListener(T listener) {
		getListeners().add(listener);
	}

	public void removeListener(T listener) {
		getListeners().remove(listener);
	}

	public void notifyListeners(VoidFunction<T> command) {
		for (Iterator<T> i=getListeners().iterator(); i.hasNext(); ) {
			final T l = i.next();
			try {
				command.apply(l);
			}
			catch (RuntimeException e) {
                LOGGER.error("Unexpected exception in listener", e);
				i.remove();
			}
		}
	}

    public static <T> ListenerSupport<T> newInstance() {
        return new ListenerSupport<T>();
    }
}
