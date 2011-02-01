package org.asoem.greyfish.utils;

import javolution.util.FastList;
import org.asoem.greyfish.lang.Functor;

import static com.google.common.base.Preconditions.checkNotNull;

public class FastLists {

	public static <E> void foreach(FastList<E> list, Functor<E> functor) {
		checkNotNull(list);
		checkNotNull(functor);

		for (FastList.Node<E> n = list.head(), end = list.tail(); (n = n.getNext()) != end;) {
			assert (n != null);
			functor.update(n.getValue());
		}
	}
}
